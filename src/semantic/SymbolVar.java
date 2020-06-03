public class SymbolVar {

    private String name;
    private String type;
    private int initialized;
    private Boolean isArray;
    private String value;

    public SymbolVar(String name, String type) {
        this.name = name;
        this.type = type;
        this.initialized = 0;
        this.value = null;
        this.isArray = this.isArrayType();
    }

    private Boolean isArrayType() {
        for (int i = 0; i < this.type.length(); i++) {
            if (this.type.charAt(i) == '[') {
                return true;
            }
        }
        return false;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getInitialized() {
        return initialized;
    }

    public Boolean getIsArray() {
        return isArray;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setInitialize(int initialized) {
        this.initialized = initialized;
    }

    public String toString() {

        String initialized = null;

        switch (getInitialized()) {
            case 0:
                initialized = "no";
                break;
            case 1:
                initialized = "maybe";
                break;
            case 2:
                initialized = "yes";
                break;
            default:
        }

        String variableInfo = "name = " + getName() + "; type = " + getType() + "; initialized = " + initialized
                + ";";
        return variableInfo;
    }
}