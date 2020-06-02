/* 
* This class was created to make some optimizations for previous code generation
*/
public class Optimization {

    private static Boolean optimizeO;
    private static CodeGenerator codeGenerator;

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

    public static String writePlusOperation(final ASTPlus plusNode, final int scope, final MethodManager methodManager) {

        if (!optimizeO)
            return null;

        String code = "";
        
        final SimpleNode childLeft = (SimpleNode) plusNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) plusNode.jjtGetChild(1);

        //caso soma de dois literals
        if(childLeft instanceof ASTLiteral && childRight instanceof ASTLiteral){
            String literalLeft = ((ASTLiteral) childLeft).getLiteral();
            String literalRight = ((ASTLiteral) childRight).getLiteral();

            int literalLeftInt = Integer.parseInt(literalLeft);
            int literalRightInt = Integer.parseInt(literalRight);

            int total = literalLeftInt + literalRightInt;

            if (total >= 0 && total <= 5) {
                    code = CodeGeneratorUtils.writeToString(code, "iconst_" + total + "\n", scope);
                    methodManager.addInstruction("bipush", "int");
            } else if (total == -1) {
                    code = CodeGeneratorUtils.writeToString(code, "iconst_m1\n", scope);
                    methodManager.addInstruction("bipush", "int");
            } else if (total > 127) {
                    code = CodeGeneratorUtils.writeToString(code, "ldc_w " + total + "\n", scope);
                    methodManager.addInstruction("ldc_w", "long");
            } else {
                    code = CodeGeneratorUtils.writeToString(code, "bipush " + total + "\n", scope);
                    methodManager.addInstruction("bipush", "int");
            }
            return code;
        } 
        //caso um dos elementos da soma ser 0
        else if(childLeft instanceof ASTLiteral){        
            String literal = ((ASTLiteral) childLeft).getLiteral();
            if(literal.equals("0")){
                code += codeGenerator.processMethodNodes(childRight, scope, methodManager);
                return code;
            }

        } else if (childRight instanceof ASTLiteral){
            String literal = ((ASTLiteral) childRight).getLiteral();
            if(literal.equals("0")){
                code += codeGenerator.processMethodNodes(childLeft, scope, methodManager);
                return code;
            }
        }

        return null;
    }

    public static String writeMinusOperation(final ASTMinus minusNode, final int scope, final MethodManager methodManager) {

        if (!optimizeO)
            return null;

        String code = "";
        
        final SimpleNode childLeft = (SimpleNode) minusNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) minusNode.jjtGetChild(1);

        //caso subtração de dois literals
        if(childLeft instanceof ASTLiteral && childRight instanceof ASTLiteral){
            String literalLeft = ((ASTLiteral) childLeft).getLiteral();
            String literalRight = ((ASTLiteral) childRight).getLiteral();

            int literalLeftInt = Integer.parseInt(literalLeft);
            int literalRightInt = Integer.parseInt(literalRight);

            int total = literalLeftInt - literalRightInt;

            if (total >= 0 && total <= 5) {
                    code = CodeGeneratorUtils.writeToString(code, "iconst_" + total + "\n", scope);
                    methodManager.addInstruction("bipush", "int");
            } else if (total == -1) {
                    code = CodeGeneratorUtils.writeToString(code, "iconst_m1\n", scope);
                    methodManager.addInstruction("bipush", "int");
            } else if (total > 127) {
                    code = CodeGeneratorUtils.writeToString(code, "ldc_w " + total + "\n", scope);
                    methodManager.addInstruction("ldc_w", "long");
            } else {
                    code = CodeGeneratorUtils.writeToString(code, "bipush " + total + "\n", scope);
                    methodManager.addInstruction("bipush", "int");
            }
            return code;
        }
        //caso um dos elementos da subtração ser 0
        else if(childLeft instanceof ASTLiteral){        
            String literal = ((ASTLiteral) childLeft).getLiteral();
            if(literal.equals("0")){
                code += codeGenerator.processMethodNodes(childRight, scope, methodManager);
                //0 do lado esquerdo é necessário negar o valor obtido no lado direito
                code = CodeGeneratorUtils.writeToString(code, "ineg\n", scope);
                methodManager.addInstruction("ineg", "int");
                return code;
            }

        } else if (childRight instanceof ASTLiteral){
            String literal = ((ASTLiteral) childRight).getLiteral();
            if(literal.equals("0")){
                code += codeGenerator.processMethodNodes(childLeft, scope, methodManager);
                return code;
            }
        }

        return null;
    }

    public static void setOptimizeO(Boolean optimizeO) {
        Optimization.optimizeO = optimizeO;
    }

    public static void setCodeGenerator(CodeGenerator codeGenerator) {
        Optimization.codeGenerator = codeGenerator;
    }
}