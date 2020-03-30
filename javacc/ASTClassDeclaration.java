public class ASTClassDeclaration extends SimpleNode {

  protected String classId, extendsId;

  public ASTClassDeclaration(int id) {
    super(id);
  }

  public ASTClassDeclaration(Parser p, int id) {
    super(p, id);
  }

  /**
   * @return the classId
   */
  public String getClassId() {
    return classId;
  }

  /**
   * @return the extendsId
   */
  public String getExtendsId() {
    return extendsId;
  }

  @Override
  public String toString() {
    return super.toString() + " [ name=\'" + classId + "\' ]";
  }

}
