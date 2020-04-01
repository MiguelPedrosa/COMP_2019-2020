import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private SymbolTable parentTable;
    private Map<String, String> variables;
    private Map<String, FunctionSymbol> functions;

    public SymbolTable(SymbolTable parentTable) {
        this.parentTable = parentTable;
        variables = new HashMap<>();
        functions = new HashMap<>();
    }

    public Boolean containsVariable(String name) {
        final String variable = variables.get(name);
        return variable != null;
    }

    public void addVariable(String type, String name) {
        if(containsVariable(name)){
            System.out.println(MyUtils.ANSI_RED + "ERROR: Variable (" + name + ") declaration repeated" + MyUtils.ANSI_RESET);
            return;
        }

        variables.put(name, type);
    }

    public void addFunction(String returnType, String name, Boolean isStatic){
        final FunctionSymbol function = new FunctionSymbol(this, returnType, isStatic);
        functions.put(name, function);
    }

    public String toString() {
        String variableInfo = "Variables:\n";
        for(Map.Entry<String, String> entry : variables.entrySet()) {
            variableInfo += "\tname=" + entry.getKey() + ";type=" + entry.getValue() + "\n";
        }

        return variableInfo;
    }
}