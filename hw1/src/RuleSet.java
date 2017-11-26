public class RuleSet {
	private String type;
	private String firstPart;
	private String afterPart;
	

	public RuleSet(String type, String p1, String p2) {
		this.type = type;
		this.firstPart=p1;
		this.afterPart = p2;
	}
	
	public String getPart1(){
		return this.firstPart;
	}
	
	public String getPart2(){
		return this.afterPart;
	}
	public String getType(){
		return type;
	}

}
