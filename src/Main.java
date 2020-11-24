import java.util.*;
import java.util.stream.Collectors;

class Main {
	static Scanner useVal = new Scanner(System.in);
	static String strVal = "";
	static Stack<Operande> stack = new Stack<> ();
	static Dictionnaire dico = new Dictionnaire();

	static boolean isNull(String val) {
		if(val.length() <= 0) return true;
		return false;
	}

	private static String withoutSpaces(String s){
		if(s == null) return "";
		return s.replaceAll("\\s+","");
	}

	private static void addOperations(){
		dico.put("+",Integer.class,2, (x, y) -> (Integer) x.getValue() + (Integer) y.getValue());
		dico.put("-",Integer.class,2, (x, y) -> (Integer) x.getValue() - (Integer) y.getValue());
		dico.put("*",Integer.class,2, (x, y) -> (Integer) x.getValue() * (Integer) y.getValue());
		dico.put("/",Integer.class,2, (x, y) -> (Integer) x.getValue() / (Integer) y.getValue());
		dico.put("AND",Boolean.class,2, (x, y) -> (Boolean) x.getValue() && (Boolean) y.getValue());
		dico.put("OR",Boolean.class,2, (x, y) -> (Boolean) x.getValue() || (Boolean) y.getValue());
	}

	public static void main(String[] args) {
		addOperations();

		while (!strVal.equals("=")) {
			if(stack.size() > 0) System.out.println(stack.peek());

			System.out.print(">");
			strVal = withoutSpaces(useVal.nextLine());

			//si la valeur est nulle
			if (isNull(strVal)) continue;

			Pair<Signature,Operation> pair = null;
			if(stack.size() > 0) pair = dico.getPair(strVal.toUpperCase(),stack.peek().getType());

			if(pair == null){
				Operande c = Operande.createOperande(strVal);
				if(c != null){ stack.add(c); continue; }
				throw new IllegalArgumentException("Type d'opérande inconnue.");
			}

			if(pair.key.arite > stack.size()) throw new IndexOutOfBoundsException("Pas assez d'élements dans la pile. (operation d'aritée "+0
					+" avec seulement "+stack.size()+ " élements.s dans la pile)");
			for(int i = 0; i < pair.key.arite; i++)
				if(stack.get(i).getType() != pair.key.cls) throw new IllegalArgumentException("L'un des arguments n'est pas du bon type.");

			Operation op = pair.value;
			Operande res = Operande.createOperande(op.operate(stack.pop(),stack.pop()).toString());
			stack.push(res);
		}
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
