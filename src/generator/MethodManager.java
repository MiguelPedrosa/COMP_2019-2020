import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MethodManager {
    private int currentStackSize;
    private int maxStackSize;
    private Boolean isMain;

    private static HashMap<String, Integer> instructions = buildInstructions();

    private List<String> stackTypes;
    List<SymbolVar> locals;

    public MethodManager() {
        this.currentStackSize = 0;
        this.maxStackSize = 0;
        this.locals = new ArrayList<>();
        this.stackTypes = new ArrayList<>();
        this.isMain = false;
    }

    private static HashMap<String, Integer> buildInstructions() {
        HashMap<String, Integer> instructionsAux = new HashMap<>();

        instructionsAux.put("bipush", +1);
        instructionsAux.put("ldc_w", +1);
        instructionsAux.put("aload", +1);
        instructionsAux.put("iload", +1);
        instructionsAux.put("lload", +1);
        instructionsAux.put("getfield", +1);
        instructionsAux.put("putfield", -2);
        instructionsAux.put("invokestatic", +1);
        instructionsAux.put("invokevirtual", +1);

        instructionsAux.put("iadd", +1);
        instructionsAux.put("isub", +1);
        instructionsAux.put("imul", +1);
        instructionsAux.put("idiv", +1);
        instructionsAux.put("iand", +1);
        instructionsAux.put("iconst", +1);
        instructionsAux.put("i2l", +2);

        instructionsAux.put("aaload", +1);
        instructionsAux.put("iaload", +1);
        instructionsAux.put("lcmp", +1);
        
        instructionsAux.put("arraylength", +1);
        instructionsAux.put("new", +1);
        instructionsAux.put("newarray", +1);

        instructionsAux.put("ifgt", -1);
        instructionsAux.put("ifle", -1);
        instructionsAux.put("ireturn", -1);
        instructionsAux.put("areturn", -1);
        instructionsAux.put("lreturn", -1);

        instructionsAux.put("astore", -1);
        instructionsAux.put("istore", -1);
        instructionsAux.put("lstore", -1);
        instructionsAux.put("aastore", -3);
        instructionsAux.put("iastore", -3);

        return instructionsAux;
    }

    private void updateStackType(String instruction, String type) {
        switch (instruction) {
            case "ifgt":
            case "ifle":
            case "ireturn":
            case "areturn":
            case "lreturn":
            case "astore":
            case "istore":
            case "lstore":
                this.stackTypes.remove(this.stackTypes.size() - 1);
                break;
            case "putfield":
                this.stackTypes.remove(this.stackTypes.size() - 1);
                this.stackTypes.remove(this.stackTypes.size() - 1);
                break;
            case "iastore":
            case "aastore":
                this.stackTypes.remove(this.stackTypes.size() - 1);
                this.stackTypes.remove(this.stackTypes.size() - 1);
                this.stackTypes.remove(this.stackTypes.size() - 1);
                break;
            case "bipush":
            case "ldc_w":
            case "arraylength":
            case "aload":
            case "iload":
            case "lload":
            case "getfield":
            case "invokestatic":
            case "invokevirtual":
            case "iadd":
            case "isub":
            case "imul":
            case "idiv":
            case "lcmp":
            case "iaload":
            case "aaload":
            case "iand":
            case "iconst":
            case "new":
            case "newarray":
                this.stackTypes.add(type);
                break;
            case "i2l":
                this.stackTypes.add(type);
                this.stackTypes.add(type);
                break;
            default:
                System.out.println("intruction" + instruction + " not being analised");
                break;
        }
    }

    public void addInstruction(String instruction, String type) {
        if (!instructions.containsKey(instruction)) {
            System.err.printf("Forgot to add instruction %s\n", instruction);
            return;
        }

        final int offset = instructions.get(instruction);
        if (this.currentStackSize + offset < 0) {
            System.err.println("Stack size was smaller than zero");
            return;
        }
        this.updateStackType(instruction, type);
        this.currentStackSize += offset;

        if (this.maxStackSize < this.currentStackSize)
            this.maxStackSize = this.currentStackSize;

    }

    public int getCurrentStackSize() {
        return currentStackSize;
    }

    public Boolean getIsMain() {
        return isMain;
    }

    public void setMain() {
        this.isMain = true;
    }

    public void stackPop(int numPop) {
        if (this.currentStackSize - numPop < 0) {
            System.err.println("Stack size was smaller than zero");
            return;
        }

        this.currentStackSize = this.currentStackSize - numPop;
        for (int i = 0; i < numPop; i++) {
            this.stackTypes.remove(this.stackTypes.size() - 1);
        }
    }

    public int indexOfLocal(String local) {
        for (int i = 0; i < this.locals.size(); i++)
            if (this.locals.get(i).getName().equals(local))
                return i;
        return -1;
    }

    public String typeOfLocal(String local) {
        for (int i = 0; i < this.locals.size(); i++)
            if (this.locals.get(i).getName().equals(local))
                return this.locals.get(i).getType();
        return null;
    }

    public String getLastTypeInStack() {
        if (this.stackTypes.size() == 0)
            return "";
        return this.stackTypes.get(this.stackTypes.size() - 1);
    }

    public String getSimpleArrayType(String ArrayType) {
        String result = "";

        for (int i = 0; i < ArrayType.length(); i++) {
            if (ArrayType.charAt(i) == '[') {
                break;
            } else {
                result += ArrayType.charAt(i);
            }
        }

        return result;
    }

    /**
     * @return the stackTypes
     */
    public List<String> getStackTypes() {
        return stackTypes;
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