import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    private String className;
    private String classExtendsName;
    private SymbolTable parentTable;
    private LinkedHashMap<String, SymbolVar> variables;
    private LinkedHashMap<String, MethodTable> methods;
    private MainTable main;
    private ImportTable importTable;

    public SymbolTable(SymbolTable parentTable) {
        this.parentTable = parentTable;
        variables = new LinkedHashMap<>();
        methods = new LinkedHashMap<>();
        importTable = new ImportTable();
    }

    /*
     * ------------------------------------------ ADD TO TABLE
     * ----------------------------------------------
     */

    public void addClasseName(String name) {
        this.className = name;
    }

    public void addClassExtendsName(String name) {
        this.classExtendsName = name;
    }

    public void addVariable(String type, String name, int tokenLine) {
        if (containsVariable(name)) {
            ErrorHandler.addError("Variable (" + name + ") declaration repeated", tokenLine);

            return;
        }
        final SymbolVar var = new SymbolVar(name, type);
        variables.put(name, var);
    }

    public void addLocalVariable(String methodKey, String type, String name, int tokenLine) {
        if (containsVariable(name))
            ErrorHandler.addWarning("Variable '" + name + "' already defined in class.", tokenLine);

        if (methodKey.equals("main"))
            this.main.addVariable(type, name, tokenLine);
        else
            methods.get(methodKey).addVariable(type, name, tokenLine);

    }

    public boolean addMain() {
        if(this.main != null)
            return false;
        
        this.main = new MainTable(this);
        return true;
        
    }

    public boolean addMethod(String key, String name, LinkedHashMap<String, String> arguments, String returnType) {
        MethodTable method = new MethodTable(this, name, returnType, arguments);
        if (methods.containsKey(key))
            return false;
        methods.put(key, method);
        return true;
    }

    public void addImport(ASTImportDeclaration node) {
        this.importTable.addImport(node);
    }

    /*
     * ------------------------------------------- GETTERS
     * -------------------------------------------------
     */

    public String getClasseName() {
        return this.className;
    }

    public String getClassExtendsName() {
        return this.classExtendsName;
    }

    public Map<String, SymbolVar> getVariables() {
        return variables;
    }

    public String getVariableType(String VarId) {
        SymbolVar var = variables.get(VarId);
        return var.getType();
    }

    public SymbolVar getVariable(String VarId) {
        SymbolVar var = variables.get(VarId);
        return var;
    }

    public String getMethodVariableType(String methodKey, String VarId) {
        SymbolVar var;

        if (methodKey.equals("main"))
            var = this.main.getVariables().get(VarId);
        else {
            MethodTable method = methods.get(methodKey);
            var = method.getVariables().get(VarId);
        }

        return var.getType();
    }

    public SymbolVar getMethodVariable(String methodKey, String VarId) {
        SymbolVar var;

        if (methodKey.equals("main"))
            var = this.main.getVariables().get(VarId);
        else {
            MethodTable method = methods.get(methodKey);
            var = method.getVariables().get(VarId);
        }

        return var;
    }

    public String getMethodReturn(String methodKey) {
        MethodTable method = methods.get(methodKey);
        return method.getReturnType();
    }

    public String getMethodName(String methodKey) {
        MethodTable method = methods.get(methodKey);
        return method.getName();
    }

    public LinkedHashMap<String,MethodTable> getMethods() {
        return this.methods;
    }

    public MainTable getMain() {
        return this.main;
    }

    public HashMap<String, SymbolImport> getImports() {
        return this.importTable.getClasses();
    }

    /*
     * ------------------------------------------- CHECKERS
     * ------------------------------------------------
     */

    public Boolean containsMethod(String methodKey) {
        return methods.containsKey(methodKey);
    }

    public Boolean containsVariable(String VarId) {
        return variables.containsKey(VarId);
    }

    public Boolean containsMethodVariable(String methodKey, String VarId) {
        if (methodKey.equals("main") && this.main != null)
            return this.main.containsVariable(VarId);

        if (methods.containsKey(methodKey))
            return methods.get(methodKey).containsVariable(VarId);

        return false;
    }

    /*
     * ------------------------------------------- EXTRA
     * ---------------------------------------------------
     */

    public void initializeVariable(String methodKey, String VarId, int initializationLevel) {
        if (containsMethodVariable(methodKey, VarId)) {
            if (this.main != null && methodKey.equals("main") && this.main.getVariables().get(VarId).getInitialized() != 2 )
                this.main.getVariables().get(VarId).setInitialize(initializationLevel);
            else if (methods.containsKey(methodKey) && methods.get(methodKey).getVariables().get(VarId).getInitialized() != 2)
                methods.get(methodKey).getVariables().get(VarId).setInitialize(initializationLevel);

            return;
        } else if (containsVariable(VarId))
            getVariables().get(VarId).setInitialize(initializationLevel);
        else
            return;
    }

    public Boolean canObjectBeCreated(String className) {
        return this.importTable.canObjectBeCreated(className);
    }

    public String toString() {

        String variableInfo = MyUtils.ANSI_CYAN + "Class\n" + MyUtils.ANSI_RESET + "Variables:\n";

        for (Map.Entry<String, SymbolVar> entry : variables.entrySet())
            variableInfo += "\t" + entry.getValue() + "\n";

        variableInfo += this.main;

        for (Map.Entry<String, MethodTable> entry : methods.entrySet())
            variableInfo += "\t" + entry.getValue() + "\n";

        variableInfo += this.importTable;

        return variableInfo;
    }
}