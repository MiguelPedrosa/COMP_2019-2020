import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class ASTMethodDeclaration extends SimpleNode {

	protected String methodName;
	protected List<String[]> arguments;
	protected String returnType;
	protected String key;

	public ASTMethodDeclaration(int id) {
		super(id);
		this.returnType = "void";
		this.methodName = null;
		this.arguments = new ArrayList<>();
		this.key = null;
	}

	public ASTMethodDeclaration(Parser p, int id) {
		super(p, id);
	}

	public void buildMethodName(String name) {
		this.methodName = name;
	}

	public void addArgument(String type, String name) {
		String[] aux = {name, type};
		this.arguments.add(aux);
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

	public List<String[]> getArguments() {
		return arguments;
	}

	public String getMethodKey(String methodName) {
        String key = methodName;

        for(String[] argument: this.arguments)
            key += ";" + argument[1];

        return key;
    }

	@Override
	public String toString() {
		final String name = "name=\'" + methodName + "\'";
		String args = "arguments=[ ";

		for(String[] aux: this.arguments)
			args += "{" + aux[1] + ", " + aux[0] + "} ";
			
		args += "]";

		final String returnInfo = "return=" + returnType;
		return super.toString() + " [ " + name + "; " + args + "; " + returnInfo + " ]";
	}

}
