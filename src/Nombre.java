public class Nombre extends Operande{
    private final int value;

    private Nombre(int v){ value = v; }
    private Nombre(String s){
        this(Integer.parseInt(s));
    }

    public static Nombre getNombre(String s){
        try{
            return new Nombre(s);
        }catch(Exception e){
            return null;
        }
    }
    public static Nombre getNombre(int x){
        return new Nombre(x);
    }

    public String toString() {
        return String.valueOf(value);
    }

    public int getValue(){
        return value;
    }


}
