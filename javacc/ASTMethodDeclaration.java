import java.util.ArrayList;
import java.util.List;

public class ASTMethodDeclaration extends SimpleNode {

	protected String methodName;
	protected List<String> argumentTypes;
	protected String returnType;

	public ASTMethodDeclaration(int id) {
		super(id);
		this.returnType = "void";
		this.methodName = null;
		this.argumentTypes = new ArrayList<>();
	}

	public ASTMethodDeclaration(Parser p, int id) {
		super(p, id);
	}

	public void buildMethodName(String name) {
		this.methodName = name;
	}

	public void addArgument(String type, String name) {
		final String argument = "{" + type + ", " + name + "}";
		this.argumentTypes.add(argument);
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	@Override
	public String toString() {
		final String name = "name=\'" + methodName + "\'";
		final String args = "arguments=" + argumentTypes;
		final String returnInfo = "return=" + returnType;
		return super.toString() + " [ " + name + "; " + args + "; " + returnInfo + " ]";
	}

}
