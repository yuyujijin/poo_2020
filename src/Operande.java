public final class Operande<T>{
    private final T value;
    private final Class type;

    private Operande(T value, Class t) {
        this.value = value;
        type = t;
    }

    public static <T> Operande createOperande(String s){
        Object obj;
        if((obj = getFractionValue(s)) != null) return new Operande<>((Fraction) obj,Fraction.class);
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
    private static Fraction getFractionValue(String s){
        if(s.contains("/")){
            int d, n;
            try{
                n = Integer.parseInt(s.substring(0, s.indexOf("/")));
                d = Integer.parseInt(s.substring(s.indexOf("/") + 1));
                return new Fraction(n,d);
            }catch(Exception e){
                return null;
            }
        }
        return null;
    }
}
