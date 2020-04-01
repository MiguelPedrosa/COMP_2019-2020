import java.util.LinkedHashMap;

public class FunctionSymbol extends SymbolTable {

    private LinkedHashMap<String,String> arguments;
    private String returnType;
    private Boolean isStatic;


    public FunctionSymbol(SymbolTable parentTable, String returnType, Boolean isStatic) {
        super(parentTable);
        this.arguments = new LinkedHashMap<>();
        this.returnType = returnType;
        this.isStatic = isStatic;
    }

    public void addArgument(String type, String name) {
        arguments.put(name, type);
    }

}