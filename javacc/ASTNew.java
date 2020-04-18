public class ASTNew extends SimpleNode {

  private String type;

  public ASTNew(int id) {
    super(id);
  }

  public ASTNew(Parser p, int id) {
    super(p, id);
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }

}
