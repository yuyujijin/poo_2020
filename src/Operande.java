import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

public class Operande implements Flow.Publisher {
    private Object value;
    private final SubmissionPublisher output = new SubmissionPublisher<>();

    public Operande(Object value){
        this.value = value;
    }

    public void updateValue(Object value){
        System.out.println("j'ai update avec : "+value);
        this.value = value;
        output.submit(this.value);
    }

    public Object getValue(){
        return value;
    }

    @Override
    public void subscribe(Flow.Subscriber subscriber) {
        output.subscribe(subscriber);
        output.submit(this.value);
    }

    public String toString(){
        return value.toString();
    }

    public static final class OperandeWithInputs extends Operande{
        private Flow.Subscription subscription;

        public OperandeWithInputs(Operande[] operandes, Operation operation) {
            super(null);
            this.operation = operation;
            this.inputs = new OperandeWithInputs.FanInSubscriber[operandes.length];
            this.values = new Object[operandes.length];
            for(int i = 0; i < inputs.length; i++){
                int finalI = i;
                inputs[i] = new OperandeWithInputs.FanInSubscriber(message -> {
                    values[finalI] = message;});
            }
            for(int i = 0; i < operandes.length; i++) operandes[i].subscribe(inputs[i]);
        }

        public class FanInSubscriber<T> implements Flow.Subscriber<T> {
            private final Consumer<T> store;
            private Flow.Subscription subscription;

            public FanInSubscriber(Consumer<T> store) {
                this.store = store;
            }

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(T message) {
                synchronized (OperandeWithInputs.this) {
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

        public final OperandeWithInputs.FanInSubscriber<Object>[] inputs;

        private Object[] values;

        private final Operation operation;


        public boolean valuesNull(){
            for(int i = 0; i < values.length; i++) if(values[i] == null) return true;
            return false;
        }

        private void tryProcessNext() {
            if (valuesNull())
                return; // la source en avance s'arrête là et ne fait rien de particulier
            updateValue(operation.compute(values));
            for(int i = 0; i < inputs.length; i++) inputs[i].subscription.request(1);
        }
    }
}
