import java.util.ArrayList;
/**
 * @deprecated
 * @author Kunhuan
 *
 */
public class TokenList {
	private ArrayList<Token> myList;

	public TokenList() {
		this.myList = new ArrayList<>();
	}
	
	public void addToken(Token a){
		this.myList.add(a);
	}
	

}
