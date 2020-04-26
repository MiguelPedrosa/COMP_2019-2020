import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportTable {
    private HashMap<String, SymbolImport> classes;

    public ImportTable() {
        this.classes = new HashMap<>();
    }

    public void addImport(ASTImportDeclaration node) {
        final String className = node.getClassName();
        if (!classes.containsKey(className)) {
            final SymbolImport newClass = new SymbolImport();
            this.classes.put(className, newClass);
        }
        final SymbolImport classInfo = this.classes.get(className);
        final String methodName = node.getMethodName();
        final List<String> arguments = node.getArguments();
        final Boolean isStatic = node.isStatic();
        final String returnType = node.getReturnType();
        if (methodName != null) { // Node represents a method
            if (isStatic) {
                classInfo.addStaticMethod(methodName, returnType, arguments);
            } else {
                classInfo.addMethod(methodName, returnType, arguments);
            }
        } else { // Node represents a constructor
            if (isStatic) {
                ErrorHandler.addError("Constructor can't be declared as static", node.getLine());
                return;
            }
            if (returnType != className && !returnType.equals("void")) {
                ErrorHandler.addError("Constructor can't produce an object different from its type", node.getLine());
                return;
            }
            if (arguments.size() == 0) { // Default constructor
                classInfo.addDefaultConstructor();
            } else { // Alternative constructor
                classInfo.addConstructor(className, arguments);
            }
        }
    }

    public String toString() {
        String returnInfo = "";
        for (Map.Entry<String, SymbolImport> entry : classes.entrySet()) {
            returnInfo += "class " + entry.getKey() + ":\n" + entry.getValue();
        }
        return returnInfo;
    }
}