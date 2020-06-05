/* 
* This class was created to make some optimizations for previous code generation
*/
public class Optimization {

    private static Boolean optimizeO;
    private static Boolean optimizeAtribution;
    private static CodeGenerator codeGenerator;

    public static String writeInteger(int integer, final int scope, final MethodManager methodManager) {
        String code = "";

        if (integer >= 0 && integer <= 5) {
            code = CodeGeneratorUtils.writeToString(code, "iconst_" + integer + "\n", scope);
            methodManager.addInstruction("bipush", "int");
        } else if (integer == -1) {
            code = CodeGeneratorUtils.writeToString(code, "iconst_m1\n", scope);
            methodManager.addInstruction("bipush", "int");
        } else if (integer > 5 && integer <= 127) {
            code = CodeGeneratorUtils.writeToString(code, "bipush " + integer + "\n", scope);
            methodManager.addInstruction("bipush", "int");
        } else if (integer > 127 && integer <= 32767) {
            code = CodeGeneratorUtils.writeToString(code, "sipush " + integer + "\n", scope);
            methodManager.addInstruction("bipush", "int");
        } else {
            code = CodeGeneratorUtils.writeToString(code, "ldc " + integer + "\n", scope);
            methodManager.addInstruction("ldc", "int");
        }

        return code;
    }

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
            if (localIndex != -1) { // faz parte das variaveis locais
                String sign = " ";

                if (childRight instanceof ASTMinus) // controla se é soma ou subtração
                    sign = " -";
                else if (!(childRight instanceof ASTPlus))
                    return null;

                if (childRight.jjtGetChild(0) instanceof ASTIdentifier
                        && childRight.jjtGetChild(1) instanceof ASTLiteral) {
                    String nameVar2 = ((ASTIdentifier) childRight.jjtGetChild(0)).getIdentifier();
                    String literal = ((ASTLiteral) childRight.jjtGetChild(1)).getLiteral();
                    if (nameVar2.equals(namevar)) {
                        if (literal.equals("0"))
                            return "";
                        code = CodeGeneratorUtils.writeToString(code, "iinc " + localIndex + sign + literal + "\n",
                                scope);
                        return code;
                    }

                } else if (childRight.jjtGetChild(1) instanceof ASTIdentifier
                        && childRight.jjtGetChild(0) instanceof ASTLiteral) {
                    String nameVar2 = ((ASTIdentifier) childRight.jjtGetChild(1)).getIdentifier();
                    String literal = ((ASTLiteral) childRight.jjtGetChild(0)).getLiteral();
                    if (nameVar2.equals(namevar)) {
                        if (literal.equals("0"))
                            return "";
                        code = CodeGeneratorUtils.writeToString(code, "iinc " + localIndex + sign + literal + "\n",
                                scope);
                        return code;
                    }
                }
            }
        }
        return null;
    }

    public static String writePlusOperation(final ASTPlus plusNode, final int scope,
            final MethodManager methodManager) {

        if (!optimizeO)
            return null;

        String code = "";

        final SimpleNode childLeft = (SimpleNode) plusNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) plusNode.jjtGetChild(1);

        // caso soma de dois literals
        if (childLeft instanceof ASTLiteral && childRight instanceof ASTLiteral) {
            String literalLeft = ((ASTLiteral) childLeft).getLiteral();
            String literalRight = ((ASTLiteral) childRight).getLiteral();

            int literalLeftInt = Integer.parseInt(literalLeft);
            int literalRightInt = Integer.parseInt(literalRight);

            int total = literalLeftInt + literalRightInt;

            code += Optimization.writeInteger(total, scope, methodManager);

            return code;
        }
        // caso um dos elementos da soma ser 0
        else if (childLeft instanceof ASTLiteral) {
            String literal = ((ASTLiteral) childLeft).getLiteral();
            if (literal.equals("0")) {
                code += codeGenerator.processMethodNodes(childRight, scope, methodManager);
                return code;
            }

        } else if (childRight instanceof ASTLiteral) {
            String literal = ((ASTLiteral) childRight).getLiteral();
            if (literal.equals("0")) {
                code += codeGenerator.processMethodNodes(childLeft, scope, methodManager);
                return code;
            }
        }

        return null;
    }

    public static String writeMinusOperation(final ASTMinus minusNode, final int scope,
            final MethodManager methodManager) {

        if (!optimizeO)
            return null;

        String code = "";

        final SimpleNode childLeft = (SimpleNode) minusNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) minusNode.jjtGetChild(1);

        // caso subtração de dois literals
        if (childLeft instanceof ASTLiteral && childRight instanceof ASTLiteral) {
            String literalLeft = ((ASTLiteral) childLeft).getLiteral();
            String literalRight = ((ASTLiteral) childRight).getLiteral();

            int literalLeftInt = Integer.parseInt(literalLeft);
            int literalRightInt = Integer.parseInt(literalRight);

            int total = literalLeftInt - literalRightInt;

            code += Optimization.writeInteger(total, scope, methodManager);

            return code;
        }
        // caso um dos elementos da subtração ser 0
        else if (childLeft instanceof ASTLiteral) {
            String literal = ((ASTLiteral) childLeft).getLiteral();
            if (literal.equals("0")) {
                code += codeGenerator.processMethodNodes(childRight, scope, methodManager);
                // 0 do lado esquerdo é necessário negar o valor obtido no lado direito
                code = CodeGeneratorUtils.writeToString(code, "ineg\n", scope);
                methodManager.addInstruction("ineg", "int");
                return code;
            }

        } else if (childRight instanceof ASTLiteral) {
            String literal = ((ASTLiteral) childRight).getLiteral();
            if (literal.equals("0")) {
                code += codeGenerator.processMethodNodes(childLeft, scope, methodManager);
                return code;
            }
        }

        return null;
    }

    public static String writeMultiOperation(final ASTTimes multiNode, final int scope,
            final MethodManager methodManager) {

        if (!optimizeO)
            return null;

        String code = "";

        final SimpleNode childLeft = (SimpleNode) multiNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) multiNode.jjtGetChild(1);

        // caso multiplicação de dois literals
        if (childLeft instanceof ASTLiteral && childRight instanceof ASTLiteral) {
            String literalLeft = ((ASTLiteral) childLeft).getLiteral();
            String literalRight = ((ASTLiteral) childRight).getLiteral();

            int literalLeftInt = Integer.parseInt(literalLeft);
            int literalRightInt = Integer.parseInt(literalRight);

            int total = literalLeftInt * literalRightInt;

            code += Optimization.writeInteger(total, scope, methodManager);

            return code;
        } else if (childLeft instanceof ASTLiteral && childRight instanceof ASTIdentifier) { // 8 * i

            String literal = ((ASTLiteral) childLeft).getLiteral();
            int numero = Integer.parseInt(literal);

            if(numero == 0) {
                code += CodeGeneratorUtils.writeToString(code, "iconst_0 \n", scope);
                methodManager.addInstruction("iconst", "int");
                return code;
            } else if(numero == 1) {
                code += codeGenerator.processMethodNodes(childRight, scope, methodManager);
                return code;
            } else if (numero > 1) {
                double check = Math.log((double) numero) / Math.log(2); // verificar se o número a ser multiplicado é
                                                                        // uma
                                                                        // potencia de 2

                if (check % 1 == 0) {
                    code += codeGenerator.processMethodNodes(childRight, scope, methodManager);
                    code += Optimization.writeInteger((int) check, scope, methodManager);
                    code = CodeGeneratorUtils.writeToString(code, "ishl\n", scope);
                    methodManager.stackPop(2);
                    methodManager.addInstruction("ishl", "int");
                    return code;
                }
            }
        } else if (childRight instanceof ASTLiteral && childLeft instanceof ASTIdentifier) { // i * 8

            String literal = ((ASTLiteral) childRight).getLiteral();
            int numero = Integer.parseInt(literal);
            
            if(numero == 0) {
                code += CodeGeneratorUtils.writeToString(code, "iconst_0 \n", scope);
                methodManager.addInstruction("iconst", "int");
                return code;
            } else if(numero == 1) {
                code += codeGenerator.processMethodNodes(childLeft, scope, methodManager);
                return code;
            } else if (numero > 1) {
                double check = Math.log((double) numero) / Math.log(2); // verificar se o número a ser multiplicado é
                                                                        // uma
                                                                        // potencia de 2

                if (check % 1 == 0) {
                    code += codeGenerator.processMethodNodes(childLeft, scope, methodManager);
                    code += Optimization.writeInteger((int) check, scope, methodManager);
                    code = CodeGeneratorUtils.writeToString(code, "ishl\n", scope);
                    methodManager.stackPop(2);
                    methodManager.addInstruction("ishl", "int");
                    return code;
                }
            }
        }

        return null;
    }

    public static String writeDivOperation(final ASTDividor divNode, final int scope,
            final MethodManager methodManager) {

        if (!optimizeO)
            return null;

        String code = "";

        final SimpleNode childLeft = (SimpleNode) divNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) divNode.jjtGetChild(1);

        // caso multiplicação de dois literals
        if (childLeft instanceof ASTLiteral && childRight instanceof ASTLiteral) {
            String literalLeft = ((ASTLiteral) childLeft).getLiteral();
            String literalRight = ((ASTLiteral) childRight).getLiteral();

            int literalLeftInt = Integer.parseInt(literalLeft);
            int literalRightInt = Integer.parseInt(literalRight);

            if (literalRightInt != 0) {
                int total = literalLeftInt / literalRightInt;

                code += Optimization.writeInteger(total, scope, methodManager);

                return code;
            }
        } else if (childRight instanceof ASTLiteral && childLeft instanceof ASTIdentifier) { // i / 8

            String literal = ((ASTLiteral) childRight).getLiteral();
            int numero = Integer.parseInt(literal);

            if (numero == 1){
                code += codeGenerator.processMethodNodes(childLeft, scope, methodManager);
                return code;
            } else if (numero > 1) {
                double check = Math.log((double) numero) / Math.log(2); // verificar se o número a ser dividido é
                                                                        // uma
                                                                        // potencia de 2

                if (check % 1 == 0) {
                    code += codeGenerator.processMethodNodes(childLeft, scope, methodManager);
                    code += Optimization.writeInteger((int) check, scope, methodManager);
                    code = CodeGeneratorUtils.writeToString(code, "ishr\n", scope);
                    methodManager.stackPop(2);
                    methodManager.addInstruction("ishr", "int");
                    return code;
                }
            }
        } else if (childLeft instanceof ASTLiteral && childRight instanceof ASTIdentifier) { // 0 / i

            String literal = ((ASTLiteral) childLeft).getLiteral();
            int numero = Integer.parseInt(literal);

            if (numero == 0){
                code += CodeGeneratorUtils.writeToString(code, "iconst_0 \n", scope);
                methodManager.addInstruction("iconst", "int");
                return code;
            }
        }

        return null;
    }

    public static String writeLessThanOperation(ASTLessThan lessThanNode, int scope, MethodManager methodManager,
            int labelCounter) {

        if (!optimizeO)
            return null;

        String code = "";

        final String lessLabel = "less" + labelCounter;
        final String endLabel = "endLess" + labelCounter;

        final SimpleNode leftChild = (SimpleNode) lessThanNode.jjtGetChild(0);
        final SimpleNode rightChild = (SimpleNode) lessThanNode.jjtGetChild(1);

        if (leftChild instanceof ASTLiteral && rightChild instanceof ASTLiteral) {
            String literalRight = ((ASTLiteral) rightChild).getLiteral();
            String literalLeft = ((ASTLiteral) leftChild).getLiteral();

            int literalLeftInt = Integer.parseInt(literalLeft);
            int literalRightInt = Integer.parseInt(literalRight);

            if (literalLeftInt < literalRightInt)
                code = CodeGeneratorUtils.writeToString(code, "iconst_1\n", scope);
            else
                code = CodeGeneratorUtils.writeToString(code, "iconst_0\n", scope);

            methodManager.addInstruction("iconst", "boolean");

            return code;
        } else if (rightChild instanceof ASTLiteral) {
            String literal = ((ASTLiteral) rightChild).getLiteral();
            if (literal.equals("0")) {
                code += codeGenerator.processMethodNodes(leftChild, scope, methodManager);
                code = CodeGeneratorUtils.writeToString(code, "iflt " + lessLabel + "\n", scope);
                code = CodeGeneratorUtils.writeToString(code, "iconst_0\n", scope);
                code = CodeGeneratorUtils.writeToString(code, "goto " + endLabel + "\n", scope);
                code = CodeGeneratorUtils.writeToString(code, lessLabel + ":\n", 0);
                code = CodeGeneratorUtils.writeToString(code, "iconst_1\n", scope);
                code = CodeGeneratorUtils.writeToString(code, endLabel + ":\n", 0);

                methodManager.addInstruction("iflt", "void");
                methodManager.addInstruction("iconst", "boolean");

                return code;
            }

        } else if (leftChild instanceof ASTLiteral) {
            String literal = ((ASTLiteral) leftChild).getLiteral();
            if (literal.equals("0")) {
                code += codeGenerator.processMethodNodes(rightChild, scope, methodManager);
                code = CodeGeneratorUtils.writeToString(code, "ifgt " + lessLabel + "\n", scope);
                code = CodeGeneratorUtils.writeToString(code, "iconst_0\n", scope);
                code = CodeGeneratorUtils.writeToString(code, "goto " + endLabel + "\n", scope);
                code = CodeGeneratorUtils.writeToString(code, lessLabel + ":\n", 0);
                code = CodeGeneratorUtils.writeToString(code, "iconst_1\n", scope);
                code = CodeGeneratorUtils.writeToString(code, endLabel + ":\n", 0);

                methodManager.addInstruction("ifgt", "void");
                methodManager.addInstruction("iconst", "boolean");

                return code;
            }
        }

        return null;
    }

    public static String writeIf(final ASTIF ifNode, final int scope, final MethodManager methodManager) {

        if (!optimizeO)
            return null;

        String code = "";

        final SimpleNode conditionChild = (SimpleNode) ifNode.jjtGetChild(0);
        final SimpleNode ifScope = (SimpleNode) ifNode.jjtGetChild(1);
        final SimpleNode elseScope = (SimpleNode) ifNode.jjtGetChild(2);

        String equalsValue = Optimization.getEqualsValue(conditionChild, methodManager);
        if (equalsValue != null) {
            if (equalsValue.equals("true")) {
                code += codeGenerator.processMethodNodes(ifScope, scope, methodManager);
                return code;
            } else if (equalsValue.equals("false")) {
                code += codeGenerator.processMethodNodes(elseScope, scope, methodManager);
                return code;
            }
        }

        return null;
    }

    public static String writeWhile(final ASTWhile whileNode, final int scope, final MethodManager methodManager) {

        if (!optimizeO)
            return null;

        String code = "";

        final SimpleNode conditionChild = (SimpleNode) whileNode.jjtGetChild(0);

        String equalsValue = Optimization.getEqualsValue(conditionChild, methodManager);
        if (equalsValue != null) {
            if (equalsValue.equals("true")) {
                return null;
            } else if (equalsValue.equals("false")) {
                return "";
            }
        }

        return null;
    }

    public static String writeIdentifier(final ASTIdentifier identifierNode, final int scope,
            final MethodManager methodManager) {

        if (!optimizeAtribution)
            return null;

        if (!optimizeO)
            return null;

        String code = "";
        final String identifier = ((ASTIdentifier) identifierNode).getIdentifier();

        String valueOfIdentifier = methodManager.getValueOfLocal(identifier);
        if (valueOfIdentifier != null) {
            if (valueOfIdentifier.equals("true")) {
                code = CodeGeneratorUtils.writeToString(code, "iconst_1\n", scope);
                methodManager.addInstruction("iconst", "boolean");
            } else if (valueOfIdentifier.equals("false")) {
                code = CodeGeneratorUtils.writeToString(code, "iconst_0\n", scope);
                methodManager.addInstruction("iconst", "boolean");
            } else if (!valueOfIdentifier.equals("this")) {
                int value = Integer.parseInt(valueOfIdentifier);
                code += Optimization.writeInteger(value, scope, methodManager);
            }
            return code;
        }

        return null;
    }

    public static String getEqualsValue(SimpleNode node, MethodManager mothodManager) {
        if (!optimizeO)
            return null;

        return PreCalculator.getEqualsValue(node, mothodManager);
    }

    public static Boolean getOptimizeO() {
        return optimizeO;
    }

    public static Boolean getOptimizeAtribution() {
        return optimizeAtribution;
    }

    public static void setOptimizeO(Boolean optimizeO) {
        Optimization.optimizeO = optimizeO;
    }

    public static void setCodeGenerator(CodeGenerator codeGenerator) {
        Optimization.codeGenerator = codeGenerator;
    }

    public static void setOptimizeAtribution(Boolean optimizeAtribution) {
        Optimization.optimizeAtribution = optimizeAtribution;
    }
}