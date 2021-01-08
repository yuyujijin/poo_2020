import java.util.*;
import java.util.concurrent.Flow;

public final class Calculatrice {
	// La pile
	private Stack<Object> stack = new Stack<>();
	// L'historique, chaque Token representant une cellule
	private List<Token> histo = new Stack<> ();
	// Le stockage des variables, dans une Map, clé "nom" valeur "token de rappel"
	private Map <String, Token.RecallToken> variables = new HashMap<>();
	// Le dictionnaire des opérations disponibles
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

	public Object[] stackToArray(){ return stack.toArray(new Object[stack.size()]); }
	public Token[] histToArray(){ return histo.toArray(new Token[histo.size()]); }
	public Map.Entry<String, Token.RecallToken>[] varToArray(){
		return variables.entrySet().toArray(new Map.Entry[variables.size()]); }

	/**
	 * retourn un string sans les espaces
	 * @param s une chaîne de charactere
	 * @return s sans ses espaces
	 */
	private static String withoutSpaces(String s){
		if(s == null) return "";
		return s.replaceAll("\\s+","");
	}

	/**
	 * permet de trouver une opération effectuable sur une collection d'éléments
	 * @param m Une map de signature et operations
	 * @param c Une collection d'objets
	 * @return un optionnel de couple signature/opérations, avec opérations satisfaisant tout les types de c
	 */
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
		throw new IllegalArgumentException("Opération impossible sur les éléments présent dans la pile");
	}

	/**
	 * permet de calculer une opération depuis la pile avec a partir d'un String
	 * @param s String représentant le nom d'une opération
	 * @return un optionnel de couple object/operationToken, (opération de nom s)
	 */
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
					List<Token> t = new ArrayList<>(histo.subList(histo.size() - sigop.getKey().getNbArgs(),
							histo.size()));
					Collections.reverse(t);
					// Puis on passe a l'input suivant
					return Optional.of(Map.entry(obj,new Token.OperationToken(t.toArray(new Token[sigop.getKey().getNbArgs()]),sigop.getValue(),s)));
				}
			}

		}
		return Optional.empty();
	}

	/**
	 * ajoute toutes les opérations disponible à la map d'opérations
	 */
	private void addOperations(){
		String[] operations = new String[]{"+", "-","*","/","AND","OR","NOT","UNION","INTER","CONTAINS","EQUALS"};
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
		dico.get("-").put(new Signature(List.of(Fraction.class,Fraction.class)),
				args -> Fraction.substract((Fraction) args[0],(Fraction) args[1]));
		dico.get("/").put(new Signature(List.of(Fraction.class,Fraction.class)),
				args -> Fraction.divide((Fraction) args[0],(Fraction) args[1]));
		dico.get("*").put(new Signature(List.of(Fraction.class,Fraction.class)),
				args -> Fraction.multiplicate((Fraction) args[0],(Fraction) args[1]));

		// opérations usuelles sur les ensembles
		dico.get("UNION").put(new Signature(List.of(Ensemble.class,Ensemble.class)),
				args -> Ensemble.union((Ensemble) args[0],(Ensemble) args[1]));
		dico.get("INTER").put(new Signature(List.of(Ensemble.class,Ensemble.class)),
				args -> Ensemble.inter((Ensemble) args[0],(Ensemble) args[1]));
		dico.get("CONTAINS").put(new Signature(List.of(Ensemble.class,Object.class)),
				args -> Ensemble.contains((Ensemble) args[0],args[1]));
		dico.get("EQUALS").put(new Signature(List.of(Ensemble.class,Ensemble.class)),
				args -> Ensemble.equals((Ensemble) args[0],(Ensemble) args[1]));

		// addition entre des entiers et des fractions
		dico.get("+").put(new Signature(List.of(Fraction.class,Integer.class)),
				args -> Fraction.sum((Fraction) args[0],(Integer) args[1]));
		dico.get("+").put(new Signature(List.of(Integer.class,Fraction.class)),
				args -> Fraction.sum((Integer) args[0],(Fraction) args[1]));
		// soustraction entre des entiers et des fractions
		dico.get("-").put(new Signature(List.of(Fraction.class,Integer.class)),
				args -> Fraction.substract((Fraction) args[0],(Integer) args[1]));
		dico.get("-").put(new Signature(List.of(Integer.class,Fraction.class)),
				args -> Fraction.substract((Integer) args[0],(Fraction) args[1]));
		// soustraction entre des entiers et des fractions
		dico.get("/").put(new Signature(List.of(Fraction.class,Integer.class)),
				args -> Fraction.divide((Fraction) args[0],(Integer) args[1]));
		dico.get("/").put(new Signature(List.of(Integer.class,Fraction.class)),
				args -> Fraction.divide((Integer) args[0],(Fraction) args[1]));
		// soustraction entre des entiers et des fractions
		dico.get("*").put(new Signature(List.of(Fraction.class,Integer.class)),
				args -> Fraction.multiplicate((Fraction) args[0],(Integer) args[1]));
		dico.get("*").put(new Signature(List.of(Integer.class,Fraction.class)),
				args -> Fraction.multiplicate((Integer) args[0],(Fraction) args[1]));
	}

	/**
	 * modifie la valeur d'historique d'index i, en essayant de créer un objet ou une opération à partir de s
	 * l'ancien token d'index i est déconnectée de son input et ses outputs, et ses outputs sont donnés au nouveau token
	 * si le nouveau token est une opération, il est reconnecté aux token précedents
	 * @param i l'index de la valeur d'historique
	 * @param s le String à parser pour changer la valeur
	 */
	public void updateValue(int i, String s) throws IndexOutOfBoundsException, IllegalArgumentException{
		if(s == null || s.length() == 0)
			throw new IndexOutOfBoundsException("Index "+i+" pour un historique de taille "+s.length());

		Map m = dico.get(s.toUpperCase());
		if (m != null) {
			Optional<Map.Entry<Signature, Operation>> operation = getCorrespondingOperation(m,
					new ArrayList<>(histo).stream().map(x -> x.getValue()).limit(i).toArray());
			// on compute si possible
			if (operation.isPresent()) {
				Map.Entry<Signature, Operation> sigop = operation.get();
				// On récupère les n inputs, et on créer un nouveau OperationToken avec ces derniers
				// qu'on ajoute a l'historique
				// On récupère l'ancien output (qu'on a deconnecté de la case actuel)
				Optional<List<Flow.Subscriber>> sub = histo.get(i).delete();
				List<Token> t = new ArrayList<Token>(histo.subList(i - sigop.getKey().getNbArgs(),
						i));
				Collections.reverse(t);
				// On remplace la case par la nouvelle
				histo.set(i,new Token.OperationToken(t.toArray(new Token[sigop.getKey().getNbArgs()]),sigop.getValue(),s));
				// Et si il y avait un output, on rebranche les 2 :)
				if(sub.isPresent()) histo.get(i).subscribe(sub.get());
				return;
			}
		}
		// Sinon en tente de parser la valeur
		Optional<Object> opt = TypeParser.parse(s);
		if(opt.isPresent()){
			if(opt.get().getClass() != histo.get(i).getValue().getClass()) return;
			Optional<List<Flow.Subscriber>> sub = histo.get(i).delete();
			histo.set(i,new Token.OperandToken(opt.get()));
			if(sub.isPresent()) histo.get(i).subscribe(sub.get());
			return;
		}
		throw new IllegalArgumentException(s);
	}

	/**
	 * permet de modifier une variable de nom s avec la nouvelle valeur v (sous forme de String)
	 * @param s le nom de la variable
	 * @param v le String à parser pour obtenir la valeur de remplacement
	 */
	public void updateVar(String s, String v){
		Optional<Object> opt = TypeParser.parse(v);
		if(opt.isPresent()){
			if(opt.get().getClass() != variables.get(s).getValue().getClass()) return;
			variables.get(s).cancelSubscription();
			variables.get(s).updateValue(opt.get());
		}
	}

	/**
	 * permet de modifier une variable de nom s avec la nouvelle valeur v
	 * @param s le nom de la variable
	 * @param o la valeur de remplacement
	 */
	public void updateVar(String s, Object o){
		if(o.getClass() != variables.get(s).getValue().getClass()) return;
		variables.get(s).cancelSubscription();
		variables.get(s).updateValue(o);
	}

	/**
	 * pop n fois la pile et retourne un tableau contenant les valeurs
	 * @param n nombre de fois ou l'on pop la pile
	 * @return un tableau d'objet contenant les n 'sommets' de la pile
	 */
	private Object[] toArrayInRange(int n){
		if(n > stack.size()) throw new IndexOutOfBoundsException("Taille "+n+" pour un historique de taille "+stack.size());
		Object[] o = new Object[n];
		for(int i = 0 ; i < n; i++) o[i] = stack.pop();
		return o;
	}

	/**
	 * ajoute un String donné a la pile
	 * @param phrase un string (peut être une phrase de mot séparés par un espace)
	 */
	public void addStringToStack(String phrase) throws IllegalArgumentException, IndexOutOfBoundsException, EmptyStackException{
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
				// si le mot commence par '!', on stock une variable avec en nom le reste du mot (sans le '!')
				if (s.charAt(0) == '!') {
					if(stack.size() <= 0)
						throw new EmptyStackException();
					String sub = s.substring(1);
					if(variables.get(sub) != null){
						if(stack.peek().getClass() == variables.get(sub).getValue().getClass())
							updateVar(sub, stack.pop());
						continue;
					}
					stack.pop();
					variables.put(sub, new Token.RecallToken(histo.get(histo.size() - 1),sub));
					continue;
				}
				// si le mot commence par '?', on empile le valeur de la variable stockée
				if (s.charAt(0) == '?') {
					String sub = s.substring(1);
					Token x = variables.get(sub);
					if(x == null) throw new NullPointerException("'"+sub+"' non présent dans la liste des variables");
					Token recall = new Token.RecallToken(x,sub);
					stack.push(recall.getValue());
					histo.add(recall);
					continue;
				}
				// si on execute la commande 'hist', obj prend la valeur de l'historique demandée
				if (s.length() > 4 && s.substring(0, 4).equals("hist")) {
					int i = Integer.valueOf(s.substring(s.indexOf("(") + 1, s.indexOf(")")));
					// On créer un token de rappel pointant sur la case dans l'historique
					Token recall = new Token.RecallToken(histo.get((i >= 0) ? i : histo.size() + i),s);
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
			}
		}

	/**
	 * lit dans l'entrée standard grâce a un scanner et parser chaque entrée en utilisant addStringToStack()
	 */
	private void parse(){
		String strVal = "";
		Scanner useVal = new Scanner(System.in);
		while (!strVal.equals("=")) {
			if(stack.size() > 0)
				for(int i = 0; i < stack.size(); i++){
					System.out.print(stack.get(i)+"\t");
			}
			System.out.println();

			System.out.print(">");
			strVal = useVal.nextLine();

			//si la valeur est nulle
			try {
				addStringToStack(strVal);
			}catch(Exception e){
				System.out.println(e);
			}
		}
	}

	/**
	 * Permet de représenter une signature (list de types)
	 */
	private final static class Signature{
		private List<Class> typeArgs;

		public Signature(List<Class> t){
			typeArgs = new ArrayList<>(t);
		}

		public int getNbArgs(){ return typeArgs.size(); }
		public List<Class> getTypeArgs(){ return new ArrayList<>(typeArgs); }
	}

	/**
	 * Classe possédant une unique fonction publique permettant de parser un String dans les types disponibles
	 */
	private final static class TypeParser{
		private TypeParser(){}

		public static Optional<Object> parse(String s){
			Object o;
			if((o = parseInt(s)) != null) return Optional.of(o);
			if((o = parseBool(s)) != null) return Optional.of(o);
			if((o = parseFrac(s)) != null) return Optional.of(o);
			if((o = parseEnsemble(s)) != null) return Optional.of(o);
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

		private static Ensemble parseEnsemble(String s){
			if(s.charAt(0) == '{' && s.charAt(s.length() - 1) == '}'){
				try{
					List<String> mots = new ArrayList<>();
					mots.addAll(Arrays.asList(s.substring(1,s.length() - 1).trim().split(";")));
					List<Object> elems = new ArrayList<>();
					for(String x : mots){
						Optional<Object> o = parse(x);
						if(!o.isPresent()) return null;
						elems.add(o.get());
					}
					return new Ensemble(elems);
				}catch(Exception e){

				}
			}
			return null;
		}
	}
}
