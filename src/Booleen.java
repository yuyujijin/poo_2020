public class Booleen extends Operande{
    private final boolean value;

    private Booleen(boolean b){ value = b; }
    private Booleen(String s){
        switch(s){
            case "true" : value = true; break;
            default : value = false; break;
        }
    }

    public static Booleen getBooleen(String s){
        if(!s.toLowerCase().equals("true") && !s.toLowerCase().equals("false")) return null;
        return new Booleen(s.toLowerCase());
    }

    public static Booleen getBooleen(boolean b){
        return new Booleen(b);
    }


    public String toString(){
        if(value) return "TRUE";
        return "FALSE";
    }

    public boolean getValue() {
        return value;
    }
}
