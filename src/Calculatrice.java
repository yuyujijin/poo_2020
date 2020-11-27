import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public final class Calculatrice {
	private final Scanner useVal = new Scanner(System.in);
	private String strVal = "";
	private Stack<Object> stack = new Stack<> ();
	private Map<String,Map <Signature, Operation>> dico = new HashMap<>();


	public Calculatrice(){
		addOperations();
		parse();
	}

	private static String withoutSpaces(String s){
		if(s == null) return "";
		return s.replaceAll("\\s+","");
	}

	private Map.Entry<Signature,Operation> getCorrespondingOperation(Map<Signature,Operation> m){
		for(Map.Entry<Signature,Operation> s : m.entrySet()){
			Signature sig = s.getKey();
			if(sig.nbArgs > stack.size()) continue;
			boolean valid = true;
			for(int i = 0; i < sig.nbArgs; i++) if(stack.get(stack.size() - 1 - i).getClass() != sig.getTypeArgs().get(i)){ valid = false; break; }
			if(valid) return s;
		}
		return null;
	}

	private void addOperations(){
		String[] operations = new String[]{"+", "-","*","/","AND","OR","NOT"};
		for(String s : operations) dico.put(s, new HashMap<>());
		dico.get("+").put(new Signature(2,List.of(Integer.class,Integer.class)), args -> (Integer) args[0] + (Integer) args[1]);
		dico.get("-").put(new Signature(2,List.of(Integer.class,Integer.class)), args -> (Integer) args[0] - (Integer) args[1]);
		dico.get("*").put(new Signature(2,List.of(Integer.class,Integer.class)), args -> (Integer) args[0] * (Integer) args[1]);
		dico.get("/").put(new Signature(2,List.of(Integer.class,Integer.class)), args -> (Integer) args[0] / (Integer) args[1]);

		dico.get("AND").put(new Signature(2,List.of(Boolean.class,Boolean.class)), args -> (Boolean) args[0] && (Boolean) args[1]);
		dico.get("OR").put(new Signature(2,List.of(Boolean.class,Boolean.class)), args -> (Boolean) args[0] || (Boolean) args[1]);
		dico.get("NOT").put(new Signature(1,List.of(Boolean.class)), args -> !(Boolean) args[0]);
	}

	private Object[] toArrayInRange(int n){
		Object[] o = new Object[n];
		for(int i = 0 ; i < n; i++) o[i] = stack.pop();
		return o;
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
				Map m = dico.get(s.toUpperCase());
				if(m != null){
					Map.Entry<Signature,Operation> o = getCorrespondingOperation(m);
					Object obj = null;
					if(o != null) obj = o.getValue().compute(toArrayInRange(o.getKey().nbArgs));
					if(obj != null) { stack.push(obj); continue; }
				}
				stack.push(TypeParser.parse(s));
			}
		}
	}

	public static void main(String[] args) {
		Calculatrice c = new Calculatrice();
	}

	private final static class Signature{
		private int nbArgs;
		private List<Class> typeArgs;

		public Signature(int n, List<Class> t){
			nbArgs = n; typeArgs = new ArrayList<>(t);
		}

		public int getNbArgs(){ return nbArgs; }
		public List<Class> getTypeArgs(){ return new ArrayList<>(typeArgs); }
	}

	private final static class TypeParser{
		private TypeParser(){}

		public static Object parse(String s){
			Object o;
			if((o = parseInt(s)) != null) return o;
			if((o = parseBool(s)) != null) return o;
			throw new IllegalArgumentException("Type de l'argument '"+s+"' inconnue.");
		}

		private static Integer parseInt(String s){
			try{
				return Integer.parseInt(s);
			}catch(Exception e){
				return null;
			}
		}

		private static Boolean parseBool(String s){
			if(s.toLowerCase().equals("true")) return true;
			if(s.toLowerCase().equals("false")) return false;
			return null;
		}
	}
}
