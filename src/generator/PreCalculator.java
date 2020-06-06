public class PreCalculator {
    public static String getEqualsValue(SimpleNode node, MethodManager methodManager) {
        String value = null;
        final String nodeType = node.getClass().getSimpleName();

        switch (nodeType) {
            case "ASTIdentifier":
                value = getIdentifierValue(node, methodManager);
                break;
            case "ASTLiteral":
                value = getLiteralValue(node, methodManager);
                break;
            case "ASTExpression":
                SimpleNode child = (SimpleNode) node.jjtGetChild(0);
                value = getEqualsValue(child, methodManager);
                break;
            case "ASTAnd":
                value = getAndValue(node, methodManager);
                break;
            case "ASTNot":
                value = getNotValue(node, methodManager);
                break;
            case "ASTLessThan":
                value = getLessThanValue(node, methodManager);
                break;
            case "ASTPlus":
                value = getPlusValue(node, methodManager);
                break;
            case "ASTMinus":
                value = getMinusValue(node, methodManager);
                break;
            case "ASTTimes":
                value = getTimesValue(node, methodManager);
                break;
            case "ASTDividor":
                value = getDividorValue(node, methodManager);
                break;
            case "ASTArrayAccess":
            case "ASTFuncCall":
            case "ASTNew":
            case "ASTLength":
                //value = getEqualsValue(node.jjtGetChild(0), methodManager);
                break;
            default:
                System.out.println("Am I supposed to get in here? " + nodeType);
                break;
        }

        return value;
    }

    private static String getAndValue(SimpleNode andNode, MethodManager methodManager){

        final SimpleNode leftChild = (SimpleNode) andNode.jjtGetChild(0);
        final SimpleNode rightChild = (SimpleNode) andNode.jjtGetChild(1);

        String leftValue = getEqualsValue(leftChild, methodManager);
        String rightValue = getEqualsValue(rightChild, methodManager);

        if(leftValue == null || rightValue == null)
            return null;

        boolean leftBoolean = Boolean.parseBoolean(leftValue);
        boolean rightBoolean = Boolean.parseBoolean(rightValue);

        
        if(leftBoolean && rightBoolean)
            return "true";
        else 
            return "false";
        
    }

    private static String getNotValue(SimpleNode notNode, MethodManager methodManager){

        final SimpleNode child = (SimpleNode) notNode.jjtGetChild(0);

        String childValue = getEqualsValue(child, methodManager);

        if(childValue == null)
            return null;

        boolean childBoolean = Boolean.parseBoolean(childValue);
        
        if(childBoolean)
            return "false";
        else 
            return "true";
        
    }

    private static String getLessThanValue(SimpleNode lessThanNode, MethodManager methodManager){

        final SimpleNode leftChild = (SimpleNode) lessThanNode.jjtGetChild(0);
        final SimpleNode rightChild = (SimpleNode) lessThanNode.jjtGetChild(1);

        String leftValue = getEqualsValue(leftChild, methodManager);
        String rightValue = getEqualsValue(rightChild, methodManager);

        if(leftValue == null || rightValue == null)
            return null;

        int leftInt = Integer.parseInt(leftValue);
        int rightInt = Integer.parseInt(rightValue);

        
        if(leftInt < rightInt)
            return "true";
        else 
            return "false";
    }

    private static String getPlusValue(SimpleNode plusNode, MethodManager methodManager){

        final SimpleNode leftChild = (SimpleNode) plusNode.jjtGetChild(0);
        final SimpleNode rightChild = (SimpleNode) plusNode.jjtGetChild(1);

        String leftValue = getEqualsValue(leftChild, methodManager);
        String rightValue = getEqualsValue(rightChild, methodManager);

        if(leftValue == null || rightValue == null)
            return null;

        int leftInt = Integer.parseInt(leftValue);
        int rightInt = Integer.parseInt(rightValue);

        int total = leftInt + rightInt;

        return String.valueOf(total);
    }

    private static String getMinusValue(SimpleNode minusNode, MethodManager methodManager){

        final SimpleNode leftChild = (SimpleNode) minusNode.jjtGetChild(0);
        final SimpleNode rightChild = (SimpleNode) minusNode.jjtGetChild(1);

        String leftValue = getEqualsValue(leftChild, methodManager);
        String rightValue = getEqualsValue(rightChild, methodManager);

        if(leftValue == null || rightValue == null)
            return null;

        int leftInt = Integer.parseInt(leftValue);
        int rightInt = Integer.parseInt(rightValue);

        int total = leftInt - rightInt;
        
        return String.valueOf(total);
    }

    private static String getTimesValue(SimpleNode timesNode, MethodManager methodManager){

        final SimpleNode leftChild = (SimpleNode) timesNode.jjtGetChild(0);
        final SimpleNode rightChild = (SimpleNode) timesNode.jjtGetChild(1);

        String leftValue = getEqualsValue(leftChild, methodManager);
        String rightValue = getEqualsValue(rightChild, methodManager);

        if(leftValue == null || rightValue == null)
            return null;

        int leftInt = Integer.parseInt(leftValue);
        int rightInt = Integer.parseInt(rightValue);

        int total = leftInt * rightInt;
        
        return String.valueOf(total);
    }

    private static String getDividorValue(SimpleNode dividorNode, MethodManager methodManager){

        final SimpleNode leftChild = (SimpleNode) dividorNode.jjtGetChild(0);
        final SimpleNode rightChild = (SimpleNode) dividorNode.jjtGetChild(1);

        String leftValue = getEqualsValue(leftChild, methodManager);
        String rightValue = getEqualsValue(rightChild, methodManager);

        if(leftValue == null || rightValue == null)
            return null;

        int leftInt = Integer.parseInt(leftValue);
        int rightInt = Integer.parseInt(rightValue);

        int total = leftInt / rightInt;
        
        return String.valueOf(total);
    }

    private static String getIdentifierValue(SimpleNode identifierNode, MethodManager methodManager){
        String identifier = ((ASTIdentifier) identifierNode).getIdentifier();
        
        if (!Optimization.getOptimizeAtribution())
            return null;

        return methodManager.getValueOfLocal(identifier);
    }

    private static String getLiteralValue(SimpleNode literalNode, MethodManager methodManager){
        String literal = ((ASTLiteral) literalNode).getLiteral();

        if(literal.equals("this"))
            return null;

        return literal;
    }
}