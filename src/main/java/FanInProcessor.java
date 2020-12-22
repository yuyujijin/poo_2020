import java.util.concurrent.Flow;
import java.util.concurrent.Flow.*;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.*;

public class FanInProcessor implements Publisher, AutoCloseable,  Flow.Subscriber {
    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Object item) {
        System.out.println("("+this+") reçoit : " + item);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }

    public class FanInSubscriber<T> implements Subscriber<T> {
        private final Consumer<T> store;
        private Subscription subscription;

        public FanInSubscriber(Consumer<T> store) {
            this.store = store;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(T message) {
            synchronized (FanInProcessor.this) {
                store.accept(message);
                tryProcessNext();
            }
        }

        @Override
        public void onError(Throwable arg0) {
        }

        @Override
        public void onComplete() {
        }

    }

    public final FanInSubscriber<Object>[] inputs;

    private Object[] lastInputs;

    private final SubmissionPublisher output = new SubmissionPublisher<>();
    private final Operation operation;

    @Override
    public void subscribe(Subscriber subscriber) {
        output.subscribe(subscriber);
    }

    @Override
    public void close() {
        output.close();
    }

    public boolean inputsNull(){
        for(int i = 0; i < lastInputs.length; i++) if(lastInputs[i] == null) return true;
        return false;
    }

    private void tryProcessNext() {
        if (inputsNull())
            return; // la source en avance s'arrête là et ne fait rien de particulier
        System.out.println("("+this+") envoie : "+lastInputs[0]+" "+lastInputs[1]+" = "+operation.compute(lastInputs));
        output.submit(operation.compute(lastInputs));
        for(int i = 0; i < inputs.length; i++) inputs[i].subscription.request(1);
    }

    public FanInProcessor(Operation operation, int n) {
        this.operation = operation;
        this.inputs = new FanInSubscriber[n];
        this.lastInputs = new Object[n];
        for(int i = 0; i < n; i++){
            int finalI = i;
            inputs[i] = new FanInSubscriber<>(message -> {
                System.out.println(finalI+" "+message);
                lastInputs[finalI] = message;});
        }
    }

}
