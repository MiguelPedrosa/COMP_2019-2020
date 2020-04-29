public class ASTMainDeclaration extends SimpleNode {

  public String argumentName = "";

  public ASTMainDeclaration(int id) {
    super(id);
  }

  public ASTMainDeclaration(Parser p, int id) {
    super(p, id);
  }

  @Override
  public String toString() {
    return super.toString() + " [ argumentName=\'" + argumentName + "\' ]";
  }

}
