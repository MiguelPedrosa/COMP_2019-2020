import java.util.Map;

public class MainTable extends SymbolTable {

    public MainTable(SymbolTable parentTable, String argumentName) {
        super(parentTable);
        addVariable("String[]", argumentName, -1);

        for (Map.Entry<String, SymbolVar> entry : getVariables().entrySet())
            entry.getValue().setInitialize(2);
    }
    
    public String toString() {
        String variableInfo = MyUtils.ANSI_CYAN + "\n\tMain" + MyUtils.ANSI_RESET + "\n\tLocal variables:\n";
        for(Map.Entry<String, SymbolVar> entry : getVariables().entrySet()) {
            variableInfo += "\t\t" + entry.getValue() + "\n";
        }

        return variableInfo;
    }

}