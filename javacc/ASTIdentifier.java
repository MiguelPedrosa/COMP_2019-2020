public class ASTIdentifier extends SimpleNode {

	protected String identifier;

	public ASTIdentifier(int id) {
		super(id);
	}

	public ASTIdentifier(Parser p, int id) {
		super(p, id);
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String toString() {
	  return super.toString() + " [ identifier=\'" + identifier + "\' ]";
	}

}
