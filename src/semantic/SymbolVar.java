public class SymbolVar {

    private String name;
    private String type;
    private Object value;

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

    public Object getValue(){
        return value;
    }

    public void setValue(Object value){
        this.value = value;
    }

    public String toString(){
        String variableInfo = "name = " + getName() + "; type = " + getType() + ";";
        if(getValue() != null)
            variableInfo += " value = " + getValue() + ";";
        return variableInfo;
    }
}