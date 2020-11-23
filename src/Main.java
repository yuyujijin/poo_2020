import java.util.Scanner;
import java.util.Stack;

class Main {
	static Scanner useVal = new Scanner(System.in);
	static String strVal = "";
	static Stack<Integer> stack = new Stack<Integer> ();
	static String[] opes = { "+", "-", "*", "/" };
	
	/*
	 * on cherche à savoir si la chaine est nulle
	 * ou si elle ne contient que des espaces
	 * et on enleve les espaces superflus
	 * ex : pour "   5    4  " on aura : "54"
	 */
	static boolean isNull(String val) {
		//la valeur n'est pas nulle :
		if(val == null) return true;
		
		int len = val.length();
		boolean onlySpaces = true;

		for (int i = 0; i < len; i++) {
			if (val.charAt(i) == ' ')
				val = val.substring(0,i) + val.substring(i+1);
			else onlySpaces=false;
		}
		//System.out.println('5'+strVal);
		//System.out.println(!onlySpaces);
		return onlySpaces;

	}
	
	/*
	 * retourne l'operateur, si val en est un sinon null
	 */
	static String isOperation(String val) {
		int len = opes.length;

		for (int i = 0; i < len; i++) {
			if (val.equals(opes[i]))
				return opes[i];
		}
		return null;
	}
	
	static Integer makeOpe(Integer term1, Integer term2, String ope) {
		switch (ope) {
			case "+": return term1 + term2;
			case "-": return term1 - term2;
			case "*": return term1 * term2;
			case "/": return term1 / term2;
		}
		return null;
	}

	public static void main(String[] args) {	
		while (!strVal.equals("=")) {
			
			System.out.print(">");
			strVal = useVal.nextLine();
			//System.out.println('1'+strVal);

			//si la valeur n'est pas nulle
			if (! isNull(strVal)) {
				//System.out.println('2'+strVal);
				//si c'est une valeur
				if(isOperation(strVal) == null) {
					System.out.println(strVal);
					stack.push(Integer.valueOf(strVal));
				} else{ 
					
					stack.push(makeOpe
							(stack.pop(), stack.pop(), strVal));
					System.out.println(stack.peek());
				}
			}
		}
	}
}
