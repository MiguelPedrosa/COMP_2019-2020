public class ASTEqualsId extends SimpleNode {

  protected String identifier;

  public ASTEqualsId(int id) {
    super(id);
  }

  public ASTEqualsId(Parser p, int id) {
    super(p, id);
  }

  public String getIdentifier() {
    return identifier;
  }

  @Override
  public String toString() {
    return super.toString() + " [ " + identifier + " ]";
  }

}
