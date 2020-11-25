import java.util.*;
import java.util.stream.Collectors;

public final class Calculatrice {
	private final Scanner useVal = new Scanner(System.in);
	private String strVal = "";
	private Stack<Operande> stack = new Stack<> ();
	private final Dictionnaire dico = new Dictionnaire();

	public Calculatrice(){
		addOperations();
		parse();
	}

	private static String withoutSpaces(String s){
		if(s == null) return "";
		return s.replaceAll("\\s+","");
	}

	private void addOperations(){
		dico.put("+",Integer.class,2, (x, y) -> (Integer) x.getValue() + (Integer) y.getValue());
		dico.put("-",Integer.class,2, (x, y) -> (Integer) x.getValue() - (Integer) y.getValue());
		dico.put("*",Integer.class,2, (x, y) -> (Integer) x.getValue() * (Integer) y.getValue());
		dico.put("/",Integer.class,2, (x, y) -> (Integer) x.getValue() / (Integer) y.getValue());
		dico.put("AND",Boolean.class,2, (x, y) -> (Boolean) x.getValue() && (Boolean) y.getValue());
		dico.put("OR",Boolean.class,2, (x, y) -> (Boolean) x.getValue() || (Boolean) y.getValue());
		dico.put("+",Fraction.class,2, (x, y) -> Fraction.sum((Fraction) x.getValue(), (Fraction) y.getValue()));
		dico.put("-",Fraction.class,2, (x, y) -> Fraction.substract((Fraction) x.getValue(), (Fraction) y.getValue()));
		dico.put("*",Fraction.class,2, (x, y) -> Fraction.multiplicate((Fraction) x.getValue(), (Fraction) y.getValue()));
		dico.put("/",Fraction.class,2, (x, y) -> Fraction.divide((Fraction) x.getValue(), (Fraction) y.getValue()));
	}

	private void parse(){
		while (!strVal.equals("=")) {
			if (stack.size() > 0) System.out.println(stack.peek());

			System.out.print(">");
			strVal = useVal.nextLine();

			//si la valeur est nulle
			if (withoutSpaces(strVal).length() <= 0) continue;

			Stack<String> mots = new Stack<>();
			mots.addAll(Arrays.asList(strVal.trim().split("[ \t]+")));

			for (String s : mots) {
				Pair<Signature, Operation> pair = null;
				if (stack.size() > 0) pair = dico.getPair(s.toUpperCase(), stack.peek().getType());

				if (pair == null) {
					Operande c = Operande.createOperande(s);
					if (c != null) {
						stack.push(c);
						continue;
					}
					throw new IllegalArgumentException("Type d'opérande inconnue.");
				}

				if (pair.key.arite > stack.size())
					throw new IndexOutOfBoundsException("Pas assez d'élements dans la pile. (operation d'aritée " + 0
							+ " avec seulement " + stack.size() + " élements.s dans la pile)");
				for (int i = 0; i < pair.key.arite; i++) {
					if (stack.get(stack.size() - 1 - i).getType() != pair.key.cls)
						throw new IllegalArgumentException("L'un des arguments n'est pas du bon type.");
				}
				Operation op = pair.value;
				Operande res = Operande.createOperande(op.operate(stack.pop(), stack.pop()).toString());
				stack.push(res);
			}
		}
	}

	public static void main(String[] args) {
		Calculatrice c = new Calculatrice();
	}

	private final static class Dictionnaire{
		private static Map<Signature,Operation> dico;

		public Dictionnaire(){
			dico = new HashMap<>();
		}

		public Operation getOperation(String op, Class c){
			return dico.get(getSignature(op,c));
		}

		public Signature getSignature(String s, Class c){
			List l = dico.keySet().stream().filter(sig -> sig.ope.equals(s) && sig.cls == c).collect(Collectors.toList());
			if(l.size() > 0) return (Signature) l.get(0);
			return null;
		}

		public Pair getPair(String s, Class c){
			Signature sig = getSignature(s,c);
			if(sig == null) return null;
			return new Pair(sig,dico.get(sig));
		}

		public void put(String s, Class c, int i, Operation o){
			Signature sig = new Signature(s,c,i);
			dico.put(sig,o);
		}
	}

	private final static class Signature{
		private String ope;
		private Class cls;
		private int arite;

		public Signature(String s, Class c, int i){
			ope = s; cls = c; arite = i;
		}
	}

	private final static class Pair<K,V>{
		private final K key;
		private final V value;

		public Pair(K k, V v){
			key = k; value = v;
		}

		public String toString(){
			return key.toString()+" _ "+value.toString();
		}
	}
}
