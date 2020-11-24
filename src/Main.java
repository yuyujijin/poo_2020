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
		dico.put(new Pair<>(new Pair<>("+",Nombre.class),2),OperationNombre.getOperationNombrePlus());
		dico.put(new Pair<>(new Pair<>("-",Nombre.class),2),OperationNombre.getOperationNombreMoins());
		dico.put(new Pair<>(new Pair<>("*",Nombre.class),2),OperationNombre.getOperationNombreMul());
		dico.put(new Pair<>(new Pair<>("/",Nombre.class),2),OperationNombre.getOperationNombreDiv());
		dico.put(new Pair<>(new Pair<>("AND",Booleen.class),2),OperationBooleen.getOperationBooleenEt());
	}

	public static void main(String[] args) {
		addOperations();
		while (!strVal.equals("=")) {
			if(stack.size() > 0) System.out.println(stack.peek());

			System.out.print(">");
			strVal = withoutSpaces(useVal.nextLine());

			//si la valeur est nulle
			if (isNull(strVal)) continue;
			/* récuperer l'opération avec le même nom et de même class que le sommet de la pile */

			Operation p = null;
			if(stack.size() > 0) {
				Class clss = stack.peek().getClass();
				p = dico.getOperation(strVal,clss);
			}

			if(p == null){
				Operande c = Operande.createOperande(strVal);
				if(c != null){ stack.add(c); continue; }
				throw new IllegalArgumentException("Type d'opérande inconnue.");
			}

			if(p.getArite() > stack.size()) throw new IndexOutOfBoundsException("Pas assez d'élements dans la pile. (operation d'aritée "+p.getArite()
					+" avec seulement "+stack.size()+ " élements.s dans la pile)");

			stack.push(p.operate(stack.pop(),stack.pop()));
		}
	}

	private final static class Dictionnaire{
		private static Map<Pair<Pair<String,Class>,Integer>,Operation> dico;

		public Dictionnaire(){
			dico = new HashMap<>();
		}

		public Operation getOperation(String op, Class c){
			List l = dico.entrySet().stream().filter(p -> p.getKey().key.key.equals(op) && p.getKey().key.value == c).map(p -> p.getValue()).collect(Collectors.toList());
			if(l.size() > 0) return (Operation) l.get(0);
			return null;
		}

		public void put(Pair<Pair<String,Class>,Integer> p, Operation o){
			dico.put(p,o);
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
