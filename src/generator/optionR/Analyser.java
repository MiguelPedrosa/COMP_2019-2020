package generator.optionR;

import java.util.ArrayList;
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


    public Analyser(List<SymbolVar> locals, int targetSize) {
        this.targetSize = targetSize;
        statments = new ArrayList<>();
        varNames = new HashMap<>();
        for(int i = 0; i < locals.length(); i++)
            this.varNames.put(locals.get(i).getName(), i);
    }

    public void setup(ASTMethodDeclaration method) {
        final int numChildren = method.jjtGetNumChildren();
        for(int i = 0; i < numChildren; i++) {
            processStatment(method.jjtGetChild(i), -1, -1);
        }
    }
    public void setup(ASTMainDeclaration method) {
        final int numChildren = method.jjtGetNumChildren();
        for(int i = 0; i < numChildren; i++) {
            processStatment(method.jjtGetChild(i), -1, -1);
        }
    }

    private void processStatment(SimpleNode node, int lastOffsetNodePosition, int offsetNodePosition) {
        final String nodeType = node.getClass().getSimpleName();
        final int nVariables = varNames.size();
        final NodeR nodeR = new NodeR(nVariables);

        switch(nodeType) {
            case "ASTScope":
                analyseScope(node);
            case "ASTEquals":
                analyseEquals((ASTEquals) node, nodeR);
                break;
            case "ASTExpression":
                processExpression(node, nodeR);
                break;
            case "ASTIF":
                processIf((ASTIf) node, nodeR);
                break;
            case "ASTWhile":
                processWhile((ASTWhile) node, nodeR);
                break;
            case "ASTReturn":
                processExpression(node.jjtGetChild(0), nodeR);
                nodeR.setSuccessor1(-1);
                return;
            default:
                System.err.printf("Node not processed: %s\n", nodeType);
            case "ASTVarDeclaration":
                return;
        }

        final int currentNodePosition = statments.size();
        // Check if last node of while
        if(lastOffsetNodePosition == currentNodePosition) {
            nodeR.setSuccessor1(offsetNodePosition);
        } else {
            nodeR.setSuccessor1(currentNodePosition +1);
        }
        statments.add(nodeR);
    }

    private void processExpression(SimpleNode node, NodeR nodeR) {
        final String nodeType = node.getClass().getSimpleName();
        switch(nodeType) {
            //TODO: Handle these 4 cases
            case "ASTFuncCall":
            case "ASTIdentifier":
            case "ASTLiteral":
            case "ASTArrayAccess":

            case "ASTPlus":
            case "ASTMinus":
            case "ASTTimes":
            case "ASTDividor":
            case "ASTNot":
            case "ASTNew":
            case "ASTAnd":
            case "ASTLessThan":
                final int numChildren = node.jjtGetNumChildren();
                for (int i = 0; i < numChildren; i++) {
                    processExpression(node.jjtGetChild(i), nodeR);
                }
                break;
            case "ASTLength":
                break;
            default:
                System.err.printf("Node not processed: %S\n", nodeType);
        }
    }

    private void analyseScope(SimpleNode node) {
        final int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            processStatment(node.jjtGetChild(i), -1);
        }
    }

    private void analyseEquals(ASTEquals equals, NodeR nodeR) {
        final SimpleNode leftNode =  equals.jjtGetChild(0);
        final SimpleNode rightNode =  equals.jjtGetChild(1);

        String varName = null;
        if(leftNode instanceof ASTIdentifier) {
            varName = ((ASTIdentifier) leftNode).getIdentifier();
        } else if(leftNode instanceof ASTArrayAccess) {
            varName = ((ASTIdentifier) leftNode.jjtGetChild(0)).getIdentifier();
            processExpression(leftNode.jjtGetChild(1), nodeR);
        } else {
            System.err.printf("Unexpected node type on left side of equals: %s\n", leftNode);
            return;
        }
        final int varIndex = this.varNames.get(varName);
        nodeR.setDef(varIndex);

        processExpression(rightNode, nodeR);
    }

    private void processIf(ASTIF ifNode, NodeR nodeR) {
        final SimpleNode test =  ifNode.jjtGetChild(0);
        final SimpleNode caseTrue =  ifNode.jjtGetChild(1);
        final SimpleNode caseFalse =  ifNode.jjtGetChild(2);

        processExpression(test, nodeR);
        //Future position of if statment in list
        final int currentNodePosition = statments.size();
        final int caseTrueSize = getTotalNumChildren(caseTrue);
        final int caseFalseSize = getTotalNumChildren(caseFalse);
        final int caseFalseStartPosition = currentNodePosition + caseTrueSize + 1;
        nodeR.setSuccessor2(caseFalseStartPosition);

        processStatment(caseTrue, currentNodePosition + caseTrueSize, currentNodePosition + caseTrueSize + caseFalseSize +1);
        processStatment(caseFalse, -1, -1);
    }

    private void processWhile(ASTWhile whileNode, NodeR nodeR) {
        final SimpleNode test =  whileNode.jjtGetChild(0);
        final SimpleNode whileBody = whileNode.jjtGetChild(1);

        processExpression(test, nodeR);

        //Future position of exit node on while
        final int currentNodePosition = statments.size();
        final int whileBodySize = getTotalNumChildren(whileBody);
        final int caseExitStartPosition = currentNodePosition + whileBodySize + 1;
        nodeR.setSuccessor2(caseExitStartPosition);
        processStatment(whileBody, caseExitStartPosition -1, currentNodePosition);
    }

    private int getTotalNumChildren(SimpleNode node) {
        final int numChildren = node.jjtGetNumChildren();
        int sum = 0;
        for(int i = 0; i < numChildren; i++) {
            final SimpleNode child = node.jjtGetChild(i);
            if(child instanceof ASTScope || 
                child instanceof ASTIf || 
                child instanceof ASTWhile)
                sum += getTotalNumChildren(child);
            else {
                sum++;
            }
        }

        return sum;
    }
                

}