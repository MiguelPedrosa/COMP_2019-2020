public class ASTIdentifier extends SimpleNode {
	public ASTIdentifier(int id) {
		super(id);
	}

	public ASTIdentifier(Parser p, int id) {
		super(p, id);
	}
}
