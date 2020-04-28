import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SymbolImport {

    /*
     * Default constructor has to be imported for class to be constructed.
     */
    private Boolean hasDefaultConstructor;
    /*
     * The following two structures are used to store static and non static methods.
     * This can be infered based on the way the function is called.
     */
    private HashMap<String, ImportMethod> methods;
    private HashMap<String, ImportMethod> staticMethods;
    /*
     * This List is meant to store all possible and importable constructor. However,
     * the grammar currently only accepts the default constructor for now, so this
     * is here to store alternative imported constructors in case the grammar is
     * changed.
     */
    private ArrayList<ImportMethod> constructors;

    public SymbolImport() {
        this.hasDefaultConstructor = false;
        this.methods = new HashMap<>();
        this.staticMethods = new HashMap<>();
        this.constructors = new ArrayList<>();
    }

    public Boolean hasDefaultConstructor() {
        return this.hasDefaultConstructor;
    }

    public Boolean hasMethod(String methodKey) {
        return this.methods.containsKey(methodKey);
    }

    public String getMethodType(String methodKey) {
        return this.methods.get(methodKey).getReturnType();
    }
    
    public String getStaticMethodType(String methodKey) {
        return this.staticMethods.get(methodKey).getReturnType();
    }

    public Boolean hasStaticMethod(String methodKey) {
        return this.staticMethods.containsKey(methodKey);
    }

    public void addDefaultConstructor() {
        this.hasDefaultConstructor = true;
    }

    public void addConstructor(String className, List<String> arguments) {
        final ImportMethod constructor = new ImportMethod(className, className, arguments);
        this.constructors.add(constructor);
    }

    public void addMethod(String name, String returnType, List<String> arguments) {
        final ImportMethod method = new ImportMethod(name, returnType, arguments);
        final String signature = method.getSignature();
        this.methods.put(signature, method);
    }

    public void addStaticMethod(String name, String returnType, List<String> arguments) {
        final ImportMethod staticMethod = new ImportMethod(name, returnType, arguments);
        final String signature = staticMethod.getSignature();
        this.staticMethods.put(signature, staticMethod);
    }

    public String toString() {
        String stringInfo = "";
        stringInfo += "\thas" + (this.hasDefaultConstructor ? "" : " no") + " default constructor\n";
        stringInfo += "\tConstructores:\n";
        for (ImportMethod cons : this.constructors) {
            stringInfo += "\t\t" + cons + "\n";
        }
        stringInfo += "\tNone static methods:\n";
        for (Map.Entry<String, ImportMethod> entry : this.methods.entrySet()) {
            stringInfo += "\t\t" + entry.getValue() + "\n";
        }
        stringInfo += "\tStatic methods:\n";
        for (Map.Entry<String, ImportMethod> entry : this.staticMethods.entrySet()) {
            stringInfo += "\t\t" + entry.getValue() + "\n";
        }
        return stringInfo;
    }

    private class ImportMethod {

        private List<String> arguments;
        private String returnType;
        private String name;

        public ImportMethod(String name, String returnType, List<String> arguments) {
            this.returnType = returnType;
            this.arguments = arguments;
            this.name = name;
        }

        public String getReturnType(){
            return returnType;
        }

        public String getSignature() {
            String signature = name;
            for (String arg : this.arguments) {
                signature += ";" + arg;
            }
            return signature;
        }

        public String toString() {
            return "name: " + name + "; return: " + returnType + "; arguments: " + arguments;
        }
    }
}