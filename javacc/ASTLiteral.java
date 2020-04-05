public class ASTLiteral extends SimpleNode {

  protected String literal;

  public ASTLiteral(int id) {
    super(id);
  }

  public ASTLiteral(Parser p, int id) {
    super(p, id);
  }

  public String getLiteral() {
    return literal;
  }

  @Override
  public String toString() {
    return super.toString() + " [ " + literal + " ]";
  }

}
