import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MethodTable extends SymbolTable {

    private List<String[]> arguments; //name, type
    private String returnType;
    private String name;


    public MethodTable(SymbolTable parentTable, String name, String returnType, List<String[]> arguments) {
        super(parentTable);
        this.arguments = arguments;
        this.returnType = returnType;
        this.name = name;
    }

    public void initializeAllVariables(){
        for (Map.Entry<String, SymbolVar> entry : getVariables().entrySet())
            entry.getValue().setInitialize(2);
    }

    public String getReturnType(){
        return returnType;
    }

    public String getName(){
        return name;
    }

    public String toString() {
        String variableInfo = MyUtils.ANSI_CYAN + "\n\t" + this.name + MyUtils.ANSI_YELLOW + "|" + MyUtils.ANSI_RESET + " Return Type = " + this.returnType + "; Arguments = { ";

        for (String[] argument: arguments)
            variableInfo += argument[1] + " ";

        variableInfo += "}\n";
        variableInfo += "\tLocal variables:\n";

        for(Map.Entry<String, SymbolVar> entry : getVariables().entrySet()) 
            variableInfo += "\t\t" + entry.getValue() + "\n";
        

        return variableInfo;
    }

}