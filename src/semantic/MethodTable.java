import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MethodTable extends SymbolTable {

    private LinkedHashMap<String, String> arguments; //name, type
    private String returnType;
    private String name;


    public MethodTable(SymbolTable parentTable, String name, String returnType, LinkedHashMap<String, String> arguments) {
        super(parentTable);
        this.arguments = arguments;
        this.returnType = returnType;
        this.name = name;

        for (Map.Entry<String, String> entry : arguments.entrySet())
            addVariable(entry.getValue(), entry.getKey());
        
        for (Map.Entry<String, SymbolVar> entry : getVariables().entrySet())
            entry.getValue().setInitialize(true);
        
        
    }

    public String getReturnType(){
        return returnType;
    }

    public String getName(){
        return name;
    }

    public String toString() {
        String variableInfo = MyUtils.ANSI_CYAN + "\n\t" + this.name + MyUtils.ANSI_YELLOW + "|" + MyUtils.ANSI_RESET + " Return Type = " + this.returnType + "; Arguments = { ";

        for (Map.Entry<String, String> entry : arguments.entrySet())
            variableInfo += entry.getValue() + " ";

        variableInfo += "}\n";
        variableInfo += "\tLocal variables:\n";

        for(Map.Entry<String, SymbolVar> entry : getVariables().entrySet()) 
            variableInfo += "\t\t" + entry.getValue() + "\n";
        

        return variableInfo;
    }

}