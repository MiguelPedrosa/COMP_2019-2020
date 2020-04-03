import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    private SymbolTable parentTable;
    private Map<String, SymbolVar> variables;
    private Map<String, MethodTable> methods;
    private MainTable main;

    public SymbolTable(SymbolTable parentTable) {
        this.parentTable = parentTable;
        variables = new HashMap<>();
        methods = new HashMap<>();
    }

    public Boolean containsVariable(String name) {
        final SymbolVar variable = variables.get(name);
        return variable != null;
    }

    public void addVariable(String type, String name) {
        if(containsVariable(name)){
            System.out.println(MyUtils.ANSI_RED + "ERROR: Variable (" + name + ") declaration repeated" + MyUtils.ANSI_RESET);
            return;
        }
        final SymbolVar var = new SymbolVar(name, type);
        variables.put(name, var);
    }

    public void addLocalVariable(String method, String type, String name) {
        if(method.equals("main"))
            this.main.addVariable(type, name);
        else{
            methods.get(method).addVariable(type, name);
        }
    }

    public void addMain(){
        this.main = new MainTable(this);
    }

    public void addMethod(String returnType, String name, List<String> argumentTypes){
        final MethodTable method = new MethodTable(this, name, returnType, argumentTypes);
        methods.put(name, method);
    }

    public Map<String, SymbolVar> getVariables(){
        return variables;
    }

    public String toString() {
        String variableInfo = MyUtils.ANSI_CYAN + "Class\n" + MyUtils.ANSI_RESET + "Variables:\n";
        for(Map.Entry<String, SymbolVar> entry : variables.entrySet()) {
            variableInfo += "\t" + entry.getValue() + "\n";
        }

        variableInfo += this.main;

        for(Map.Entry<String, MethodTable> entry : methods.entrySet()) {
            variableInfo += "\t" + entry.getValue() + "\n";
        }
        return variableInfo;
    }
}