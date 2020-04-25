import java.util.ArrayList;
import java.util.List;

public class ASTImportDeclaration extends SimpleNode {

	protected String methodName;
	protected String className;
	protected List<String> argumentTypes;
	protected Boolean isStatic;
	protected String returnType;
	
	public ASTImportDeclaration(int id) {
		super(id);
		// By default, class import is non-static
		this.isStatic = false;
		// By default, class import is void
		this.returnType = "void";
		this.className = null;
		this.methodName = null;
		this.argumentTypes = new ArrayList<>();
	}

	public ASTImportDeclaration(Parser p, int id) {
		super(p, id);
	}

	public void setClass(String className) {
		this.className = className;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void addArgument(String arg) {
		this.argumentTypes.add(arg);
	}

	public void setStatic() {
		this.isStatic = true;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	@Override
	public String toString() {
		final String classInfo = "class=\'" + className + "\'";
		final String methodInfo = methodName != null ? "method=\'" + methodName + "\'" : "constructor";
		final String staticInfo = (this.isStatic ? "static" : "non-static");
		final String args = "arguments=" + argumentTypes;
		final String returnInfo = "return=" + returnType;
		return super.toString() + " [ " + classInfo + "; "+ methodInfo + "; " + staticInfo + "; " + args + "; " + returnInfo + " ]";
	}
}
