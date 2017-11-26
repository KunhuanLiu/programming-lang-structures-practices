import java.util.ArrayList;

public class TokenType {
	private ArrayList<ArrayList<TokenType>> grammar_ruleS;
	public final static int TERMINAL = 0;
	public final static int NONTERMINAL = 1;
	private final String name;
	private final int term;
	private boolean optional; // if the grammar symbol can be omitted

	public TokenType(String name, int term) {
		// TODO Auto-generated constructor stub
		this(name,term,false);
	}
	
	public TokenType(String name, int term, boolean optional) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.term = term;
		this.grammar_ruleS = new ArrayList<ArrayList<TokenType>>();
		this.optional=optional;
	}


	public boolean isTerminal() {
		if (term == TERMINAL) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * check if the grammar symbol can be omitted.
	 * @return
	 */
	public boolean isOptional(){
		return this.optional;
	}

	public void setOptional(boolean option){
		this.optional = option;
	}
	public String get_its_name() {
		return name;
	}
	
	
	
	

	/**
	 * to define the grammar of your TokenType. You NEED TO DEFINE ALL THE
	 * GRAMMAR SYMMBOLS FIRST AND <b> then write the grammar </b>
	 * 
	 * @param an
	 *            array of tokentype which includes grammar symbols as objects; <br>
	 *            for example, your var tokentype will need an array of {;, var,
	 *            tail}--all addresses to the tokentypes <br>
	 *            rule: (var-->; var tail)
	 * @return how many grammar rules are there for this grammar symbol.
	 */
	public int addGrammar(TokenType[] arrStr) {
		if (this.isTerminal()) {
			System.out.println("a terminal cannot have grammar rules. type:"+get_its_name());
			System.exit(1);
		}
		ArrayList<TokenType> a_rule = new ArrayList<>();
		for (TokenType s : arrStr) {
			a_rule.add(s);
		}
		this.grammar_ruleS.add(a_rule);
		return this.grammar_ruleS.size();
	}

	/**
	 * @return it gives back an array of TokenType which contains the
	 *         appropriate grammar rule for future use.
	 *         An optional tokentype will return an empty list, remember.
	 * @return <b>It will return NULL if it doesn't match</b>
	 * @param TokenType
	 *            . You will need to map String value to TokenType before
	 *            passing the input.
	 */
	public TokenType[] doesItMatch(TokenType input) {
		for (ArrayList<TokenType> a_rule : this.grammar_ruleS) {
			TokenType toCompare = a_rule.get(0); // get the first one
			if (toCompare.isTerminal()) { // when the first token in the rule is
											// a terminal, BASE CASE
				System.out.println("terminal detected.");
				System.out.println("input is"+input.toString());
				if (toCompare.name.equals(input.name)) {
					TokenType[] arr = new TokenType[a_rule.size()];
					return a_rule.toArray(arr);
				}
				else if (this.isOptional()){
					return new TokenType[0]; // an empty array of length 0
				}
			} else { // if the first token in the rule is not a terminal, we
						// need to pass down till the terminal
				System.out.println("test type <"+this.get_its_name()+"> first token's is "+toCompare.get_its_name());
				TokenType[] verify = toCompare.doesItMatch(input);
				if ((verify == null) && this.isOptional()) { 
					//optional!
					return new TokenType[0];
				}
				else if (verify != null){
					// when the following structure finds a
					// legit solution, we find the matched
					// rule
					TokenType[] arr = new TokenType[a_rule.size()];
					return a_rule.toArray(arr);
				}
			}
		}
		//System.out.println("???????");
		return null;
	}

}
