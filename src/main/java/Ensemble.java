import java.util.*;

public final class Ensemble<T>{
    private Set<T> elements;
    private static final char EMPTY = '∅';

    public Ensemble(List<T> l){
        elements = new HashSet<>(l);
    }

    public Ensemble(){
        elements = new HashSet<>();
    }

    public static <T> Ensemble of(T... t){
        return new Ensemble(List.of(t));
    }

    public void add(T elem){
        elements.add(elem);
    }

    public static final Ensemble union(Ensemble e1, Ensemble e2){
        Ensemble e = new Ensemble();
        e1.elements.stream().forEach(x -> e.add(x));
        e2.elements.stream().forEach(x -> e.add(x));
        return e;
    }

    public static final Ensemble product(Ensemble e1, Ensemble e2){
        Ensemble e = new Ensemble();
        for(Object elem : e1.elements){
            e2.elements.stream().forEach(x -> e.add(new Ensemble(List.of(elem,x))));
        }
        return e;
    }

    public static final <T> boolean contains(Ensemble e, T elem){
        return e.elements.contains(elem);
    }

    public static final boolean equals(Ensemble e1, Ensemble e2){
        return e1.elements.equals(e2.elements);
    }

    public static final Ensemble inter(Ensemble e1, Ensemble e2){
        Ensemble e = new Ensemble();
        e1.elements.stream().filter(x -> e2.elements.contains(x)).forEach(x -> e.add(x));
        return e;
    }

    public String toString(){
        Optional<String> opt = elements.stream().map(x -> x.toString()).reduce((a, b) -> a + ";" + b);
        if(opt.isPresent()) return "{"+opt.get()+"}";
        return String.valueOf(EMPTY);
    }
}
