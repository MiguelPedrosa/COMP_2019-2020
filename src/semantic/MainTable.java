import java.util.Map;

public class MainTable extends SymbolTable {

    public MainTable(SymbolTable parentTable) {
        super(parentTable);
    }
    
    public String toString() {
        String variableInfo = MyUtils.ANSI_CYAN + "\n\tMain" + MyUtils.ANSI_RESET + "\n\tLocal variables:\n";
        for(Map.Entry<String, SymbolVar> entry : getVariables().entrySet()) {
            variableInfo += "\t\t" + entry.getValue() + "\n";
        }

        return variableInfo;
    }

}