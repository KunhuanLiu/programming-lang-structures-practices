public class Token {
	private String myType;
	private String myLexeme;

	public Token(String type, String lexeme) {
		this.myLexeme = lexeme;
		this.myType = type;
	}

	public String getType() {
		return myType;
	}

	public String getLexeme() {
		return myLexeme;
	}

	public boolean equals(String obj) {
		if (this.myType.equals(obj)) {
			return true;
		} else {
			return false;
		}

	}
}
