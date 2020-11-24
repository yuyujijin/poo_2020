public abstract class OperationNombre extends Operation{

    private OperationNombre(int n) {
        super(n);
    }

    private static class OperationNombrePlus extends Operation{
        private OperationNombrePlus(){ super(2); }
        public Operande operate(Operande x, Operande y) {
            if(!(x instanceof Nombre) || !(y instanceof Nombre)) throw new IllegalArgumentException("L'un des arguments n'est pas du bon type.");
            int val = ((Nombre) x).getValue() + ((Nombre) y).getValue();
            return Nombre.getNombre(val);
        }
    }
    private static class OperationNombreMoins extends Operation{
        private OperationNombreMoins(){ super(2); }
        public Operande operate(Operande x, Operande y) {
            if(!(x instanceof Nombre) || !(y instanceof Nombre)) throw new IllegalArgumentException("L'un des arguments n'est pas du bon type.");
            int val = ((Nombre) x).getValue() - ((Nombre) y).getValue();
            return Nombre.getNombre(val);
        }
    }
    private static class OperationNombreDiv extends Operation{
        private OperationNombreDiv(){ super(2); }
        public Operande operate(Operande x, Operande y) {
            if(!(x instanceof Nombre) || !(y instanceof Nombre)) throw new IllegalArgumentException("L'un des arguments n'est pas du bon type.");
            int val = ((Nombre) x).getValue() / ((Nombre) y).getValue();
            return Nombre.getNombre(val);
        }
    }
    private static class OperationNombreMul extends Operation{
        private OperationNombreMul(){ super(2); }
        public Operande operate(Operande x, Operande y) {
            if(!(x instanceof Nombre) || !(y instanceof Nombre)) throw new IllegalArgumentException("L'un des arguments n'est pas du bon type.");
            int val = ((Nombre) x).getValue() * ((Nombre) y).getValue();
            return Nombre.getNombre(val);
        }
    }

    public static Operation getOperationNombrePlus(){ return new OperationNombrePlus(); }
    public static Operation getOperationNombreMoins(){ return new OperationNombreMoins(); }
    public static Operation getOperationNombreMul(){ return new OperationNombreMul(); }
    public static Operation getOperationNombreDiv(){ return new OperationNombreDiv(); }
}
