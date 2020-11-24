public abstract class OperationBooleen extends Operation{

    private OperationBooleen(int n){
        super(n);
    }

    private static class OperationEt extends OperationBooleen{
        private OperationEt(){
            super(2);
        }

        @Override
        public Operande operate(Operande x, Operande y) {
            if(!(x instanceof Booleen) || !(y instanceof Booleen)) throw new IllegalArgumentException("L'un des arguments n'est pas du bon type.");
            return Booleen.getBooleen(((Booleen) x).getValue() && ((Booleen) y).getValue());
        }
    }

    public static Operation getOperationBooleenEt(){ return new OperationEt(); }
}
