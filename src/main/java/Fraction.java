public final class Fraction{
    private final int numerateur, denominateur;
    Fraction(int n, int d){
        if(d == 0) throw new IllegalArgumentException("Fraction avec un denominateur a 0.");
        numerateur = n; denominateur = d;
    }
    public static Object sum(Fraction f1, Fraction f2){
        return new Fraction(f1.numerateur * f2.denominateur + f2.numerateur * f1.denominateur, f1.denominateur * f2.denominateur).reduce();
    }
    public static Object sum(Integer i, Fraction f2){
        return sum(new Fraction(i,1),f2);
    }
    public static Object sum(Fraction f1, Integer i){
        return sum(f1,new Fraction(i,1));
    }
    public static Object substract(Fraction f1, Fraction f2){
        return new Fraction(f1.numerateur * f2.denominateur - f2.numerateur * f1.denominateur, f1.denominateur * f2.denominateur).reduce();
    }
    public static Object substract(Fraction f1, Integer i){
        return substract(f1,new Fraction(i,1));
    }
    public static Object substract(Integer i, Fraction f2){
        return substract(new Fraction(i,1),f2);
    }
    public static Object multiplicate(Fraction f1, Fraction f2){
        return new Fraction(f1.numerateur * f2.numerateur, f1.denominateur * f2.denominateur).reduce();
    }
    public static Object multiplicate(Fraction f1, Integer i){
        return multiplicate(f1,new Fraction(i,1));
    }
    public static Object multiplicate(Integer i, Fraction f2){
        return multiplicate(new Fraction(i,1),f2);
    }
    public static Object divide(Fraction f1, Fraction f2){
        return new Fraction(f1.numerateur * f2.denominateur, f1.denominateur * f2.numerateur).reduce();
    }
    public static Object divide(Fraction f1, Integer i){
        return divide(f1,new Fraction(i,1));
    }
    public static Object divide(Integer i, Fraction f2){
        return divide(new Fraction(i,1),f2);
    }
    private Object reduce(){
        int pgcd = PGCD(numerateur,denominateur);
        if(denominateur/pgcd == 1) return numerateur/pgcd;
        return new Fraction(numerateur/pgcd,denominateur/pgcd);
    }
    private int PGCD(int n1, int n2) {
        if (n2 == 0) {
            return n1;
        }
        return PGCD(n2, n1 % n2);
    }

    public String toString(){
        return numerateur+"/"+denominateur;
    }
}