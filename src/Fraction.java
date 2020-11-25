public final class Fraction{
    private final int numérateur, dénominateur;
    Fraction(int n, int d){
        if(d == 0) throw new IllegalArgumentException("Fraction avec un dénominateur a 0.");
        numérateur = n; dénominateur = d;
    }
    public static Fraction sum(Fraction f1, Fraction f2){
        return new Fraction(f1.numérateur * f2.dénominateur + f2.numérateur * f1.dénominateur, f1.dénominateur * f2.dénominateur).reduce();
    }
    public static Fraction substract(Fraction f1, Fraction f2){
        return new Fraction(f1.numérateur * f2.dénominateur - f2.numérateur * f1.dénominateur, f1.dénominateur * f2.dénominateur).reduce();
    }
    public static Fraction multiplicate(Fraction f1, Fraction f2){
        return new Fraction(f1.numérateur * f2.numérateur, f1.dénominateur * f2.dénominateur).reduce();
    }
    public static Fraction divide(Fraction f1, Fraction f2){
        return new Fraction(f1.numérateur * f2.dénominateur, f1.dénominateur * f2.numérateur).reduce();
    }
    private Fraction reduce(){
        int pgcd = PGCD(numérateur,dénominateur);
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