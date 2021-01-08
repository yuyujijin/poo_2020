import java.util.*;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

public abstract class Token implements Flow.Publisher {
    private final SubmissionPublisher output;
    private Object value;

    private Token(){
        this.output = new SubmissionPublisher();
    }

    public Object getValue(){ return this.value; }
    protected void setValue(Object o){ this.value = o; }

    @Override
    public synchronized final void subscribe(Flow.Subscriber subscriber){
        output.subscribe(subscriber);
        output.submit(getValue());
    }

    public synchronized final void subscribe(List<Flow.Subscriber> subscribers){
        subscribers.stream().forEach(subscriber -> subscribe(subscriber));
    }
    protected final Optional<List<Flow.Subscriber>> unsubscribe(){
        if(output.hasSubscribers()){
            List<Flow.Subscriber> l = output.getSubscribers();
            output.close();
            return Optional.of(l);
        }
        return Optional.empty();
    }


    public abstract Optional<List<Flow.Subscriber>> delete();

    protected void submit(Object o){
        output.submit(o);
    }

    public final String valueClassToString(){ return getValue().getClass().getSimpleName(); }

    public String toString(){
        return getValue().toString();
    }

    public static final class OperandToken extends Token{

        public OperandToken(Object o){
            setValue(o);
        }

        @Override
        public Object getValue() {
            return super.getValue();
        }

        public final Optional<List<Flow.Subscriber>> delete(){
            return super.unsubscribe();
        }
    }

    public static final class OperationToken extends Token{
        private Object[] values;
        private final Operation operation;
        public final OperationToken.FanInSubscriber<Object>[] inputs;
        private final String operationName;


        public OperationToken(Token[] operandes, Operation operation, String operationName) {
            this.operation = operation;
            this.operationName = operationName;
            this.inputs = new OperationToken.FanInSubscriber[operandes.length];
            this.values = new Object[operandes.length];
            for(int i = 0; i < inputs.length; i++){
                int finalI = i;
                inputs[i] = new OperationToken.FanInSubscriber(message -> {
                    values[finalI] = message;});
            }
            for(int i = 0; i < operandes.length; i++){
                operandes[i].subscribe(inputs[i]);
            }
        }

        public final Optional<List<Flow.Subscriber>> delete(){
            deleteSubs();
            return super.unsubscribe();
        }

        public void deleteSubs(){
            for(FanInSubscriber f : inputs) f.subscription.cancel();
        }

        public Object getValue(){
            // Dans le cas d'une demande de valeur alors que tout les inputs n'ont pas été envoyé (appel bloquant)
            while (valuesNull());
            if(super.getValue() == null) setValue(operation.compute(values));
            return super.getValue();
        }

        public String toString(){
            return "("+operationName+") "+super.toString();
        }


        private final class FanInSubscriber<T> implements Flow.Subscriber<T>{
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
                synchronized (this) {
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


        public boolean valuesNull(){
            for(int i = 0; i < values.length; i++) if(values[i] == null) return true;
            return false;
        }

        private void tryProcessNext() {
            if (valuesNull())
                return; // la source en avance s'arrête là et ne fait rien de particulier
            // On force le recalcule
            setValue(operation.compute(values));
            submit(getValue());
            for(int i = 0; i < inputs.length; i++) inputs[i].subscription.request(1);
        }
    }

    public static final class RecallToken extends Token implements Flow.Subscriber{
        private String name;
        private Flow.Subscription subscription;
        private Token subscriber;

        public RecallToken(Token t, String s){
            subscriber = t;
            subscriber.subscribe(this);
            name = s;
            setValue(t.getValue());
        }

        public final void cancelSubscription(){
            subscription.cancel();
        }

        public final String toString(){
            return "("+name+") "+super.toString();
        }

        public final Optional<List<Flow.Subscriber>> delete(){
            subscription.cancel();
            return super.unsubscribe();
        }

        public void updateValue(Object o){
            setValue(o);
            submit(o);
        }

        @Override
        public Object getValue() {
            return super.getValue();
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(Object item) {
            synchronized (this) {
                setValue(item);
                subscription.request(1);
                submit(item);
            }
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onComplete() {

        }
    }
}
