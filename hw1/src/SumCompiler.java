import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class SumCompiler {
	private ArrayList<Token> toklist;
	private String location;
	private String filename;
	private int pos;
	private Map<String, TokenType> tokenMap;
	private JavaWriter myWriter;
	private ArrayList<Integer> lineN;

	public SumCompiler(String location) {
		this(location, "test");
	}

	public SumCompiler(String location, String filename) {
		this.toklist = new ArrayList<Token>();
		this.lineN = new ArrayList<>();
		this.tokenMap = new HashMap<String, TokenType>();
		this.pos = 0;
		this.location = location;
		this.filename = filename;
		this.myWriter = new JavaWriter();

		// this.createMyTestGrammar(); // for lab1 case
		this.createMyGrammar();
	}

	public void compile(File aFile) {
		this.readFile(aFile); // it will call converTokens to generate TokList
		String s = this.parse("programT", false);
		myWriter.write(myWriter.format(s));

	}

	private void readFile(File aFile) {
		// begin of file-reading
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(aFile), "UTF-8"));
			// initiation of variables
			String line;
			int linenum = 0;
			int toknum = 0;
			ArrayList<String> tokens = new ArrayList<String>();
			boolean comment = false;
			String delimt = "(?<=%1$s)|(?=%1$s)";
			/*/=|<=|>=|:=
			 * +|-|*|;|()|[]|=|,|{}|<>
			 * dual meaning signs: =|/|<|>
			 * 
			 * s.split("(?<=\\+)|(?=\\+)|" + "(?<=\\-)|(?=\\-)|" +
			 */
			String q = Pattern.quote("+-*;(){}[],");
			// System.out.println("q =    "+q);
			String s01 = String.format(delimt, "[" + q + "]"); // whenever you found these, split
			String s02 = "(?<![\\Q:<>/\\E])(?=\\=)"; //whenever before = it is NOT :<>/, split in front of =
			String s03 = "(?<=\\=)"; //always split after =
			String s04 = "(?=[\\Q<>/:\\E])"; // always split before <|>|/|:
			String s05 = "(?<=[\\Q:<>/\\E])(?!\\=)"; //if NOT FOLLOWED BY =, split after <|>|/|:
			
			//String s3 = "(?=\\/)|(?<=\\/)(?!\\=)";
			//String s4 = String.format(delimt, "[\\Q:<>/\\E]\\="); //whenever you see :=|<=|>=|/=, split
			String signs = String.format("%s|%s|%s|%s|%s", s01,s02,s03,s04,s05);
			//System.out.println(signs);
			// System.exit(1);
			// reading lines
			while ((line = reader.readLine()) != null) {
				/*
				 * StringTokenizer is a legacy, discouraged-2-use class
				 * https://docs
				 * .oracle.com/javase/7/docs/api/java/util/StringTokenizer.html
				 */
				linenum += 1;
				/*
				 * if (line.matches("^\\s*\\{.*\\}\\s*$")) {// we take out
				 * comments // here // regex: ^ \s* \{ .* \} \s* $ //
				 * System.out.println(line); System.out.println("**omitted**");
				 * 
				 * this.lineN.add(toknum);
				 * System.out.printf("line number %d, lineN %d.\n"
				 * ,linenum,lineN.size()); } else {
				 */
				String[] tempStr = line.split("\\s+");

				for (String s : tempStr) {
					// split + - , ; () []
					String[] sarr = s.split(signs);

					for (String s2 : sarr) {
						if (!s2.isEmpty()) {
							if (s2.equals("{")) {
								comment = true;
								System.out
										.println("found the start of comment - "
												+ s2);
							}
							if (comment) {
								if (s2.equals("}")) {
									comment = false;
									System.out
											.println("found the end of comment");
								}
							} else {
								System.out.println("found a token = " + s2);
								tokens.add(s2);
								toknum += 1;
							}
						}

						// tokens.add(s);
					}
				}
				this.lineN.add(toknum);
				System.out.printf(
						"line number %d, the size of lineN %d.\n", linenum,
						lineN.size());
			}
			// now we have a fully built tokens, pass to compiler
			this.convertTokens(tokens); // everything converted to tokens.
			// this.printToklist(); //for debug use
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @deprecated
	 */
	private void createMyTestGrammar() {
		// this is where I write down my grammar
		this.toklist.add(new Token("int", "int"));
		this.toklist.add(new Token("id", "ii"));
		this.toklist.add(new Token("=", "="));
		this.toklist.add(new Token("num", "9"));
		this.toklist.add(new Token(",", ","));
		this.toklist.add(new Token("id", "a"));
		this.toklist.add(new Token("=", "="));
		this.toklist.add(new Token("num", "10"));
		this.toklist.add(new Token(";", ";"));

		TokenType t_list, r_int, t_var, t_tail, r_comma, t_id, r_semco, t_num, r_equal;
		// list int var tail , id ; num =
		t_list = new TokenType("list", TokenType.NONTERMINAL);
		r_int = new TokenType("int", TokenType.TERMINAL);
		t_var = new TokenType("var", TokenType.NONTERMINAL);
		t_tail = new TokenType("tail", TokenType.NONTERMINAL);
		r_comma = new TokenType(",", TokenType.TERMINAL);
		t_id = new TokenType("id", TokenType.TERMINAL);
		r_semco = new TokenType(";", TokenType.TERMINAL);
		t_num = new TokenType("num", TokenType.TERMINAL);
		r_equal = new TokenType("=", TokenType.TERMINAL);

		this.tokenMap.put("list", t_list);
		this.tokenMap.put("int", r_int);
		this.tokenMap.put("var", t_var);
		this.tokenMap.put("tail", t_tail);
		this.tokenMap.put(",", r_comma);
		this.tokenMap.put("id", t_id);
		this.tokenMap.put(";", r_semco);
		this.tokenMap.put("num", t_num);
		this.tokenMap.put("=", r_equal);

		// creating grammar for each one
		// list ->int var tail
		TokenType[] arr1 = { tokT("int"), tokT("var"), tokT("tail") };
		tokT("list").addGrammar(arr1);

		// tail ->, var tail | ;
		TokenType[] arr2 = { tokT(","), tokT("var"), tokT("tail") };
		tokT("tail").addGrammar(arr2);
		TokenType[] arr3 = { tokT(";") };
		tokT("tail").addGrammar(arr3);

		// var ->id = num
		TokenType[] arr4 = { tokT("id"), tokT("="), tokT("num") };
		tokT("var").addGrammar(arr4);

		// ////////end of grammar definition

		parse("list", false);

	}

	private void createMyGrammar() {
		ArrayList<TokenType> nTerms = new ArrayList<>(); // match my grammar
															// rule document
		/*
		 * TOO MUCH WORK!!! nTerms.add(new TokenType("program",
		 * TokenType.NONTERMINAL)); //0 nTerms.add(new
		 * TokenType("program_header", TokenType.NONTERMINAL)); //1
		 * nTerms.add(new TokenType("program_footer", TokenType.NONTERMINAL));
		 * //2 nTerms.add(new TokenType("declarations", TokenType.NONTERMINAL));
		 * //3 nTerms.add(new TokenType("varlist", TokenType.NONTERMINAL)); //4
		 * nTerms.add(new TokenType("stmts", TokenType.NONTERMINAL)); //5
		 * nTerms.add(new TokenType("stmt", TokenType.NONTERMINAL)); //6
		 * nTerms.add(new TokenType("asg_stmt", TokenType.NONTERMINAL)); //7
		 * nTerms.add(new TokenType("read_stmt", TokenType.NONTERMINAL)); //8
		 * nTerms.add(new TokenType("print_stmt", TokenType.NONTERMINAL)); //9
		 * nTerms.add(new TokenType("if_stmt", TokenType.NONTERMINAL)); //10
		 * nTerms.add(new TokenType("while_stmt", TokenType.NONTERMINAL)); //11
		 * nTerms.add(new TokenType("do_stmt", TokenType.NONTERMINAL)); //12
		 * nTerms.add(new TokenType("cond", TokenType.NONTERMINAL)); //13
		 * nTerms.add(new TokenType("relop", TokenType.NONTERMINAL)); //14
		 * nTerms.add(new TokenType("expr", TokenType.NONTERMINAL)); //15
		 * nTerms.add(new TokenType("arithop", TokenType.NONTERMINAL)); //16
		 * nTerms.add(new TokenType("variable", TokenType.NONTERMINAL)); //17
		 */
		String[] nonTerm_lib = { "programT", "program_header",
				"program_footer", "declarations", "varlist", "stmts", "stmt",
				"asg_stmt", "read_stmt", "print_stmt", "if_stmt", "while_stmt",
				"do_stmt", "cond", "relop", "expr", "arithop", "variable",
				"stmts_p", "array_assign", "varlist_append", "else_p",
				"variable_p" }; // hahaha
		for (String s : nonTerm_lib) {
			// add every tokentype into the terms list and then they all match
			// at the same index
			nTerms.add(new TokenType(s, TokenType.NONTERMINAL));
		}
		// System.out.println("nTerms:"+nTerms.toString());

		ArrayList<TokenType> terms = new ArrayList<>();
		String[] term_lib = { "program", ";", "end", "declare", "[", "]", ",",
				":=", "read", "print", "if", "then", "else", "while", "do",
				"until", "=", "/=", "<", ">", "<=", ">=", "+", "-", "*", "/",
				"(", ")", "unsigned_number", "identifier" }; // very useful
		for (String s : term_lib) {
			// add every tokentype into the terms list and then they all match
			// at the same index
			terms.add(new TokenType(s, TokenType.TERMINAL));
		}

		// mapping string to the object
		int nT_size = nTerms.size();
		for (int i = 0; i < nT_size; i++) {
			this.tokenMap.put(nonTerm_lib[i], nTerms.get(i));
		}
		int t_size = terms.size();
		for (int i = 0; i < t_size; i++) {
			this.tokenMap.put(term_lib[i], terms.get(i));
		}

		// writing grammar for everyone
		String[] grule;
		// I don't like to write redundant code.

		// for program
		/*
		 * "program", "program_header", "program_footer", "declarations",
		 * "varlist", "stmts", "stmt", "asg_stmt", "read_stmt", "print_stmt",
		 * "if_stmt", "while_stmt", "do_stmt", "cond", "relop", "expr",
		 * "arithop", "variable"
		 */

		grule = new String[] { "program_header", "declarations", "stmts",
				"program_footer" };
		pushGrammarTo("programT", grule);

		// for program_header
		grule = new String[] { "program", "identifier", ";" };
		pushGrammarTo("program_header", grule);

		// for program_footer
		grule = new String[] { "end", "program" };
		pushGrammarTo("program_footer", grule);

		// for declarations
		grule = new String[] { "declare", "varlist", ";" };
		pushGrammarTo("declarations", grule);
		tokT("declarations").setOptional(true);

		// for varlist
		grule = new String[] { "identifier", "array_assign", "varlist_append" };
		pushGrammarTo("varlist", grule);

		// for varlist children
		pushGrammarTo("array_assign", new String[] { "[", "unsigned_number",
				"]" });
		tokT("array_assign").setOptional(true);
		pushGrammarTo("varlist_append", new String[] { ",", "varlist" });
		tokT("varlist_append").setOptional(true);

		// for stmts
		grule = new String[] { "stmt", "stmts_p" };
		pushGrammarTo("stmts", grule);

		pushGrammarTo("stmts_p", new String[] { "stmts" });
		tokT("stmts_p").setOptional(true);

		// for stmt
		pushGrammarTo("stmt", new String[] { "asg_stmt" });
		pushGrammarTo("stmt", new String[] { "read_stmt" });
		pushGrammarTo("stmt", new String[] { "print_stmt" });
		pushGrammarTo("stmt", new String[] { "if_stmt" });
		pushGrammarTo("stmt", new String[] { "while_stmt" });
		pushGrammarTo("stmt", new String[] { "do_stmt" });

		// for asg_stmt
		pushGrammarTo("asg_stmt",
				new String[] { "variable", ":=", "expr", ";" });

		// for read_stmt
		pushGrammarTo("read_stmt", new String[] { "read", "variable", ";" });

		// for print_stmt
		pushGrammarTo("print_stmt", new String[] { "print", "expr", ";" });

		// for if_stmt
		pushGrammarTo("if_stmt", new String[] { "if", "cond", "then", "stmts",
				"else_p", "end", "if", ";" });
		pushGrammarTo("else_p", new String[] { "else", "stmts" });
		tokT("else_p").setOptional(true);

		// for while_stmt
		pushGrammarTo("while_stmt", new String[] { "while", "cond", "do",
				"stmts", "end", "while", ";" });

		// for do_stmt
		pushGrammarTo("do_stmt", new String[] { "do", "stmts", "until", "cond",
				";" });

		// for cond
		pushGrammarTo("cond", new String[] { "expr", "relop", "expr" });

		// for relop
		pushGrammarTo("relop", new String[] { "=" });
		pushGrammarTo("relop", new String[] { "/=" });
		pushGrammarTo("relop", new String[] { "<" });
		pushGrammarTo("relop", new String[] { ">" });
		pushGrammarTo("relop", new String[] { "<=" });
		pushGrammarTo("relop", new String[] { ">=" });

		// for expr
		pushGrammarTo("expr", new String[] { "variable" });
		pushGrammarTo("expr", new String[] { "-", "unsigned_number" });
		pushGrammarTo("expr", new String[] { "unsigned_number" });
		pushGrammarTo("expr", new String[] { "(", "expr", "arithop", "expr",
				")" });

		// for arithop
		pushGrammarTo("arithop", new String[] { "+" });
		pushGrammarTo("arithop", new String[] { "-" });
		pushGrammarTo("arithop", new String[] { "*" });
		pushGrammarTo("arithop", new String[] { "/" });

		// for variable
		pushGrammarTo("variable", new String[] { "identifier", "variable_p" });
		pushGrammarTo("variable_p", new String[] { "[", "expr", "]" });
		tokT("variable_p").setOptional(true);

	}

	/**
	 * A great function that helps you make code look better.
	 * 
	 * @param target
	 * @param grule
	 */
	private void pushGrammarTo(String target, String[] grule) {
		int length = grule.length;
		TokenType[] arr = new TokenType[length];
		for (int i = 0; i < length; i++) {
			arr[i] = tokT(grule[i]);
			/*
			 * if (target.equals("program_header")){
			 * System.out.println(grule[i]); }
			 */
		}

		tokT(target).addGrammar(arr);
		return;
	}

	/**
	 * 
	 * @param s_type
	 *            The name of your tokentype, such as "var"
	 * @return the object that represents your tokentype.
	 */
	private TokenType tokT(String s_type) {
		/*
		 * if (s_type.equals("")){ System.out.println("empty s_type"); }
		 */
		// System.out.println("s_type = "+s_type+"; the return would be"+this.tokenMap.get(s_type));
		TokenType r = this.tokenMap.get(s_type);
		if (r == null) {
			System.err.println("error: a token type is not recognized. It is '"+ s_type + "'");
			System.exit(1);
		}
		return r;
	}

	/**
	 * 
	 * @param tok_type
	 *            expected token type a string which indicates which evaluation
	 *            function we should call--each evaluation function is generated
	 *            based on your grammar rule, written and stored somewhere else
	 */
	private String parse(String tok_type, boolean lexeme_needed) {
		// first, is this tokentype terminal? if it's not, we need to check

		boolean isTerminal = tokT(tok_type).isTerminal();
		// get my first token
		Token cursor = this.getCursorToken();

		// System.out.printf("looking for %s at %d, where the token value is %s \n",tok_type,pos,cursor.getLexeme());
		if (isTerminal) {
			if (cursor.getType().equals(tok_type)) { // if my input matches
														// grammar
				// write_and_adjust();
				pos++; // move to next token
				// if (lexeme_needed) {
				return myWriter.convertTerm(cursor);
				/*
				 * } else {
				 * System.out.println("lexeme not needed for terminal - " +
				 * tok_type); // myWriter.writeNormal(tok_type,
				 * cursor.getLexeme()); return null;
				 * 
				 * }
				 */
			} else {
				// something is wrong
				parseError(tok_type, false);
			}
		} else { // it's not a terminal
			TokenType cursorT = tokT(cursor.getType());
			// System.out.println(cursorT.get_its_name());
			// System.out.println(tokT(tok_type).get_its_name());
			TokenType[] grule = tokT(tok_type).doesItMatch(cursorT);
			// input is the cursor

			if (grule == null) { // nonTerminal didn't match the first
				parseError(tok_type, true);
			} else if (grule.length == 0) {
				// base case: optional terminal
				// pos++ <--bug
				return null;
			} else {
				// this is the begin of a nonterminal symbol, so we need to
				// print something.
				// write_and_adjust();
				StringBuilder toReturn = new StringBuilder();
				ArrayList<String> specialkids = new ArrayList<>();
				boolean iamSpecial = false;

				if (myWriter.specialRules.contains(tok_type)) {
					// if I'm containing special kids, like if I'm <varlist>
					// speical tokens cannot be parsed as normal ones
					iamSpecial = true;
				}
				// dealing with pre-string
				if (!iamSpecial) {
					toReturn.insert(0, myWriter.firstPart(tok_type));
					// System.out.printf("----type %s first part [%s].----\n",tok_type,toReturn);
				} else {
					// if I'm special, wait for return;
				}

				// child terms
				for (TokenType expected_token_type : grule) {
					// !!! NOT moving on to next token!!!
					String childvalue;
					childvalue = parse(expected_token_type.get_its_name(),
							iamSpecial);
					// System.out.println("grammar symbol <"+tok_type+"> is successfully parsed.");
					specialkids.add(childvalue);
					// if you are special you have to return it

				}
				// end of the parsing of a nonterminal symbol, now it's time to
				// write something.

				if (!iamSpecial) {
					for (String str : specialkids) {
						if (str != null)
							toReturn.append(str);
						// System.out.printf("kids added for type %s, the value now is \n[]\n",tok_type,toReturn);
					}
					toReturn.append(myWriter.secondPart(tok_type));
				} else {
					if (specialkids.size() == 0)
						System.out
								.printf("!!tok type [%s] on token \"%s\" gives a nothing-return!!!\n",
										tok_type, cursor.getLexeme());
					toReturn.append(myWriter.returnSpecial(tok_type,
							specialkids));
					// System.out.printf("kids found for special type. the value of <%s> now is \n[]\n",tok_type,toReturn);
				}
				return toReturn.toString();
			}
		}
		System.out.println("this shouldn't be reached");
		return null;
		// we check if the first token matches with our

	}

	private void parseError(String tok_type, boolean grammarError) {
		// position is for human reading purpose, so we always put pos+1 after
		// calculation.
		Token tt = getCursorToken();
		int lineN = -1;
		boolean found = false;
		int i = 0;
		int max = this.lineN.size();
		// q is the number of token listed as the end of N
		while ((!found) && i < max) {
			double q = this.lineN.get(i);
			i += 1;
			if (pos <= q) {
				found = true;
				lineN = i;
			}
		}
		if (grammarError) {
			System.err
					.printf("Error: The grammar rule for '%s' is not matched. Refer to line %d, at token '%s'\n",
							tok_type, lineN, tt.getLexeme());
		} else {
			// if
			// (tok_type.equals(";")||tok_type.equals("do")||tok_type.equals("then"))
			// lineN-=1;
			System.err
					.printf("Error: Token type '%s' expected. Refer to line %d, at token '%s'\n",
							tok_type, lineN, tt.getLexeme());
		}
		System.exit(1);

	}

	private Token getCursorToken() {

		if (toklist.size() == pos) {
			return new Token("EOF", "");
		} else {
			return toklist.get(pos);
		}
	}

	/**
	 * the method which converts the string list to a token list.
	 * 
	 * @param stringlist
	 */
	private void convertTokens(ArrayList<String> stringlist) {
		for (String s : stringlist) {
			// algorithm: if it's not a regex-recognizable identifier, we assume
			// it's a sepcial identifier of its own type
			// such as = ; <= :=

			/*
			 * revise 9/18 23:59 SELECT function first for identifying reserve
			 * words. DON'T BE LAZY
			 */
			boolean reserved = false;
			String[] reserve_lib = { "program", ";", "end", "declare", "{",
					"}", "[", "]", ",", ":=", "read", "print", "if", "then",
					"else", "while", "do", "until", "=", "/=", "<", ">", "<=",
					">=", "+", "-", "*", "/", "(", ")" };
			for (String s2 : reserve_lib) {
				if (!reserved) {
					if (s2.equals(s)) {
						reserved = true;
						toklist.add(new Token(s2, s));
					}
				}
			}
			if (reserved) {
				// do nothing
			} else if (s.matches("[a-zA-Z]\\w*")) {
				// regex : [a-zA-Z]\w*
				toklist.add(new Token("identifier", s));
			} else if (s.matches("\\d+")) {
				// regex : \d+
				toklist.add(new Token("unsigned_number", s));
			} else {
				// special identifier of its own type own value, which is
				// unexpected
				// System.out.println("unexpected token: " + s);
				if (!s.equals("")) {
					toklist.add(new Token(s, s));
				}
			}
		}
		System.out.println("toklist is done. Size:" + toklist.size());
	}

	private void printToklist() {
		// test purpose: to understand Toklist composition; output stored in
		// toklist.txt
		try {
			// BufferedWriter writer = new BufferedWriter((new
			// FileWriter(mFile)));
			String str = "";
			File mFile = new File(Driver.getLocation() + "toklist.txt");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					(new FileOutputStream(mFile)), "UTF-8"));

			for (Token tok : toklist) {
				str = "[" + tok.getType() + "] " + "    " + tok.getLexeme()
						+ "\r\n";
				writer.write(str);
			}

			writer.flush();
			writer.close();
			System.out.println("toklist.txt ready");
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private class JavaWriter {
		int tab;
		private Map<String, RuleSet> javaRules;
		public ArrayList<String> specialRules;
		public Map<String, String> directTerms;
		public StringBuilder sstring;
		public boolean nread;

		public JavaWriter() {
			this.javaRules = new HashMap<String, RuleSet>();// key = type's name
															// , value =
															// conversion rule
			specialRules = new ArrayList<>();
			directTerms = new HashMap<>();
			sstring = new StringBuilder();
			tab = 0;
			this.nread = false;
			// System.out.println("nread :"+nread);
			initialJavaRule();
		}

		private boolean check_read() {
			Iterator<Token> i = SumCompiler.this.toklist.iterator();
			while (i.hasNext()) {
				if (i.next().getLexeme().equals("read")) {
					return true;
				}
			}
			return false;
		}

		/**
		 * 
		 * @param cur
		 * @return
		 */
		public String convertTerm(Token cur) {
			String type = cur.getType();
			if (type.equals("identifier") || type.equals("unsigned_number")) {
				return cur.getLexeme();
			}
			String conversion = directTerms.get(cur.getType());
			if (conversion == null) {
				return "";
			} else {
				return conversion;
			}
		}

		public String firstPart(String tok_type) {
			RuleSet rules = javaRules.get(tok_type);
			if (rules != null) {
				String str = rules.getPart1();
				// return format(str);
				return str;
			} else {
				// System.out.printf("type %s doesn't have ruleset.\n",
				// tok_type);
				return "";
			}
		}

		private StringBuffer format(String str) {
			// System.out.println("formatting is called. String:~~~~~~~~~\n"+str);
			if (str == null)
				return null;
			String delimt = "(?<=%1$s)|(?=%1$s)";
			// String sp_tab = String.format(delimt, "\\(\\tab)|(n)|(\\bt)");
			String sp_tab = String.format(delimt, "\\\\tab");
			// System.out.println(sp_tab);
			String sp_bt = String.format(delimt, "\\\\bt");
			String sp_n = String.format(delimt, "\\n");
			String[] arr = str.split(sp_tab + "|" + sp_bt + "|" + sp_n);
			// System.out.println(arr.length);
			StringBuffer mystr = new StringBuffer();
			for (String s : arr) {
				s = s.replaceAll("(^[\\t ]+)|([\\t ]$)", "");
				// System.out.println("substring [" + s + "]");
				if (s.equals("")) {
					// System.out.println("empty string");
				} else if (s.equals("\\tab")) {
					// System.out.println("tab detected");
					this.tab += 1;
					mystr.append("\t");
				} else if (s.equals("\\bt")) {
					// System.out.println("bt");
					this.tab -= 1;
					mystr.deleteCharAt(mystr.lastIndexOf("\t"));
				} else if (s.equals("\n")) {
					mystr.append("\n");
					for (int i = 0; i < tab; i++) {
						mystr.append("\t");
					}
				} else {
					mystr.append(s);
				}
			}

			return mystr;
		}

		public String secondPart(String tok_type) {
			RuleSet rules = javaRules.get(tok_type);
			if (rules != null) {
				String str = rules.getPart2();
				// return format(str);
				return str;
			} else {
				// System.out.printf("type %s doesn't have ruleset.\n",
				// tok_type);
				return "";
			}
		}

		public void initialJavaRule() {
			// write a conversion rule for the java output process
			// unsigned_number and identifiers need to be kept as its lexeme,
			// suggested by using "\\lexeme"
			/*
			 * String[] term_lib = { "program", ";", "end", "declare", "[", "]",
			 * ",", ":=", "read", "print", "if", "then", "else", "while", "do",
			 * "until", "=", "/=", "<", ">", "<=", ">=", "+", "-", "*", "/",
			 * "(", ")", "unsigned_number", "identifier" }; // very useful
			 * 
			 * String[] nonTerm_lib = { "programT", "program_header",
			 * "program_footer", "declarations", "varlist", "stmts", "stmt",
			 * "asg_stmt", "read_stmt", "print_stmt", "if_stmt", "while_stmt",
			 * "do_stmt", "cond", "relop", "expr", "arithop", "variable",
			 * "stmts_p", "array_assign", "varlist_append", "else_p",
			 * "variable_p" }; // hahaha
			 */
			// addR("program","", ""); // when finishing analyzing
			// program token
			directTerms.put("+", " + ");
			directTerms.put("-", " - ");
			directTerms.put("*", " * ");
			directTerms.put("/", " / ");
			directTerms.put("=", "==");
			directTerms.put("/=", "!=");
			directTerms.put("<", "<");
			directTerms.put(">", ">");
			directTerms.put("<=", "<=");
			directTerms.put(">=", ">=");
			directTerms.put("[", "[");
			directTerms.put("]", "]");
			directTerms.put("(", "(");
			directTerms.put(")", ")");
			directTerms.put(":=", " = ");

			// addR("varlist_append", "", "");
			// stmts = empty; stmts_p = empty;
			// addR("stmt", "", "\n");
			addR("asg_stmt", "", ";\n");
			addR("expr", "", "");
			addR("variable", "", "");
			addR("variable_p", "", "");
			addR("program_footer", "", "\\bt}\n\\bt}");

			// varlist is special
			// array_assign
			// identifier?
			/*
			 * addR("program_header", "\n{"); addR("program_header", "\n{");
			 * addR("program_header", "\n{");
			 */

			// specialRules.add("array_assign");

			specialRules.add("program_header");
			specialRules.add("declarations");
			specialRules.add("varlist");
			// specialRules.add("variable");
			// specialRules.add("variable_p");
			specialRules.add("read_stmt");
			specialRules.add("print_stmt");
			specialRules.add("if_stmt");
			specialRules.add("do_stmt");
			specialRules.add("while_stmt");

		}

		private void addR(String type, String firstPart, String part2) {
			// \\tab(\s) means tab, but how many depends on the case
			// \\lex(\s) means its lexeme is required
			// \\bt(\s) means to reverse one tab
			RuleSet rr = new RuleSet(type, firstPart, part2);
			this.javaRules.put(type, rr);

		}

		/*
		 * 
		 * if there's no rules -->special function checked
		 */
		/**
		 * @deprecated
		 * @param specialkids
		 * @return
		 */
		private String variable_p(ArrayList<String> specialkids) {

			return null;

		}

		/**
		 * this generates the string for special tokens.
		 * 
		 * @param tok_type
		 * @param specialkids
		 * @return
		 */
		public String returnSpecial(String tok_type,
				ArrayList<String> specialkids) {
			switch (tok_type) {
			case "declarations":
				return declarations(specialkids);
			case "varlist":
				return varlist(specialkids);
			case "read_stmt":
				return read_stmt(specialkids);
			case "print_stmt":
				return print_stmt(specialkids);
			case "do_stmt":
				return do_stmt(specialkids);
			case "while_stmt":
				return while_stmt(specialkids);
			case "if_stmt":
				return if_stmt(specialkids);
			case "program_header":
				return program_header(specialkids);
			default:
				// System.out.printf("type %s is not special.\n",tok_type);
				break;
			}
			return "";
		}

		private String declarations(ArrayList<String> specialkids) {
			// need to define in stream and then append all the junk
			StringBuilder s = new StringBuilder();
			this.nread = check_read();
			if (this.nread) {
				s.append("BufferedReader in = new BufferedReader(new InputStreamReader(System.in));\n");
			}
			s.append(specialkids.get(1));
			return s.toString();
		}

		private String program_header(ArrayList<String> specialkids) {
			// need to define in stream and then append all the junk
			StringBuilder s = new StringBuilder(
					"import java.io.*;\n public class ");
			String input = specialkids.get(1);
			// s.append(input.substring(0,
			// 1).toUpperCase()).append(input.substring(1));
			s.append(input);
			s.append("\n{\n\\tab public static void main(String[] args) throws IOException {\n\\tab");
			return s.toString();
		}

		private String varlist(ArrayList<String> specialkids) {
			String idt = specialkids.get(0);
			String arrr = specialkids.get(1);
			String append = specialkids.get(2);
			StringBuilder s = new StringBuilder();
			if (arrr == null || arrr.isEmpty()) {
				// int n;
				s.append("int ").append(idt).append(";\n");
			} else {
				s.append("int[] ").append(idt).append(" = new int")
						.append(arrr);
				s.append(";\n");
			}
			if (append != null)
				s.append(append);
			return s.toString();

		}

		private String read_stmt(ArrayList<String> specialkids) {
			String s1 = specialkids.get(1);
			String s2 = " = Integer.parseInt(in.readLine().trim());\n";
			return s1.concat(s2);
		}

		private String print_stmt(ArrayList<String> specialkids) {
			// System.out.println(A[i]);
			String s1 = specialkids.get(1);
			return "System.out.println(" + s1 + ");\n";
		}

		private String do_stmt(ArrayList<String> specialkids) {
			// System.out.println(A[i]);
			String stmts = specialkids.get(1);
			String cond = specialkids.get(3);
			StringBuilder s = new StringBuilder("do {\n\\tab").append(stmts);
			s.append("\\bt} while ( !(").append(cond).append("));\n");
			return s.toString();
		}

		private String while_stmt(ArrayList<String> specialkids) {
			// System.out.println(A[i]);
			String cond = specialkids.get(1);
			String stmts = specialkids.get(3);
			StringBuilder s = new StringBuilder("while (").append(cond).append(
					"){\n\\tab");
			s.append(stmts).append("\\bt}\n");
			return s.toString();
		}

		private String if_stmt(ArrayList<String> specialkids) {
			// System.out.println(A[i]);
			String cond = specialkids.get(1);
			String stmts = specialkids.get(3);
			String else_p = specialkids.get(4);
			StringBuilder s = new StringBuilder("if (").append(cond).append(
					") { \n\\tab");
			s.append(stmts);
			s.append("\n\\bt }\n");
			if (else_p == null || else_p.isEmpty()) {
				// s.append("\n");
			} else {
				s.append(" else { \n\\tab").append(else_p);
				s.append("\n\\bt }\n");
			}
			return s.toString();
		}

		public void write(StringBuffer s) {
			try {
				// BufferedWriter writer = new BufferedWriter((new
				// FileWriter(mFile)));
				File mFile = new File(Driver.getLocation()
						+ SumCompiler.this.filename + ".java");
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter((new FileOutputStream(mFile)),
								"UTF-8"));
				writer.write(s.toString());
				writer.flush();
				writer.close();
				System.out.println("java file ready");
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		// SumCompiler a = new SumCompiler();

		Driver d = new Driver();
		/*
		 * String[] arr="fo3=e".split("(?<=\\w)(?=\\=)|(?<=\\w\\=)"); for
		 * (String a : arr){ System.out.println(a); }
		 */
		d.run();
		/*
		 * String a, b, c; a = null; b= "hoo"; c = a+b; System.out.println(c);
		 */

		/*
		 * SumCompiler ss = new SumCompiler("", ""); /* StringBuilder test = new
		 * StringBuilder().append("\n").append("oh ok").append("\n");
		 * System.out.println(test.toString());
		 */
		/*
		 * String str = ss.myWriter
		 * .format("\\tab I'm here\n there we go \n \\bt time to say goodbye"
		 * ).toString(); System.out.print(str);
		 */
	}

}
