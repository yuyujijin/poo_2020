
class Operation {
	static String[] opes = { "+", "-", "*", "/" };
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

}
