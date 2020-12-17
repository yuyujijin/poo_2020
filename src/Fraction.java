public final class Fraction{
    private final int numérateur, dénominateur;
    Fraction(int n, int d){
        if(d == 0) throw new IllegalArgumentException("Fraction avec un dénominateur a 0.");
        numérateur = n; dénominateur = d;
    }
    public static Object sum(Fraction f1, Fraction f2){
        return new Fraction(f1.numérateur * f2.dénominateur + f2.numérateur * f1.dénominateur, f1.dénominateur * f2.dénominateur).reduce();
    }
    public static Object substract(Fraction f1, Fraction f2){
        return new Fraction(f1.numérateur * f2.dénominateur - f2.numérateur * f1.dénominateur, f1.dénominateur * f2.dénominateur).reduce();
    }
    public static Object multiplicate(Fraction f1, Fraction f2){
        return new Fraction(f1.numérateur * f2.numérateur, f1.dénominateur * f2.dénominateur).reduce();
    }
    public static Object divide(Fraction f1, Fraction f2){
        return new Fraction(f1.numérateur * f2.dénominateur, f1.dénominateur * f2.numérateur).reduce();
    }
    private Object reduce(){
        int pgcd = PGCD(numérateur,dénominateur);
        if(dénominateur/pgcd == 1) return numérateur/pgcd;
        return new Fraction(numérateur/pgcd,dénominateur/pgcd);
    }
    private int PGCD(int n1, int n2) {
        if (n2 == 0) {
            return n1;
        }
        return PGCD(n2, n1 % n2);
    }

    public String toString(){
        return numérateur+"/"+dénominateur;
    }
}