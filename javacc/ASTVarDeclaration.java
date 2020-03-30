public class ASTVarDeclaration extends SimpleNode {

  protected String varId, type;

  public ASTVarDeclaration(int id) {
    super(id);
  }

  public ASTVarDeclaration(Parser p, int id) {
    super(p, id);
  }

  @Override
  public String toString() {
    return super.toString() + " [ name=\'" + varId + "\', type=\'" + type + "\' ]";
  }

}
