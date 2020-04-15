public class SymbolVar {

    private String name;
    private String type;
    private Boolean initialized;

    public SymbolVar(String name, String type) {
        this.name = name;
        this.type = type;
        this.initialized = false;
    }

    public String getName(){
        return name;
    }

    public String getType(){
        return type;
    }

    public Boolean getInitialized(){
        return initialized;
    }

    public void initialize(Boolean initialized){
        this.initialized = initialized;
    }

    public String toString(){
        String variableInfo = "name = " + getName() + "; type = " + getType() + "; initialized = " + getInitialized() + ";";
        return variableInfo;
    }
}