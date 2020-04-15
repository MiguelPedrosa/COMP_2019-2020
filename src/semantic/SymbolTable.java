import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    private SymbolTable parentTable;
    private LinkedHashMap<String, SymbolVar> variables;
    private LinkedHashMap<String, MethodTable> methods;
    private MainTable main;

    public SymbolTable(SymbolTable parentTable) {
        this.parentTable = parentTable;
        variables = new LinkedHashMap<>();
        methods = new LinkedHashMap<>();
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

    public void addLocalVariable(String methodKey, String type, String name) {
        if(methodKey.equals("main"))
            this.main.addVariable(type, name);
        else{
            methods.get(methodKey).addVariable(type, name);
        }
    }

    public void addMain(){
        this.main = new MainTable(this);
    }

    public void addMethod(String key, String name, LinkedHashMap<String, String> arguments, String returnType){
        MethodTable method = new MethodTable(this, name, returnType, arguments);
        methods.put(key, method);
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