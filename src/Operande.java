public abstract class Operande {

    public static Operande createOperande(String s){
        if(Nombre.getNombre(s) != null) return Nombre.getNombre(s);
        if(Booleen.getBooleen(s) != null) return Booleen.getBooleen(s);
        return null;
    }


}
