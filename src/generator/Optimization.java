/* 
* This class was created to make some optimizations for previous code generation
*/
public class Optimization {

    private static Boolean optimizeO;

    public static String writeEquals(final ASTEquals equalsNode, final int scope, final MethodManager methodManager) {

        if (!optimizeO)
            return null;

        String code = "";
        
        final SimpleNode childLeft = (SimpleNode) equalsNode.jjtGetChild(0);
        SimpleNode childRight = (SimpleNode) equalsNode.jjtGetChild(1);

        if (childLeft instanceof ASTIdentifier && childRight instanceof ASTExpression) {
            String namevar = ((ASTIdentifier) childLeft).getIdentifier();
            final int localIndex = methodManager.indexOfLocal(namevar);
            childRight = (SimpleNode) childRight.jjtGetChild(0);
            if(localIndex != -1){ //faz parte das variaveis locais
                String sign = " ";

                if(childRight instanceof ASTMinus) //controla se é soma ou subtração
                    sign = " -";
                else if(!(childRight instanceof ASTPlus))
                    return null;

                if (childRight.jjtGetChild(0) instanceof ASTIdentifier && childRight.jjtGetChild(1) instanceof ASTLiteral) {
                    String nameVar2 = ((ASTIdentifier) childRight.jjtGetChild(0)).getIdentifier();
                    String literal = ((ASTLiteral) childRight.jjtGetChild(1)).getLiteral();
                    if (nameVar2.equals(namevar)) {
                        if(literal.equals("0"))
                            return "";
                        code = CodeGeneratorUtils.writeToString(code, "iinc " + localIndex + sign + literal + "\n", scope);
                        return code;
                    }
    
                } else if (childRight.jjtGetChild(1) instanceof ASTIdentifier && childRight.jjtGetChild(0) instanceof ASTLiteral) {
                    String nameVar2 = ((ASTIdentifier) childRight.jjtGetChild(1)).getIdentifier();
                    String literal = ((ASTLiteral) childRight.jjtGetChild(0)).getLiteral();
                    if (nameVar2.equals(namevar)) {
                        if(literal.equals("0"))
                            return "";
                        code = CodeGeneratorUtils.writeToString(code, "iinc " + localIndex + sign + literal + "\n", scope);
                        return code;
                    }
                }
            }
        }
        return null;
    }

    public static void setOptimizeO(Boolean optimizeO) {
        Optimization.optimizeO = optimizeO;
    }
}