import java.util.ArrayList;
import java.util.List;

public class ASTImportDeclaration extends SimpleNode {

	protected String methodName;
	protected List<String> argumentTypes;
	protected Boolean isStatic;
	protected String returnType;
	
	public ASTImportDeclaration(int id) {
		super(id);
		// By default, class import is non-static
		this.isStatic = false;
		// By default, class import is void
		this.returnType = "void"; 
		this.methodName = null;
		this.argumentTypes = new ArrayList<>();
	}

	public ASTImportDeclaration(Parser p, int id) {
		super(p, id);
	}

	public void buildMethodName(String name) {
		if(this.methodName == null) {
			this.methodName = name;
		} else {
			this.methodName += "." + name;
		}
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
		final String name = "name=\'" + methodName + "\'";
		final String staticInfo = (this.isStatic ? "static" : "non-static");
		final String args = "arguments=" + argumentTypes;
		final String returnInfo = "return=" + returnType;
		return super.toString() + " [ " + name + "; " + staticInfo + "; " + args + "; " + returnInfo + " ]";
	}
}
