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

  public String getLiteralType() {
    if(literal.equals("this"))
      return literal;
    else if(literal.equals("true") || literal.equals("false"))
      return "boolean";
    else
      return "int";

  }

  @Override
  public String toString() {
    return super.toString() + " [ " + literal + " ]";
  }

}
