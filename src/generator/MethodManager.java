import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MethodManager {
    private int currentStackSize;    
    private int maxStackSize;
    
    private static HashMap<String, Integer> instructions = buildInstructions();

    private List<String> stackTypes;
    List<SymbolVar> locals;

    public MethodManager() {
        this.currentStackSize = 0;
        this.maxStackSize = 0;
        this.locals = new ArrayList<>();
        this.stackTypes = new ArrayList<>();
    }

    private static HashMap<String, Integer> buildInstructions() {
        HashMap<String, Integer> instructionsAux = new HashMap<>();

        instructionsAux.put("ifeq",      -2);
        instructionsAux.put("bipush",      +1);
        instructionsAux.put("aload",      +1);
        instructionsAux.put("iload",      +1);
        instructionsAux.put("ireturn",      -1);
        instructionsAux.put("areturn",      -1);

        return instructionsAux;
    }

    private void updateStackType(String instruction, String type){
        switch (instruction){
            case "ifeq":
                this.stackTypes.remove(this.stackTypes.size() - 1);
                this.stackTypes.remove(this.stackTypes.size() - 1);
                break;
            case "bipush":
                this.stackTypes.add(type);
                break;
            case "aload":
                this.stackTypes.add(type);
                break;
            case "iload":
                this.stackTypes.add(type);
                break;
            case "ireturn":
                this.stackTypes.remove(this.stackTypes.size() - 1);
                break;
            case "areturn":
                this.stackTypes.remove(this.stackTypes.size() - 1);
                break;
            default:
                break;
        }
    }

    public void addInstruction(String instruction, String type) {
        if(! instruction.contains(instruction)) {
            return;
        }

        final int offset = instructions.get(instruction);
        if(this.currentStackSize + offset < 0) {
            System.err.println("Stack size was smaller than zero");
            return;
        }
        this.updateStackType(instruction, type);
        this.currentStackSize += offset;

        if(this.maxStackSize < this.currentStackSize)
            this.maxStackSize = this.currentStackSize;

        
    }

    public int indexOfLocal(String local){
        for(int i = 0; i< this.locals.size(); i++)
            if(this.locals.get(i).getName().equals(local))
                return i;
        return -1;
    }

    public String typeOfLocal(String local){
        for(int i = 0; i< this.locals.size(); i++)
            if(this.locals.get(i).getName().equals(local))
                return this.locals.get(i).getType();
        return null;
    }

    public String getLastTypeInStack() {
        return this.stackTypes.get(this.stackTypes.size()-1);
    }

    /**
     * @param locals the locals to set
     */
    public void setLocals(List<SymbolVar> locals) {
        this.locals = locals;
    }

    /**
     * @return the maxStackSize
     */
    public int getMaxStackSize() {
        return maxStackSize;
    }

    /**
     * @return the locals
     */
    public List<SymbolVar> getLocals() {
        return locals;
    }

}