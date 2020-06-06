import java.util.BitSet;

public class NodeR {
    /**
     * Any node can only have at most
     * 2 successors since in a conditional
     * branch there are only two options.
     * So just two varibles is enough. 
    */
    private int successor1;
    private int successor2;

    private BitSet use;
    private BitSet def;
    private BitSet in;
    private BitSet out;

    public NodeR(int nVariables) {
        this.use = new BitSet(nVariables);
        this.def = new BitSet(nVariables);
        this.in = new BitSet(nVariables);
        this.out = new BitSet(nVariables);
        this.successor1 = -1;
        this.successor2 = -1;
    }

    public void setSuccessor1(int successor1) {
        this.successor1 = successor1;
    }
    public void setSuccessor2(int successor2) {
        this.successor2 = successor2;
    }
    public int getSuccessor1() {
        return successor1;
    }
    public int getSuccessor2() {
        return successor2;
    }

    public void setUse(int index) {
        this.use.set(index);
    }
    public void setDef(int index) {
        this.def.set(index);
    }
    public void setIn(int index) {
        this.in.set(index);
    }
    public void setOut(int index) {
        this.out.set(index);
    }

    public void clearIn(int index) {
        this.in.clear(index);
    }

    public Boolean getUse(int index) {
        return this.use.get(index);
    }
    public Boolean getDef(int index) {
        return this.def.get(index);
    }
    public Boolean getIn(int index) {
        return this.in.get(index);
    }
    public Boolean getOut(int index) {
        return this.out.get(index);
    }

    public void printNode() {
        System.out.printf("%d,%d\t\t%s\t%s\t%s\t%s\n", successor1, successor2, def, use, in, out);
    }

}