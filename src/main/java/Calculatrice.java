import java.util.*;
import java.util.concurrent.Flow;

public final class Calculatrice {
	private final Scanner useVal = new Scanner(System.in);
	private String strVal = "";
	// Les objets de stack et histo devrait implementer clonnable...
	private Stack<Object> stack = new Stack<>();
	private List<Token> histo = new Stack<> ();
	private Map <String, Token.RecallToken> variables = new HashMap<>();
	private Map<String,Map <Signature, Operation>> dico = new HashMap<>();
	private Set<Class> types;

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

	public Object[] stackToArray(){ return stack.toArray(new Object[stack.size()]); }
	public Token[] histToArray(){ return histo.toArray(new Token[histo.size()]); }
	public Set<Map.Entry<String, Token.RecallToken>> varToArray(){ return variables.entrySet(); }

	private static String withoutSpaces(String s){
		if(s == null) return "";
		return s.replaceAll("\\s+","");
	}

	private Optional<Map.Entry<Signature,Operation>> getCorrespondingOperation(Map<Signature,Operation> m, Object... c){
		for(Map.Entry<Signature,Operation> s : m.entrySet()){
			Signature sig = s.getKey();
			if(sig.getNbArgs() > c.length) continue;
			boolean valid = true;
			for(int i = 0; i < sig.getNbArgs(); i++) {
				if (!sig.getTypeArgs().get(i).isInstance(c[c.length - 1 - i])) {
					valid = false;
					break;
				}
			}
			if(valid) return Optional.of(s);
		}
		return Optional.empty();
	}

	private void addOperations(){
		types = new HashSet<>();
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
		types.add(Integer.class);

		// opérations 'et' 'ou' 'non' sur les booléens
		dico.get("AND").put(new Signature(List.of(Boolean.class,Boolean.class)),
				args -> (Boolean) args[0] && (Boolean) args[1]);
		dico.get("OR").put(new Signature(List.of(Boolean.class,Boolean.class)),
				args -> (Boolean) args[0] || (Boolean) args[1]);
		dico.get("NOT").put(new Signature(List.of(Boolean.class)),
				args -> !(Boolean) args[0]);
		types.add(Boolean.class);

		// opérations usuelles sur les fractions
		dico.get("+").put(new Signature(List.of(Fraction.class,Fraction.class)),
				args -> Fraction.sum((Fraction) args[0],(Fraction) args[1]));
		// addition entre des entiers et des fractions
		dico.get("+").put(new Signature(List.of(Fraction.class,Integer.class)),
				args -> Fraction.sum((Fraction) args[0],new Fraction((Integer) args[1],1)));
		dico.get("+").put(new Signature(List.of(Integer.class,Fraction.class)),
				args -> Fraction.sum(new Fraction((Integer) args[0],1),(Fraction) args[1]));
		types.add(Fraction.class);
	}

	public HashSet getAvailableTypes(){
		return new HashSet(types);
	}

	public void updateValue(int i, String s){
		// TODO NULL POINTER LORS DE LA MODIFICATION DE VALEUR
		if(s == null || s.length() == 0)
			throw new IndexOutOfBoundsException();

		Map m = dico.get(s.toUpperCase());
		if (m != null) {
			Optional<Map.Entry<Signature, Operation>> operation = getCorrespondingOperation(m,
					new ArrayList<Token>(histo).stream().map(x -> x.getValue()).limit(i).toArray());
			// on compute si possible
			if (operation.isPresent()) {
				Map.Entry<Signature, Operation> sigop = operation.get();
				// Si l'opération n'a pas retourné 'null'
				// On récupère les n inputs, et on créer un nouveau OperationToken avec ces derniers
				// qu'on ajoute a l'historique
				Optional<Flow.Subscriber> sub = histo.get(i).delete();
				Token[] t = histo.subList(i - sigop.getKey().getNbArgs(),
						i).toArray(new Token[sigop.getKey().getNbArgs()]);
				histo.set(i,new Token.OperationToken(t,sigop.getValue(),s));
				if(sub.isPresent()) histo.get(i).subscribe(sub.get());
				return;
			}
		}
		Optional<Object> opt = TypeParser.parse(s);
		if(opt.isPresent()){
			Optional<Flow.Subscriber> sub = histo.get(i).delete();
			histo.set(i,new Token.OperandToken(opt.get()));
			if(sub.isPresent()) histo.get(i).subscribe(sub.get());
			return;
		}
	}

	private Object[] toArrayInRange(int n){
		Object[] o = new Object[n];
		for(int i = 0 ; i < n; i++) o[i] = stack.pop();
		return o;
	}

	public void addStringToStack(String phrase) throws InterruptedException{
		if (withoutSpaces(phrase).length() <= 0) return;

		Stack<String> mots = new Stack<>();
		mots.addAll(Arrays.asList(phrase.trim().split("[ \t]+")));

		//pour chaque mot dans la ligne que l'on vient de rentrer
		for (String s : mots) {
			s = s.trim();
			//on récupère l'opération correspondante (si elle existe)
			Optional<Map.Entry<Object,Token.OperationToken>> opeOpt = getOperationFromString(s);
			if(opeOpt.isPresent()){
				Map.Entry<Object,Token.OperationToken> var = opeOpt.get();
				stack.push(var.getKey()); histo.add(var.getValue());
				continue;
			}
			// dans le cas ou on le mot ne correspond a aucune opération connue
			try {
				// si le mot commence par '!', on stock une variable avec en nom le reste du mot (sans le '!')
				if (s.charAt(0) == '!') {
					String sub = s.substring(1);
					stack.pop();
					variables.put(sub, new Token.RecallToken(histo.get(histo.size() - 1)));
					continue;
				}
				// si le mot commence par '?', on empile le valeur de la variable stockée
				if (s.charAt(0) == '?') {
					String sub = s.substring(1);
					stack.push(variables.get(sub).getValue());
					continue;
				}
				// si on execute la commande 'hist', obj prend la valeur de l'historique demandée
				if (s.length() > 4 && s.substring(0, 4).equals("hist")) {
					int i = Integer.valueOf(s.substring(s.indexOf("(") + 1, s.indexOf(")")));
					// On créer un token de rappel pointant sur la case dans l'historique
					Token recall = new Token.RecallToken(histo.get((i >= 0) ? i : histo.size() + i));
					stack.push(recall.getValue());
					histo.add(recall);
					// 'pile', obj prend la valeur de la pile demandée
				} else if (s.length() > 4 && s.contains("pile")) {
					int i = Integer.valueOf(s.substring(s.indexOf("(") + 1, s.indexOf(")")));
					// On créer une 'copie' de l'opérande (une opérande sans inputs)
					Token op = new Token.OperandToken(stack.get((i >= 0) ? i : stack.size() + i));
					stack.push(op.getValue());
					histo.add(op);
					// sinon, on tente de parser le mot lu
				} else {
					Optional<Object> opt = TypeParser.parse(s);
					if (opt.isPresent()){
						stack.push(opt.get());
						histo.add(new Token.OperandToken(opt.get()));
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private Optional<Map.Entry<Object,Token.OperationToken>> getOperationFromString(String s){
		Map m = dico.get(s.toUpperCase());
		if (m != null) {
			Optional<Map.Entry<Signature, Operation>> operation = getCorrespondingOperation(m,stack.toArray());
			// on compute si possible
			if (operation.isPresent()) {
				Map.Entry<Signature, Operation> sigop = operation.get();
				Object obj = sigop.getValue().compute(toArrayInRange(sigop.getKey().getNbArgs()));
				// Si l'opération n'a pas retourné 'null'
				if (obj != null) {
					// On récupère les n inputs, et on créer un nouveau OperationToken avec ces derniers
					// qu'on ajoute a l'historique
					Token[] t = histo.subList(histo.size() - sigop.getKey().getNbArgs(),
							histo.size()).toArray(new Token[sigop.getKey().getNbArgs()]);
					// Puis on passe a l'input suivant
					return Optional.of(Map.entry(obj,new Token.OperationToken(t,sigop.getValue(),s)));
				}
			}

		}
		return Optional.empty();
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
			addStringToStack(strVal);
		}
	}

	public static void main(String[] args) {
		Calculatrice c = new Calculatrice();
		c.start();
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

		public static Optional<Object> parse(String s){
			Object o;
			if((o = parseInt(s)) != null) return Optional.of(o);
			if((o = parseBool(s)) != null) return Optional.of(o);
			if((o = parseFrac(s)) != null) return Optional.of(o);
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
