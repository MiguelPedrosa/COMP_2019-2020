import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class ASTMethodDeclaration extends SimpleNode {

	protected String methodName;
	protected LinkedHashMap<String, String> arguments;
	protected String returnType;
	protected String key;

	public ASTMethodDeclaration(int id) {
		super(id);
		this.returnType = "void";
		this.methodName = null;
		this.arguments = new LinkedHashMap<String, String>();
		this.key = null;
	}

	public ASTMethodDeclaration(Parser p, int id) {
		super(p, id);
	}

	public void buildMethodName(String name) {
		this.methodName = name;
	}

	public void addArgument(String type, String name) {
		this.arguments.put(name, type);
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getReturnType() {
		return returnType;
	}

	public String getMethodName() {
		return methodName;
	}

	public LinkedHashMap<String, String> getArguments() {
		return arguments;
	}

	public String getKey() {
		if (this.key == null) {
			String key = this.methodName;

			for (Map.Entry<String, String> entry : arguments.entrySet())
				key += entry.getValue();

			this.key = key;
		}
		return this.key;
	}

	@Override
	public String toString() {
		final String name = "name=\'" + methodName + "\'";
		String args = "arguments=[ ";

		for (Map.Entry<String, String> entry : arguments.entrySet())
			args += "{" + entry.getValue() + ", " + entry.getKey() + "} ";

		args += "]";

		final String returnInfo = "return=" + returnType;
		return super.toString() + " [ " + name + "; " + args + "; " + returnInfo + " ]";
	}

}
