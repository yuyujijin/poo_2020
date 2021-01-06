import java.util.Arrays;

public abstract class Token{

    private Token() {}
    public abstract Object getValue();
    public String toString(){
        return getValue().toString();
    }

    public static final class OperandToken extends Token{
        private Object value;
        public OperandToken(Object o){
            this.value = o;
        }
        public final Object getValue(){
            return value;
        }
    }

    public static final class OperationToken extends Token{
        private Operation operation;
        private Object value;

        public OperationToken(Operation o){
            this.operation = o;
        }

        public void compute(Token... t){
            this.value = operation.compute(Arrays.stream(t).map(x -> x.getValue()).toArray());
        }

        public Operation getOperation(){
            return operation;
        }

        public final Object getValue(){
            return value;
        }
    }

    public static final class RecallToken extends Token{
        private Token token;
        public RecallToken(Token t){
            this.token = t;
        }

        public final Object getValue(){
            return token.getValue();
        }
    }
}