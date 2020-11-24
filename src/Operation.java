public abstract class Operation{
    public final int arite;

    public Operation(int n){ arite = n; }
    public abstract Operande operate(Operande x, Operande y);
    public int getArite(){ return arite; }

}
