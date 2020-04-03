import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MethodTable extends SymbolTable {

    private List<String> argumentTypes;
    private String returnType;
    private String name;


    public MethodTable(SymbolTable parentTable, String name, String returnType, List<String> argumentTypes) {
        super(parentTable);
        this.argumentTypes = argumentTypes;
        this.returnType = returnType;
        this.name = name;
    }

    public String toString() {
        String variableInfo = MyUtils.ANSI_CYAN + "\n\tMethod" + MyUtils.ANSI_RESET + " name=" + this.name + ";return type=" + this.returnType + ";arguments=";

        for(int i = 0; i < argumentTypes.size(); i++) {
            variableInfo += argumentTypes.get(i);
        }

        variableInfo += "\n";
        variableInfo += "\tLocal variables:\n";
        for(Map.Entry<String, SymbolVar> entry : getVariables().entrySet()) {
            variableInfo += "\t\t" + entry.getValue() + "\n";
        }

        return variableInfo;
    }

}