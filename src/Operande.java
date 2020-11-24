import java.util.ArrayList;
import java.util.List;

public final class Operande<T>{
    private final T value;
    private final Class type;

    private Operande(T value, Class t) {
        this.value = value;
        type = t;
    }

    public static <T> Operande createOperande(String s){
        Object obj;
        if((obj = getIntegerValue(s)) != null) return new Operande<>((Integer) obj,Integer.class);
        if((obj = getBooleanValue(s)) != null) return new Operande<>((Boolean) obj,Boolean.class);
        return null;
    }

    public T getValue(){ return value; }
    public String toString(){ return String.valueOf(value); }
    public Class getType(){ return type; }

    private static Integer getIntegerValue(String s){
        try{ return Integer.parseInt(s); }catch(Exception e){ return null; }
    }
    private static Boolean getBooleanValue(String s){
        if(s.toLowerCase().equals("true")) return Boolean.TRUE;
        if(s.toLowerCase().equals("false")) return Boolean.FALSE;
        return null;
    }
}
