import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

public abstract class Token implements Flow.Publisher {
    private final SubmissionPublisher output;

    private Token(){
        this.output = new SubmissionPublisher();
    }

    public abstract Object getValue();

    @Override
    public final void subscribe(Flow.Subscriber subscriber){
        output.subscribe(subscriber);
        output.submit(getValue());
    }
    protected final Optional<Flow.Subscriber> unsubscribe(){
        if(output.hasSubscribers()) {
            Flow.Subscriber s = (Flow.Subscriber) output.getSubscribers().get(0);
            output.close();
            return Optional.of(s);
        }
        output.close();
        return Optional.empty();
    }

    public abstract Optional<Flow.Subscriber> delete();

    protected void submit(Object o){
        output.submit(o);
    }

    public final boolean isSubscribed(){
        return output.hasSubscribers();
    }

    public final String valueClassToString(){ return getValue().getClass().getSimpleName(); }

    public String toString(){
        return getValue().toString();
    }

    public static final class OperandToken extends Token{
        private Object value;

        public OperandToken(Object o){
            this.value = o;
        }

        public final Optional<Flow.Subscriber> delete(){
            return super.unsubscribe();
        }

        public void updateValue(Object o){
            if(!o.getClass().isInstance(value)) throw new IllegalArgumentException();
            this.value = o;
            submit(o);
        }

        public Object getValue(){ return value; }
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
            for(int i = 0; i < operandes.length; i++) operandes[i].subscribe(inputs[i]);
        }

        public final Optional<Flow.Subscriber> delete(){
            deleteSubs();
            return super.unsubscribe();
        }

        public void deleteSubs(){
            for(FanInSubscriber f : inputs) f.subscription.cancel();
        }

        public Object getValue(){
            return operation.compute(values);
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
                synchronized (OperationToken.this) {
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
            submit(getValue());
            for(int i = 0; i < inputs.length; i++) inputs[i].subscription.request(1);
        }
    }

    public static final class RecallToken extends Token implements Flow.Subscriber{
        private Object value;
        private Flow.Subscription subscription;

        public RecallToken(Token t){
            t.subscribe(this);
            value = t.getValue();
        }

        public final Optional<Flow.Subscriber> delete(){
            subscription.cancel();
            return super.unsubscribe();
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(Object item) {
            synchronized (this) {
                this.value = item;
                subscription.request(1);
                submit(this.value);
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
