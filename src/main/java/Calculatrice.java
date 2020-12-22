import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.concurrent.ForkJoinPool.commonPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class Calculatrice {
	private final Scanner useVal = new Scanner(System.in);
	private String strVal = "";
	// Les objets de stack et histo devrait implementer clonnable...
	private Stack<Operande> stack = new Stack<> ();
	private Stack<Operande> histo = new Stack<> ();
	private Map <String, Operande> variables = new HashMap<>();
	private Map<String,Map <Signature, Operation>> dico = new HashMap<>();

	public Calculatrice(){
		addOperations();
	}

	public void start(){
		try{
			parse();
		}catch(Exception e){
			System.out.println(e);
		}
	}

	public String[] stackToString(){
		String[] s = new String[stack.size()];
		for(int i = 0; i < stack.size(); i++) s[i] = stack.get(i).toString();
		return s;
	}

	private static String withoutSpaces(String s){
		if(s == null) return "";
		return s.replaceAll("\\s+","");
	}

	private Map.Entry<Signature,Operation> getCorrespondingOperation(Map<Signature,Operation> m){
		for(Map.Entry<Signature,Operation> s : m.entrySet()){
			Signature sig = s.getKey();
			if(sig.getNbArgs() > stack.size()) continue;
			boolean valid = true;
			for(int i = 0; i < sig.getNbArgs(); i++)
				if(!sig.getTypeArgs().get(i).isInstance(stack.get(stack.size() - 1 - i).getValue())){ valid = false; break; }
			if(valid) return s;
		}
		return null;
	}

	private void addOperations(){
		String[] operations = new String[]{"+", "-","*","/","AND","OR","NOT","^"};
		for(String s : operations) dico.put(s, new HashMap<>());
		// opérations usuelles sur les entiers
		dico.get("+").put(new Signature(List.of(Integer.class,Integer.class)),
				args -> (Integer) args[0] + (Integer) args[1]);
		dico.get("-").put(new Signature(List.of(Integer.class,Integer.class)),
				args -> (Integer) args[0] - (Integer) args[1]);
		dico.get("*").put(new Signature(List.of(Integer.class,Integer.class)),
				args -> (Integer) args[0] * (Integer) args[1]);
		dico.get("/").put(new Signature(List.of(Integer.class,Integer.class)),
				args -> (Integer) args[0] / (Integer) args[1]);

		// opérations 'et' 'ou' 'non' sur les booléens
		dico.get("AND").put(new Signature(List.of(Boolean.class,Boolean.class)),
				args -> (Boolean) args[0] && (Boolean) args[1]);
		dico.get("OR").put(new Signature(List.of(Boolean.class,Boolean.class)),
				args -> (Boolean) args[0] || (Boolean) args[1]);
		dico.get("NOT").put(new Signature(List.of(Boolean.class)),
				args -> !(Boolean) args[0]);

		// opérations usuelles sur les fractions
		dico.get("+").put(new Signature(List.of(Fraction.class,Fraction.class)),
				args -> Fraction.sum((Fraction) args[0],(Fraction) args[1]));
		// addition entre des entiers et des fractions
		dico.get("+").put(new Signature(List.of(Fraction.class,Integer.class)),
				args -> Fraction.sum((Fraction) args[0],new Fraction((Integer) args[1],1)));
		dico.get("+").put(new Signature(List.of(Integer.class,Fraction.class)),
				args -> Fraction.sum(new Fraction((Integer) args[0],1),(Fraction) args[1]));
	}

	public boolean updateValue(int i, Object o){
		if(o == null || stack.get(i).getValue().getClass() != o.getClass()) return false;
		stack.get(i).updateValue(o);
		return true;
	}

	private Operande[] toArrayInRange(int n){
		Operande[] o = new Operande[n];
		for(int i = 0 ; i < n; i++) o[i] = stack.get(stack.size() - 1 - i);
		return o;
	}

	public void addStringToStack(String s) throws InterruptedException{
		s = s.trim();
		//on récupère l'opération correspondante (si elle existe)
		Map m = dico.get(s.toUpperCase());
		if(m != null){
			Map.Entry<Signature,Operation> o = getCorrespondingOperation(m);
			Operande obj = null;
			// on compute si possible
			if(o != null) obj = new Operande.OperandeWithInputs(toArrayInRange(o.getKey().getNbArgs()),o.getValue());

			commonPool().awaitTermination(1000, MILLISECONDS);

			if(obj != null) {
				// et si la computation a été faite, on push le resultat
				stack.push(obj);
				histo.push(stack.peek());
				return; }
		}
		// dans le cas ou on le mot ne correspond a aucune opération connue
		try{
			// si le mot commence par '!', on stock une variable avec en nom le reste du mot (sans le '!')
			if(s.charAt(0) == '!'){
				String sub = s.substring(1, s.length());
				variables.put(sub, stack.pop());
				return;
			}
			// si le mot commence par '?', on empile le valeur de la variable stockée
			if(s.charAt(0) == '?'){
				String sub = s.substring(1, s.length());
				stack.push(variables.get(sub));
				return;
			}
			Operande obj = null;
			// si on execute la commande 'hist', obj prend la valeur de l'historique demandée
			if(s.length() > 4 && s.substring(0,4).equals("hist")){
				int i = Integer.valueOf(s.substring(s.indexOf("(") + 1, s.indexOf(")")));
				obj = histo.get((i >= 0)? i : histo.size() + i);
				// 'pile', obj prend la valeur de la pile demandée
			}else if(s.length() > 4 && s.contains("pile")){
				int i = Integer.valueOf(s.substring(s.indexOf("(") + 1, s.indexOf(")")));
				obj = stack.get((i >= 0)? i : stack.size() + i);
				// sinon, on tente de parser le mot lu
			}else if(s.length() > 6 && s.contains("update")){
				int i = Integer.valueOf(s.substring(s.indexOf("(") + 1, s.indexOf(",")));
				Operande objet = TypeParser.parse(s.substring(s.indexOf(",") + 1, s.indexOf(")")));
				updateValue((i >= 0)? i : stack.size() + i,objet);

				commonPool().awaitTermination(1000, MILLISECONDS);
			}else{
				obj = TypeParser.parse(s);
			}
			// enfin, si on a pu récuperer un objet, on le push dans la pile et dans l'historique (clone...?)
			if(obj != null) stack.push(obj);
			histo.push(stack.peek());
		}catch(Exception e){
			System.out.println(e);
		}
	}

	private void parse() throws InterruptedException {
		while (!strVal.equals("=")) {
			//if (stack.size() > 0) System.out.println(stack.peek());
			if(stack.size() > 0)
				for(int i = 0; i < stack.size(); i++){
					System.out.print(stack.get(i)+"\t");
			}
			System.out.println();

			System.out.print(">");
			strVal = useVal.nextLine();

			//si la valeur est nulle
			if (withoutSpaces(strVal).length() <= 0) continue;

			Stack<String> mots = new Stack<>();
			mots.addAll(Arrays.asList(strVal.trim().split("[ \t]+")));

			//pour chaque mot dans la ligne que l'on vient de rentrer
			for (String s : mots) {
				addStringToStack(s);
			}
		}
	}

	public static void main(String[] args) {
		Calculatrice c = new Calculatrice();
	}

	private final static class Signature{
		private List<Class> typeArgs;

		public Signature(List<Class> t){
			typeArgs = new ArrayList<>(t);
		}

		public int getNbArgs(){ return typeArgs.size(); }
		public List<Class> getTypeArgs(){ return new ArrayList<>(typeArgs); }
	}

	public final static class TypeParser{
		private TypeParser(){}

		public static Operande parse(String s){
			Object o;
			if((o = parseInt(s)) != null) return new Operande(o);
			if((o = parseBool(s)) != null) return new Operande(o);
			if((o = parseFrac(s)) != null) return new Operande(o);
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

		private static Fraction parseFrac(String s){
			if(s.contains("/"))
				try{
					int d, n;
					n = Integer.parseInt(s.substring(0,s.indexOf("/")));
					d = Integer.parseInt(s.substring(s.indexOf("/") + 1));
					return new Fraction(n,d);
				}catch(Exception e){
				}
			return null;
		}
	}
}
