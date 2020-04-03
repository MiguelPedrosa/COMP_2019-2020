public class SymbolVar {

    private String name;
    private String type;
    private String value;

    public SymbolVar(String name, String type) {
        this.name = name;
        this.type = type;
        this.value = null;
    }

    public String getName(){
        return name;
    }

    public String getType(){
        return type;
    }

    public String getValue(){
        return value;
    }

    public String toString(){
        String variableInfo;
        if(getValue() == null)
            variableInfo = "name=" + getName() + ";type=" + getType() + ";value=;";
        else
            variableInfo = "name=" + getName() + ";type=" + getType() + ";value=" + getValue() + ";";
        return variableInfo;
    }
}