import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MethodManager {
    private int currentStackSize;    
    private int maxStackSize;
    
    private static HashMap<String, Integer> instructions = buildInstructions();

    List<String> locals;

    public MethodManager() {
        this.currentStackSize = 0;
        this.maxStackSize = 0;
        this.locals = new ArrayList<>();
    }

    private static HashMap<String, Integer> buildInstructions() {
        HashMap<String, Integer> instructionsAux = new HashMap<>();

        instructionsAux.put("if_acmpeq", -2);
        instructionsAux.put("if_acmpne", -2);
        instructionsAux.put("if_icmpeq", -2);
        instructionsAux.put("if_icmpge", -2);
        instructionsAux.put("if_icmpgt", -2);
        instructionsAux.put("if_icmple", -2);
        instructionsAux.put("if_icmplt", -2);
        instructionsAux.put("if_icmpne", -2);
        instructionsAux.put("ifeq",      -2);
        instructionsAux.put("ifge",      -2);
        instructionsAux.put("ifgt",      -2);
        instructionsAux.put("ifle",      -2);
        instructionsAux.put("iflt",      -2);
        instructionsAux.put("ifne",      -2);
        instructionsAux.put("bipush",      +1);
        instructionsAux.put("aload",      +1);

        return instructionsAux;
    }

    public void addInstruction(String instruction) {
        if(! instruction.contains(instruction)) {
            return;
        }

        final int offset = instructions.get(instruction);
        if(this.currentStackSize + offset < 0) {
            System.err.println("Stack size was smaller than zero");
            return;
        }
        this.currentStackSize += offset;

        if(this.maxStackSize < this.currentStackSize)
            this.maxStackSize = this.currentStackSize;
    }

    public int indexOfLocal(String local){
        return this.locals.indexOf(local);
    }

    /**
     * @param locals the locals to set
     */
    public void setLocals(List<String> locals) {
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
    public List<String> getLocals() {
        return locals;
    }

}