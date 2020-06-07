import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

/**
 * This class existes to implement the flag -r.
 * This changes the amount of registers (or locals)
 * that a function can use up to a given target. If
 * the given target is not enough, the compilation
 * aborts after testing the remainding functions
 * and giving up with an error and explanation
 * message
 */
public class Analyser {

    /**
     * Maximum amount of registers a function can
     * use. If it's impossible, program terminates
     */
    private int targetSize;
    /**
     * All statments from a given method enumerated
     * and can be referenced by an index 
     */
    private List<NodeR> statments;
    /**
     * Mapping from variable name to it's indice.
     * Mostly usefull to know which bit to turn
     * on/off while working with BitSets
     */
    private HashMap<String, Integer> varNames;
    /**
     * Used to store the max number of simultaneous
     * variables used at the same time. This value
     * is calculated during the lifetime analysis to
     * prevent adicional loops.
     */
    private int maxVarIntesections;
    /**
     * K-color graph, to give register indexes to
     * each local variable
     */
    private Graph graph;

    private List<String[]> arguments;

    public Analyser(List<SymbolVar> locals,List<String[]> arguments, int targetSize) {
        if((arguments.size() + 1) > targetSize) {
            System.err.printf("Cannot continue compilation due to insuficient size of locals given. Current function required at least %d, but was given %d\n",
                (arguments.size() + 1),
                targetSize);
            System.exit(1);
        }

        this.arguments = arguments;
        this.targetSize = targetSize;
        this.maxVarIntesections = 0;
        statments = new ArrayList<>();
        varNames = new HashMap<>();
        for(int i = 0; i < locals.size(); i++)
            this.varNames.put(locals.get(i).getName(), i);
    }

    public void setup(ASTMethodDeclaration method) {
        final int numChildren = method.jjtGetNumChildren();
        for(int i = 0; i < numChildren; i++) {
            processStatment((SimpleNode) method.jjtGetChild(i), -1, -1);
        }
        this.in_out_setup();
    }

    public void setup(ASTMainDeclaration method) {
        final int numChildren = method.jjtGetNumChildren();
        for(int i = 0; i < numChildren; i++) {
            processStatment((SimpleNode) method.jjtGetChild(i), -1, -1);
        }
        this.in_out_setup();
    }

    public void run() {
        this.graph = new Graph(this.statments, this.varNames, targetSize);
        graph.colorGraph(this.arguments);
        graph.printNodes();
    }

    public int adjustLocalsIndex(List<SymbolVar> locals) {
        final HashMap<String, GraphNode> nodes = this.graph.getNodes();
/*         if(nodes.get("this")) {
            nodes.get("this").setIndex(0);
        }
        for(SymbolVar local : locals) {
            final String name = local.getName();
            final GraphNode node = nodes.get(name);
            final int color = node.getColor();
            for(SymbolVar symbol : locals) {

            }
            if(color == thisColor) {
                local.setIndex(0);
            }
        } */

        int maxIndex = 0;
        for(SymbolVar local : locals) {
            final String name = local.getName();
            final GraphNode node = nodes.get(name);
            final int index = node.getColor();
            local.setIndex(index);
            if(index > maxIndex) {
                maxIndex = index;
            }
        }
        return maxIndex +1;
    }

    private void in_out_setup() {
        if(statments.size() == 0){
            System.out.println("Statements in method not found");
            return;
        }

        while(this.in_out_iteration());

        if(this.maxVarIntesections > this.targetSize) {
            System.err.printf("Cannot continue compilation due to insuficient size of locals given. Current function required %d, but was given %d\n",
                this.maxVarIntesections,
                this.targetSize);
            System.exit(1);
        }
    }

    private boolean in_out_iteration() {
        final int nVariables = varNames.size();
        boolean flag = false;

        for(int i = statments.size() - 1; i >= 0; i--){
            //set out
            NodeR node = statments.get(i);
            BitSet prevIn = (BitSet) node.getIn().clone();
            BitSet prevOut = (BitSet) node.getOut().clone();
            
            //fix main with no return
            if(node.getSuccessor1() >= statments.size())
                node.setSuccessor1(-1);
            if(node.getSuccessor2() >= statments.size())
                node.setSuccessor2(-1);

            BitSet inS1 = new BitSet(nVariables);
            BitSet inS2 = new BitSet(nVariables);
            BitSet out = new BitSet(nVariables);

            if(node.getSuccessor1() != -1)
                inS1 = statments.get(node.getSuccessor1()).getIn();

            if(node.getSuccessor2() != -1)
                inS2 = statments.get(node.getSuccessor2()).getIn();

            out.or(inS1);
            out.or(inS2);
            node.setOut(out);

            //set in
            BitSet use = (BitSet) node.getUse().clone();
            BitSet def = (BitSet) node.getDef().clone();
            BitSet in = (BitSet) out.clone();
            in.andNot(def);
            in.or(use);
            node.setIn(in);

            // Calculate if new interation, produced more
            // variable intersections
            final int inSize = in.cardinality();
            if(inSize > this.maxVarIntesections) {
                this.maxVarIntesections = inSize;
            }
            final int outSize = out.cardinality();
            if (outSize > this.maxVarIntesections) {
                this.maxVarIntesections = outSize;
            }

            if(!out.equals(prevOut) || !in.equals(prevIn))
                flag = true; 
            
        }

        return flag;
    }

    private void processStatment(SimpleNode node, int lastOffsetNodePosition, int offsetNodePosition) {
        final String nodeType = node.getClass().getSimpleName();
        final int nVariables = varNames.size();
        final NodeR nodeR = new NodeR(nVariables);

        final int currentNodePosition = statments.size();
        // Check if last node of while
        if(lastOffsetNodePosition == currentNodePosition) 
            nodeR.setSuccessor1(offsetNodePosition);
        else
            nodeR.setSuccessor1(currentNodePosition +1);
        

        if(nodeType.equals("ASTVarDeclaration"))
            return;
        
        if(!nodeType.equals("ASTScope"))
            statments.add(nodeR);

        switch(nodeType) {
            case "ASTScope":
                analyseScope(node, lastOffsetNodePosition, offsetNodePosition);
                break;
            case "ASTEquals":
                analyseEquals((ASTEquals) node, nodeR);
                break;
            case "ASTExpression":
                processExpression(node, nodeR);
                break;
            case "ASTIF":
                processIf((ASTIF) node, nodeR, lastOffsetNodePosition, offsetNodePosition);
                break;
            case "ASTWhile":
                processWhile((ASTWhile) node, nodeR, lastOffsetNodePosition, offsetNodePosition);
                break;
            case "ASTReturn":
                processExpression((SimpleNode) node.jjtGetChild(0), nodeR);
                nodeR.setSuccessor1(-1);
                return;
            default:
                System.err.printf("Node not processed: %s\n", nodeType);
            case "ASTVarDeclaration":
                return;
        }
    }

    private void processExpression(SimpleNode node, NodeR nodeR) {
        final String nodeType = node.getClass().getSimpleName();
        switch(nodeType) {
            case "ASTIdentifier":
                analyseIdentifier(node, nodeR);
                break;
            case "ASTLiteral":
                analyseLiteral(node, nodeR);
                break;
            case "ASTArrayAccess":
                analyseArrayAccess(node, nodeR);
                break;
            case "ASTFuncCall":
                processExpression((SimpleNode) node.jjtGetChild(0), nodeR);
                processExpression((SimpleNode) node.jjtGetChild(2), nodeR);
                break;
            case "ASTFuncArgs":
            case "ASTPlus":
            case "ASTMinus":
            case "ASTTimes":
            case "ASTDividor":
            case "ASTNot":
            case "ASTNew":
            case "ASTAnd":
            case "ASTLessThan":
            case "ASTExpression":
                final int numChildren = node.jjtGetNumChildren();
                for (int i = 0; i < numChildren; i++) {
                    processExpression((SimpleNode) node.jjtGetChild(i), nodeR);
                }
                break;
            case "ASTLength":
                break;
            default:
                System.err.printf("Node not processed: %S\n", nodeType);
        }
    }

    private void analyseScope(SimpleNode node, int lastOffsetNodePosition, int offsetNodePosition) {
        final int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            processStatment((SimpleNode) node.jjtGetChild(i), lastOffsetNodePosition, offsetNodePosition);
        }
    }

    private void analyseIdentifier(SimpleNode node, NodeR nodeR) {
        final String identifier = ((ASTIdentifier) node).getIdentifier();
        if(this.varNames.containsKey(identifier)){
            final int varIndex = this.varNames.get(identifier);
            nodeR.setUse(varIndex);
        } else {
            if(this.varNames.containsKey("this")){
                final int varIndex = this.varNames.get("this");
                nodeR.setUse(varIndex);
            }
        }
    }

    private void analyseLiteral(SimpleNode node, NodeR nodeR) {
        final String literal = ((ASTLiteral) node).getLiteral();
        if(literal.equals("this")){
            final int varIndex = this.varNames.get("this");
            nodeR.setUse(varIndex);
        }
    }

    private void analyseArrayAccess(SimpleNode array, NodeR nodeR) {
        final SimpleNode leftNode =  (SimpleNode) array.jjtGetChild(0);
        final SimpleNode rightNode =  (SimpleNode) array.jjtGetChild(1);

        final String varName = ((ASTIdentifier) leftNode).getIdentifier();
        if(this.varNames.containsKey(varName)){
            final int varIndex = this.varNames.get(varName);
            nodeR.setUse(varIndex);
        } else {
            if(this.varNames.containsKey("this")){
                final int varIndex = this.varNames.get("this");
                nodeR.setUse(varIndex);
            }
        }

        processExpression(rightNode, nodeR);
    }

    private void analyseEquals(ASTEquals equals, NodeR nodeR) {
        final SimpleNode leftNode =  (SimpleNode) equals.jjtGetChild(0);
        final SimpleNode rightNode =  (SimpleNode) equals.jjtGetChild(1);

        String varName = null;
        if(leftNode instanceof ASTIdentifier) {
            varName = ((ASTIdentifier) leftNode).getIdentifier();
        } else if(leftNode instanceof ASTArrayAccess) {
            varName = ((ASTIdentifier) (SimpleNode) leftNode.jjtGetChild(0)).getIdentifier();
            processExpression((SimpleNode) leftNode.jjtGetChild(1), nodeR);
        } else {
            System.err.printf("Unexpected node type on left side of equals: %s\n", leftNode);
            return;
        }
        if(this.varNames.containsKey(varName)){
            final int varIndex = this.varNames.get(varName);
            nodeR.setDef(varIndex);
        } else {
            if(this.varNames.containsKey("this")){
                final int varIndex = this.varNames.get("this");
                nodeR.setUse(varIndex);
            }
        }

        processExpression(rightNode, nodeR);
    }

    private void processIf(ASTIF ifNode, NodeR nodeR, int lastOffsetNodePosition, int offsetNodePosition) {
        final SimpleNode test =  (SimpleNode) ifNode.jjtGetChild(0);
        final SimpleNode caseTrue =  (SimpleNode) ifNode.jjtGetChild(1);
        final SimpleNode caseFalse =  (SimpleNode) ifNode.jjtGetChild(2);

        processExpression(test, nodeR);
        //Future position of if statment in list
        final int currentNodePosition = statments.size();
        final int caseTrueSize = getTotalNumChildren(caseTrue);
        final int caseFalseSize = getTotalNumChildren(caseFalse);
        final int caseFalseStartPosition = currentNodePosition + caseTrueSize;
        nodeR.setSuccessor2(caseFalseStartPosition);


        //alterar sucessores caso não tenha nada o if e o else e caso o no já seja um no de salto especial
        if(caseTrueSize == 0){
            if(currentNodePosition + caseTrueSize + caseFalseSize - 1 == lastOffsetNodePosition){
                nodeR.setSuccessor1(offsetNodePosition);
            } else{
                nodeR.setSuccessor1(currentNodePosition + caseTrueSize + caseFalseSize);
            }
        }

        if(caseFalseSize == 0){
            if(currentNodePosition + caseTrueSize + caseFalseSize - 1 == lastOffsetNodePosition){
                nodeR.setSuccessor2(offsetNodePosition);
            }
        }

        //alterar no caso de ser de salto especial
        if(currentNodePosition + caseTrueSize + caseFalseSize - 1 == lastOffsetNodePosition){
            processStatment(caseTrue,
                        caseFalseStartPosition - 1,
                        offsetNodePosition);
            processStatment(caseFalse, currentNodePosition + caseTrueSize + caseFalseSize - 1, offsetNodePosition);
        }
        else{
            processStatment(caseTrue,
                caseFalseStartPosition - 1,
                currentNodePosition + caseTrueSize + caseFalseSize);
            processStatment(caseFalse, -1, -1);
        }
        
    }

    private void processWhile(ASTWhile whileNode, NodeR nodeR, int lastOffsetNodePosition, int offsetNodePosition) {
        final SimpleNode test =  (SimpleNode) whileNode.jjtGetChild(0);
        final SimpleNode whileBody = (SimpleNode) whileNode.jjtGetChild(1);

        processExpression(test, nodeR);

        //Future position of exit node on while
        final int currentNodePosition = statments.size();
        final int whileBodySize = getTotalNumChildren(whileBody);
        final int caseExitStartPosition = currentNodePosition + whileBodySize;
        nodeR.setSuccessor2(caseExitStartPosition);

        if(whileBodySize == 0){
            nodeR.setSuccessor1(currentNodePosition - 1);
        }

        if(caseExitStartPosition - 1 == lastOffsetNodePosition){
            nodeR.setSuccessor2(offsetNodePosition);
        }

        processStatment(whileBody, caseExitStartPosition - 1, currentNodePosition - 1);
    }

    private int getTotalNumChildren(SimpleNode node) {
        final int numChildren = node.jjtGetNumChildren();
        int sum = 0;
        for(int i = 0; i < numChildren; i++) {
            final SimpleNode child = (SimpleNode) node.jjtGetChild(i);
            if(child instanceof ASTScope || 
                child instanceof ASTIF || 
                child instanceof ASTWhile)
                sum += getTotalNumChildren(child);
            else {
                sum++;
            }
        }

        return sum;
    }
    
    public void printNodes() {
        System.out.printf("Index\tSuccessors\tDef\tUse\tIn\tOut\n");
        for(int i = 0; i < statments.size(); i++){
            System.out.printf("%d\t", i);
            statments.get(i).printNode();
        }
    }

}