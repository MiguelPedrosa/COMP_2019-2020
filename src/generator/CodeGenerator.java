import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * CodeGenerator
 */
public class CodeGenerator {

    private final SimpleNode rootNode;

    private final SymbolTable symbolTable;

    private int labelCounter = 0;

    public CodeGenerator(final SimpleNode root, final SymbolTable symbolTable, final String fileName,
            final Boolean optimizeO) {
        this.rootNode = root;
        this.symbolTable = symbolTable;

        CodeGeneratorUtils.setFileName(fileName);
        Optimization.setOptimizeO(true);
        Optimization.setCodeGenerator(this);
    }

    /**
     * Method to start the generation of code
     * 
     * @return
     */
    public boolean start() {
        System.out.println("Code generation started...\n\n");
        if (!CodeGeneratorUtils.generateFile()) {
            return false;
        }

        readNodes(rootNode, 0, symbolTable);

        return true;
    }

    private void readNodes(final SimpleNode node, final int scope, final SymbolTable scopeTable) {
        final int numChildren = node.jjtGetNumChildren();

        if (numChildren == 0)
            return;

        for (int i = 0; i < numChildren; i++) {
            final SimpleNode child = (SimpleNode) node.jjtGetChild(i);
            final String nodeType = child.getClass().getSimpleName();

            switch (nodeType) {
                case "ASTClassDeclaration":
                    writeClass((ASTClassDeclaration) child, scope, scopeTable);
                    break;
                case "ASTMethodDeclaration":
                    writeMethod((ASTMethodDeclaration) child, scope, scopeTable);
                    break;
                case "ASTMainDeclaration":
                    writeMain((ASTMainDeclaration) child, scope, scopeTable);
                    break;
                case "ASTVarDeclaration":
                case "ASTImportDeclaration":
                    break;
                default:
                    System.err.println("Unexpected branch execution in readNodes function");
                    break;
            }
        }
    }



    /**
     * Method to write the end of a method to the file
     * 
     */
    private void endMethod(final int scope) {
        CodeGeneratorUtils.writeCode(".end method\n", scope);
    }

    /**
     * Method to write class initializer
     * 
     */
    private void writeInitializer(final int scope) {
        CodeGeneratorUtils.writeCode("\n; standard initializer\n", scope);
        CodeGeneratorUtils.writeCode(".method public <init>()V\n", scope);
        CodeGeneratorUtils.writeCode("aload_0\n", scope + 1);
        CodeGeneratorUtils.writeCode("invokenonvirtual java/lang/Object/<init>()V\n", scope + 1);
        CodeGeneratorUtils.writeCode("return\n", scope + 1);
        endMethod(scope);
    }

    /**
     * Method to write a class into the file
     * 
     * @param classNode
     */
    private void writeClass(final ASTClassDeclaration classNode, final int scope, final SymbolTable scopeTable) {
        // System.out.println("Writing class...");
        final String className = classNode.getClassId();
        CodeGeneratorUtils.writeCode(".class public " + className + "\n", scope);
        CodeGeneratorUtils.writeCode(".super java/lang/Object\n\n", scope);

        // Field
        final int numChildren = classNode.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            final SimpleNode child = (SimpleNode) classNode.jjtGetChild(i);
            final String nodeType = child.getClass().getSimpleName();
            if (nodeType.equals("ASTVarDeclaration")) {
                writeVarDeclaration((ASTVarDeclaration) child, scope, scopeTable);
            }
        }

        writeInitializer(scope);

        readNodes(classNode, scope, scopeTable);
    }

    /**
     * Method to transform a type string into a type for Jasmin
     */
    private String transformType(final String type) {

        String typeString = null;

        switch (type) {
            case "int":
                typeString = "I";
                break;
            case "long":
                typeString = "J";
                break;
            case "float":
                typeString = "F";
                break;
            case "double":
                typeString = "D";
                break;
            case "byte":
                typeString = "B";
                break;
            case "char":
                typeString = "D";
                break;
            case "short":
                typeString = "S";
                break;
            case "reference":
                typeString = "a";
                break;
            case "String":
                typeString = "Ljava/lang/String;";
                break;
            case "void":
                typeString = "V";
                break;
            case "boolean":
                typeString = "Z";
                break;
            case "int[]":
                typeString = "[I";
                break;
            case "long[]":
                typeString = "[J";
                break;
            case "float[]":
                typeString = "[F";
                break;
            case "double[]":
                typeString = "[D";
                break;
            case "byte[]":
                typeString = "[B";
                break;
            case "char[]":
                typeString = "[D";
                break;
            case "short[]":
                typeString = "[S";
                break;
            case "reference[]":
                typeString = "[a";
                break;
            case "String[]":
                typeString = "[Ljava/lang/String;";
                break;
            case "boolean[]":
                typeString = "[Z";
                break;
            default:
                typeString = "L" + type + ";";
                break;
        }

        return typeString;

    }

    private void writeStack(final int numStacks, final int scope) {
        CodeGeneratorUtils.writeCode(".limit stack " + numStacks + "\n", scope);
    }

    private void writeLocals(final int numLocals, final int scope) {
        CodeGeneratorUtils.writeCode(".limit locals " + numLocals + "\n", scope);
    }

    private List<SymbolVar> prepareLocals(final int scope, final String methodKey) {
        if (!this.symbolTable.containsMethod(methodKey) && !methodKey.equals("main")) {
            System.err.println("Can find method " + methodKey + " in symbol table");
            return new ArrayList<>();
        }

        Map<String, SymbolVar> variables;
        final List<SymbolVar> locals = new ArrayList<>();

        if (methodKey.equals("main")) {
            variables = this.symbolTable.getMain().getVariables();
        } else {
            variables = this.symbolTable.getMethodTable(methodKey).getVariables();
            locals.add(new SymbolVar("this", this.symbolTable.getClasseName()));
        }

        // Add variables to locals container
        for (final Map.Entry<String, SymbolVar> entry : variables.entrySet()) {
            locals.add(entry.getValue());
        }

        return locals;
    }

    /**
     * Method to write a Method (or function) into the file
     */
    private void writeMethod(final ASTMethodDeclaration methodNode, final int scope, final SymbolTable scopeTable) {
        final String methodName = methodNode.getMethodName();
        final String methodType = transformType(methodNode.getReturnType());

        final List<String[]> arguments = methodNode.getArguments();
        String argsInJasmin = "";

        for (final String[] argument : arguments) {
            final String argType = transformType(argument[1]);
            argsInJasmin = argsInJasmin.concat(argType);
        }

        CodeGeneratorUtils.writeCode("\n.method public " + methodName + "(" + argsInJasmin + ")" + methodType + "\n", scope);

        final String methodKey = methodNode.getMethodKey();
        final MethodManager methodManager = new MethodManager();
        final List<SymbolVar> locals = prepareLocals(scope + 1, methodKey);

        methodManager.setLocals(locals);

        String code = "";

        final int numChildren = methodNode.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            final SimpleNode child = (SimpleNode) methodNode.jjtGetChild(i);
            code += processMethodNodes(child, scope + 1, methodManager);
        }

        writeStack(methodManager.getMaxStackSize(), scope + 1);
        writeLocals(locals.size(), scope + 1);

        CodeGeneratorUtils.writeCode(code, scope);

        endMethod(scope);
    }

    private void writeMain(final ASTMainDeclaration mainMethodNode, final int scope, final SymbolTable scopeTable) {
        CodeGeneratorUtils.writeCode("\n.method public static main([Ljava/lang/String;)V\n", scope);

        final MethodManager methodManager = new MethodManager();
        methodManager.setMain();
        final List<SymbolVar> locals = prepareLocals(scope + 1, "main");

        methodManager.setLocals(locals);

        String code = "";

        final int numChildren = mainMethodNode.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            final SimpleNode child = (SimpleNode) mainMethodNode.jjtGetChild(i);
            code += processMethodNodes(child, scope + 1, methodManager);
        }

        writeStack(methodManager.getMaxStackSize(), scope + 1);
        writeLocals(locals.size(), scope + 1);
        CodeGeneratorUtils.writeCode(code, scope);
        CodeGeneratorUtils.writeCode("return\n", scope + 1);

        endMethod(scope);
    }

    public String processMethodNodes(final SimpleNode currentNode, final int scope,
            final MethodManager methodManager) {

        String code = "";
        final String nodeType = currentNode.getClass().getSimpleName();

        switch (nodeType) {
            case "ASTVarDeclaration":
                // variable arlready added in locals
                break;
            case "ASTIF":
                code += writeIf((ASTIF) currentNode, scope, methodManager);
                break;
            case "ASTWhile":
                code += writeWhile((ASTWhile) currentNode, scope, methodManager);
                break;
            case "ASTEquals":
                code += writeEquals((ASTEquals) currentNode, scope, methodManager);
                break;
            case "ASTReturn":
                code += writeReturn((ASTReturn) currentNode, scope, methodManager);
                break;
            case "ASTLiteral":
                code += writeLiteral((ASTLiteral) currentNode, scope, methodManager);
                break;
            case "ASTFuncCall":
                code += writeFuncCall((ASTFuncCall) currentNode, scope, methodManager);
                break;
            case "ASTIdentifier":
                code += writeIdentifier((ASTIdentifier) currentNode, scope, methodManager);
                break;
            case "ASTExpression":
                final SimpleNode expressionChild = (SimpleNode) currentNode.jjtGetChild(0);
                code += processMethodNodes(expressionChild, scope, methodManager);
                break;
            case "ASTPlus":
                code += writePlusOperation((ASTPlus) currentNode, scope, methodManager);
                break;
            case "ASTMinus":
                code += writeMinusOperation((ASTMinus) currentNode, scope, methodManager);
                break;
            case "ASTTimes":
                code += writeMultiOperation((ASTTimes) currentNode, scope, methodManager);
                break;
            case "ASTDividor":
                code += writeDivOperation((ASTDividor) currentNode, scope, methodManager);
                break;
            case "ASTNot":
                code += writeNotOperation((ASTNot) currentNode, scope, methodManager);
                break;
            case "ASTNew":
                code += writeNewOperation((ASTNew) currentNode, scope, methodManager);
                break;
            case "ASTAnd":
                code += writeAndOperation((ASTAnd) currentNode, scope, methodManager);
                break;
            case "ASTLessThan":
                code += writeLessThanOperation((ASTLessThan) currentNode, scope, methodManager);
                break;
            case "ASTLength":
                code += writeLengthOperation((ASTLength) currentNode, scope, methodManager);
                break;
            case "ASTArrayAccess":
                code += writeArrayAccess((ASTArrayAccess) currentNode, scope, methodManager);
                break;
            default:
                System.out.println("Node not processed:" + nodeType);
            case "ASTScope":
                final int numChildren = currentNode.jjtGetNumChildren();
                for (int i = 0; i < numChildren; i++) {
                    final SimpleNode child = (SimpleNode) currentNode.jjtGetChild(i);
                    code += processMethodNodes(child, scope, methodManager);
                    int stackSize = methodManager.getCurrentStackSize();
                    // Stack cleaup if values were pushed but not used
                    int j = 0;
                    if (methodManager.getIsMain())
                        j = 1;
                    for (; j < stackSize; j++) {
                        code = CodeGeneratorUtils.writeToString(code, "pop\n", scope);
                        methodManager.stackPop(1);
                    }
                }
                break;
        }

        return code;
    }

    private void writeVarDeclaration(final ASTVarDeclaration varDecNode, final int scope,
            final SymbolTable scopeTable) {
        final String tableType = scopeTable.getClass().getSimpleName();
        switch (tableType) {
            case "SymbolTable":
                writeClassField(varDecNode, scope, scopeTable);
                break;
            default:
                break;
        }
    }

    private void writeClassField(final ASTVarDeclaration varDecNode, final int scope, final SymbolTable scopeTable) {
        final String type = transformType(varDecNode.getType());
        String identifier = varDecNode.getVarId();
        identifier = getJasminIdentifier(identifier);
        // Field is always declared as private because that is the java default
        CodeGeneratorUtils.writeCode(".field private " + identifier + " " + type + "\n", scope);
    }

    private String writeFuncCall(final ASTFuncCall funcCallNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final SimpleNode firstChild = (SimpleNode) funcCallNode.jjtGetChild(0);
        final SimpleNode method = (SimpleNode) funcCallNode.jjtGetChild(1);
        final SimpleNode arguments = (SimpleNode) funcCallNode.jjtGetChild(2);

        if (firstChild instanceof ASTIdentifier) {
            List<String> list = writeFuncCallIdentifier((ASTIdentifier) firstChild, scope, methodManager);

            // static
            if (list.size() == 2) {
                String codeLine;
                String className = list.get(0);
                String methodName = ((ASTIdentifier) method).getIdentifier();
                String args = "";
                String returnType;

                code += this.writeArgs((ASTFuncArgs) arguments, scope, methodManager);

                int numberArgs = arguments.jjtGetNumChildren();
                List<String> argsTypes = new ArrayList<>();
                for (int i = (methodManager.getStackTypes().size() - numberArgs); i < methodManager.getStackTypes()
                        .size(); i++) {
                    argsTypes.add(methodManager.getStackTypes().get(i));
                }

                for (int i = 0; i < argsTypes.size(); i++)
                    args += this.transformType(argsTypes.get(i));

                String signature = methodName;
                for (String arg : argsTypes) {
                    signature += ";" + arg;
                }
                if (!this.symbolTable.containsMethod(signature) && !this.symbolTable.getImports().containsKey(className)
                        && this.symbolTable.getClasseName().equals(className)) {
                    className = this.symbolTable.getClassExtendsName();
                }

                returnType = this.getFuncReturnType(argsTypes, className, methodName, true);

                codeLine = "invokestatic " + className + "/" + methodName + "(" + args + ")"
                        + this.transformType(returnType) + "\n";
                code = CodeGeneratorUtils.writeToString(code, codeLine, scope);

                methodManager.stackPop(numberArgs);
                if (!returnType.equals("void"))
                    methodManager.addInstruction("invokestatic", returnType);

            }
            // variable
            else if (list.size() == 1) {
                code += list.get(0);
                String codeLine;
                String className = methodManager.getLastTypeInStack();
                String methodName = ((ASTIdentifier) method).getIdentifier();
                String args = "";
                String returnType;

                code += this.writeArgs((ASTFuncArgs) arguments, scope, methodManager);

                int numberArgs = arguments.jjtGetNumChildren();
                List<String> argsTypes = new ArrayList<>();
                for (int i = (methodManager.getStackTypes().size() - numberArgs); i < methodManager.getStackTypes()
                        .size(); i++) {
                    argsTypes.add(methodManager.getStackTypes().get(i));
                }

                for (int i = 0; i < argsTypes.size(); i++)
                    args += this.transformType(argsTypes.get(i));

                String signature = methodName;
                for (String arg : argsTypes) {
                    signature += ";" + arg;
                }
                if (!this.symbolTable.containsMethod(signature) && !this.symbolTable.getImports().containsKey(className)
                        && this.symbolTable.getClasseName().equals(className)) {
                    className = this.symbolTable.getClassExtendsName();
                }

                returnType = this.getFuncReturnType(argsTypes, className, methodName, false);

                codeLine = "invokevirtual " + className + "/" + methodName + "(" + args + ")"
                        + this.transformType(returnType) + "\n";
                code = CodeGeneratorUtils.writeToString(code, codeLine, scope);

                methodManager.stackPop(numberArgs + 1);
                if (!returnType.equals("void"))
                    methodManager.addInstruction("invokevirtual", returnType);

            }
        } else {
            code += processMethodNodes(firstChild, scope, methodManager);
            String codeLine;
            String className = methodManager.getLastTypeInStack();
            String methodName = ((ASTIdentifier) method).getIdentifier();
            String args = "";
            String returnType;

            code += this.writeArgs((ASTFuncArgs) arguments, scope, methodManager);

            int numberArgs = arguments.jjtGetNumChildren();
            List<String> argsTypes = new ArrayList<>();
            for (int i = (methodManager.getStackTypes().size() - numberArgs); i < methodManager.getStackTypes()
                    .size(); i++) {
                argsTypes.add(methodManager.getStackTypes().get(i));
            }

            for (int i = 0; i < argsTypes.size(); i++)
                args += this.transformType(argsTypes.get(i));

            String signature = methodName;
            for (String arg : argsTypes) {
                signature += ";" + arg;
            }
            if (!this.symbolTable.containsMethod(signature) && !this.symbolTable.getImports().containsKey(className)
                    && this.symbolTable.getClasseName().equals(className)) {
                className = this.symbolTable.getClassExtendsName();
            }

            returnType = this.getFuncReturnType(argsTypes, className, methodName, false);

            codeLine = "invokevirtual " + className + "/" + methodName + "(" + args + ")"
                    + this.transformType(returnType) + "\n";
            code = CodeGeneratorUtils.writeToString(code, codeLine, scope);

            methodManager.stackPop(numberArgs + 1);
            if (!returnType.equals("void"))
                methodManager.addInstruction("invokevirtual", returnType);
        }

        return code;
    }

    /**
     * Method to write "identifier" to the file when its called in funcCall
     */
    private String getFuncReturnType(List<String> argsTypes, String className, String methodName, boolean isStatic) {
        String signature = methodName;
        for (String arg : argsTypes) {
            signature += ";" + arg;
        }

        if (this.symbolTable.containsMethod(signature)) {
            return this.symbolTable.getMethodReturn(signature);
        }

        if (this.symbolTable.getImports().containsKey(className)) {
            if (isStatic) {
                return this.symbolTable.getImports().get(className).getStaticMethodType(signature);
            } else {
                return this.symbolTable.getImports().get(className).getMethodType(signature);
            }
        }

        if (this.symbolTable.getClasseName().equals(className)) {
            className = this.symbolTable.getClassExtendsName();

            if (this.symbolTable.getImports().containsKey(className)) {
                if (isStatic) {
                    return this.symbolTable.getImports().get(className).getStaticMethodType(signature);
                } else {
                    return this.symbolTable.getImports().get(className).getMethodType(signature);
                }
            }
        }

        System.err.printf("Unexcepted branch execution on getFuncReturnType\n");
        return null;
    }

    /**
     * Method to write "identifier" to the file when its called in funcCall
     */
    private String writeArgs(ASTFuncArgs argsNode, int scope, MethodManager methodManager) {
        String code = "";

        ArrayList<String> argsTypes = new ArrayList<String>();

        for (int i = 0; i < argsNode.jjtGetNumChildren(); i++) {
            SimpleNode childNode = (SimpleNode) argsNode.jjtGetChild(i);
            code += this.processMethodNodes(childNode, scope, methodManager);
        }

        return code;
    }

    /**
     * Method to write "identifier" to the file when its called in funcCall
     */
    private List<String> writeFuncCallIdentifier(final ASTIdentifier identifierNode, final int scope,
            final MethodManager methodManager) {
        final List<String> list = new ArrayList<String>();

        String code = "";

        final String identifier = identifierNode.getIdentifier();

        final int localIndex = methodManager.indexOfLocal(identifier);
        String type = methodManager.typeOfLocal(identifier);

        if (type == null) {
            if (this.symbolTable.containsVariable(identifier)) {
                // TODO: check if group agree with code
                // TODO: when array change for arrays instead of fields? prof example
                type = this.symbolTable.getVariableType(identifier);

                final String filteredIdentifier = getJasminIdentifier(identifier);

                code = CodeGeneratorUtils.writeToString(code, "aload_0 \n", scope);
                methodManager.addInstruction("aload", this.symbolTable.getClasseName());

                code = CodeGeneratorUtils.writeToString(code, "getfield " + this.symbolTable.getClasseName() + "/" + filteredIdentifier
                        + " " + transformType(type) + "\n", scope);
                methodManager.stackPop(1);
                methodManager.addInstruction("getfield", type);

                list.add(code);
                return list;
            } else {
                list.add(identifier);
                list.add(identifier);
                return list;
            }

        }
        // Optimization :)
        String indexForInstruction = " ";
        if (localIndex >= 0 && localIndex <= 3)
            indexForInstruction = "_";
        indexForInstruction += localIndex;

        if (type.equals("int") || type.equals("boolean")) {
            code = CodeGeneratorUtils.writeToString(code, "iload" + indexForInstruction + "\n", scope);
            methodManager.addInstruction("iload", type);
        } else if (type.equals("long")) {
            code = CodeGeneratorUtils.writeToString(code, "lload" + indexForInstruction + "\n", scope);
            methodManager.addInstruction("lload", type);
        } else {
            code = CodeGeneratorUtils.writeToString(code, "aload" + indexForInstruction + "\n", scope);
            methodManager.addInstruction("aload", type);
        }

        list.add(code);
        return list;
    }

    /**
     * Method to write "identifier" to the file
     */
    private String writeReturn(final ASTReturn returnNode, final int scope, final MethodManager methodManager) {

        String code = "";

        final SimpleNode returnExpression = (SimpleNode) returnNode.jjtGetChild(0);

        code += processMethodNodes(returnExpression, scope, methodManager);

        if (methodManager.getLastTypeInStack().equals("int") || methodManager.getLastTypeInStack().equals("boolean")) {
            code = CodeGeneratorUtils.writeToString(code, "ireturn\n", scope);
        } else if (methodManager.getLastTypeInStack().equals("long")) {
            code = CodeGeneratorUtils.writeToString(code, "lreturn\n", scope);
        } else {
            code = CodeGeneratorUtils.writeToString(code, "areturn\n", scope);
        }

        return code;
    }

    private String writeArrayAccess(final ASTArrayAccess arrayNode, final int scope,
            final MethodManager methodManager) {

        String code = "";

        final SimpleNode arrayNodeName = (SimpleNode) arrayNode.jjtGetChild(0);
        final SimpleNode arrayNodeIndex = (SimpleNode) arrayNode.jjtGetChild(1);

        code += processMethodNodes(arrayNodeName, scope, methodManager);
        final String arrayType = methodManager.getLastTypeInStack();
        final String simpleArrayType = methodManager.getSimpleArrayType(arrayType);
        code += processMethodNodes(arrayNodeIndex, scope, methodManager);

        if (simpleArrayType.equals("int") || simpleArrayType.equals("boolean")) {
            code = CodeGeneratorUtils.writeToString(code, "iaload\n", scope);
            methodManager.stackPop(2);
            methodManager.addInstruction("iaload", "int");
        } else {
            code = CodeGeneratorUtils.writeToString(code, "aaload\n", scope);
            methodManager.stackPop(2);
            methodManager.addInstruction("aaload", simpleArrayType);
        }

        return code;
    }

    /**
     * Method to write "identifier" to the file
     */
    private String writeIdentifier(final ASTIdentifier identifierNode, final int scope,
            final MethodManager methodManager) {
        String code = "";

        final String identifier = identifierNode.getIdentifier();

        final int localIndex = methodManager.indexOfLocal(identifier);
        String type = methodManager.typeOfLocal(identifier);

        if (type == null) {

            // TODO: check if group agree with code
            // TODO: when array change for arrays instead of fields? prof example
            type = this.symbolTable.getVariableType(identifier);

            String filteredIdentifier = getJasminIdentifier(identifier);

            code = CodeGeneratorUtils.writeToString(code, "aload_0 \n", scope);
            methodManager.addInstruction("aload", this.symbolTable.getClasseName());

            code = CodeGeneratorUtils.writeToString(code, "getfield " + this.symbolTable.getClasseName() + "/" + filteredIdentifier + " "
                    + transformType(type) + "\n", scope);
            methodManager.stackPop(1);
            methodManager.addInstruction("getfield", type);

            return code;
        }

        // Optimization :)
        String indexForInstruction = " ";
        if (localIndex >= 0 && localIndex <= 3)
            indexForInstruction = "_";
        indexForInstruction += localIndex;

        if (type.equals("int") || type.equals("boolean")) {
            code = CodeGeneratorUtils.writeToString(code, "iload" + indexForInstruction + "\n", scope);
            methodManager.addInstruction("iload", type);
        } else if (type.equals("long")) {
            code = CodeGeneratorUtils.writeToString(code, "lload" + indexForInstruction + "\n", scope);
            methodManager.addInstruction("lload", type);
        } else {
            code = CodeGeneratorUtils.writeToString(code, "aload" + indexForInstruction + "\n", scope);
            methodManager.addInstruction("aload", type);
        }

        return code;
    }

    /**
     * Method to write equals to the file
     */
    private String writeEquals(final ASTEquals equalsNode, final int scope, final MethodManager methodManager) {
        String code = "";

        String optimizedCode = Optimization.writeEquals(equalsNode, scope, methodManager);
        if(optimizedCode != null){
            code = optimizedCode;
            return code;
        }

        final SimpleNode childLeft = (SimpleNode) equalsNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) equalsNode.jjtGetChild(1);

        // Store value in non-array variable
        if (childLeft instanceof ASTIdentifier) {

            final String identifier = ((ASTIdentifier) childLeft).getIdentifier();
            final int localIndex = methodManager.indexOfLocal(identifier);
            String type = methodManager.typeOfLocal(identifier);

            if (type == null) {

                type = this.symbolTable.getVariableType(identifier);
                String filteredIdentifier = getJasminIdentifier(identifier);

                code = CodeGeneratorUtils.writeToString(code, "aload_0 \n", scope);
                methodManager.addInstruction("aload", this.symbolTable.getClasseName());

                code += processMethodNodes(childRight, scope, methodManager);

                code = CodeGeneratorUtils.writeToString(code, "putfield " + this.symbolTable.getClasseName() + "/" + filteredIdentifier
                        + " " + transformType(type) + "\n", scope);
                methodManager.addInstruction("putfield", type);

                return code;
            }

            String equalsValue = Optimization.getEqualsValue(childRight, methodManager);
            if(equalsValue != null){
                if(equalsValue.equals("true")) {
                    code = CodeGeneratorUtils.writeToString(code, "iconst_1 \n", scope);
                    methodManager.addInstruction("iconst", "boolean");
                    code = CodeGeneratorUtils.writeToString(code, "istore " + localIndex + "\n", scope);
                    methodManager.addInstruction("istore", type);
                }
                else if(equalsValue.equals("false")) {
                    code = CodeGeneratorUtils.writeToString(code, "iconst_0 \n", scope);
                    methodManager.addInstruction("iconst", "boolean");
                    code = CodeGeneratorUtils.writeToString(code, "istore " + localIndex + "\n", scope);
                    methodManager.addInstruction("istore", type);
                }
                else {
                    int value = Integer.parseInt(equalsValue);
                    code += Optimization.writeInteger(value, scope, methodManager);
                    code = CodeGeneratorUtils.writeToString(code, "istore " + localIndex + "\n", scope);
                    methodManager.addInstruction("istore", type);
                }

                methodManager.setValueOfLocal(identifier, equalsValue);
                
                return code;
            }

            code += processMethodNodes(childRight, scope, methodManager);
            if (type.equals("int") || type.equals("boolean")) {
                code = CodeGeneratorUtils.writeToString(code, "istore " + localIndex + "\n", scope);
                methodManager.addInstruction("istore", type);
            } else if (type.equals("long")) {
                code = CodeGeneratorUtils.writeToString(code, "lstore" + localIndex + "\n", scope);
                methodManager.addInstruction("lstore", type);
            } else {
                code = CodeGeneratorUtils.writeToString(code, "astore " + localIndex + "\n", scope);
                methodManager.addInstruction("astore", type);
            }
        } else { // Store value in array variable
            final SimpleNode arrayName = (SimpleNode) childLeft.jjtGetChild(0);
            final SimpleNode arrayIndex = (SimpleNode) childLeft.jjtGetChild(1);

            code += processMethodNodes(arrayName, scope, methodManager);
            final String arrayType = methodManager.getLastTypeInStack();
            final String simpleArrayType = methodManager.getSimpleArrayType(arrayType);
            code += processMethodNodes(arrayIndex, scope, methodManager);
            // In arrays, new value is at the top of the stack
            code += processMethodNodes(childRight, scope, methodManager);

            if (simpleArrayType.equals("int") || simpleArrayType.equals("boolean")) {
                code = CodeGeneratorUtils.writeToString(code, "iastore " + "\n", scope);
                methodManager.addInstruction("iastore", simpleArrayType);
            } else {
                code = CodeGeneratorUtils.writeToString(code, "aastore " + "\n", scope);
                methodManager.addInstruction("aastore", simpleArrayType);
            }
        }

        return code;
    }

    /**
     * Method to write "literal" to the file
     */
    private String writeLiteral(final ASTLiteral literalNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final String literal = literalNode.getLiteral();
        int stackLiteral;
        switch (literal) {
            case "true":
                stackLiteral = 1;
                code = CodeGeneratorUtils.writeToString(code, "bipush " + stackLiteral + "\n", scope);
                methodManager.addInstruction("bipush", "boolean");
                break;
            case "false":
                stackLiteral = 0;
                code = CodeGeneratorUtils.writeToString(code, "bipush " + stackLiteral + "\n", scope);
                methodManager.addInstruction("bipush", "boolean");
                break;
            case "this":
                code = CodeGeneratorUtils.writeToString(code, "aload_0 \n", scope);
                methodManager.addInstruction("aload", this.symbolTable.getClasseName());
                break;
            default:
                stackLiteral = Integer.parseInt(literal);
                code += Optimization.writeInteger(stackLiteral, scope, methodManager);
                break;
        }

        return code;
    }

    /**
     * Method to write "if" to the file
     */
    private String writeIf(final ASTIF ifNode, final int scope, final MethodManager methodManager) {

        String code = "";
        final int label = this.labelCounter;
        this.labelCounter++;

        final SimpleNode conditionChild = (SimpleNode) ifNode.jjtGetChild(0);
        final SimpleNode ifScope = (SimpleNode) ifNode.jjtGetChild(1);
        final SimpleNode elseScope = (SimpleNode) ifNode.jjtGetChild(2);

        code += processMethodNodes(conditionChild, scope, methodManager);
        code = CodeGeneratorUtils.writeToString(code, "ifgt correct" + label + "\n", scope);
        methodManager.addInstruction("ifgt", "");
        code += processMethodNodes(elseScope, scope, methodManager);
        code = CodeGeneratorUtils.writeToString(code, "goto endIf" + label + "\n", scope);
        code = CodeGeneratorUtils.writeToString(code, "correct" + label + ":\n", 0);
        code += processMethodNodes(ifScope, scope, methodManager);
        code = CodeGeneratorUtils.writeToString(code, "endIf" + label + ":\n", 0);

        return code;
    }

    /**
     * Method to write "while" to the file
     */
    private String writeWhile(final ASTWhile whileNode, final int scope, final MethodManager methodManager) {

        String code = "";

        final int label = this.labelCounter;
        this.labelCounter++;

        final SimpleNode conditionChild = (SimpleNode) whileNode.jjtGetChild(0);
        final SimpleNode scopeChild = (SimpleNode) whileNode.jjtGetChild(1);

        code = CodeGeneratorUtils.writeToString(code, "while" + label + ":\n", 0);
        code += processMethodNodes(conditionChild, scope, methodManager);
        code = CodeGeneratorUtils.writeToString(code, "ifle endWhile" + label + "\n", scope);
        methodManager.addInstruction("ifle", "");
        code += processMethodNodes(scopeChild, scope, methodManager);
        code = CodeGeneratorUtils.writeToString(code, "goto while" + label + "\n", scope);
        code = CodeGeneratorUtils.writeToString(code, "endWhile" + label + ":\n", 0);

        return code;
    }

    /**
     * Method to write "addition" (+) operation to the file
     */
    private String writePlusOperation(final ASTPlus plusNode, final int scope, final MethodManager methodManager) {
        String code = "";

        String optimizedCode = Optimization.writePlusOperation(plusNode, scope, methodManager);
        if(optimizedCode != null){
            code = optimizedCode;
            return code;
        }

        final SimpleNode childLeft = (SimpleNode) plusNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) plusNode.jjtGetChild(1);

        code += processMethodNodes(childLeft, scope, methodManager);
        code += processMethodNodes(childRight, scope, methodManager);

        code = CodeGeneratorUtils.writeToString(code, "iadd\n", scope);

        methodManager.stackPop(2);
        methodManager.addInstruction("iadd", "int");

        return code;
    }

    /**
     * Method to write "subtraction" (-) operation to the file
     */
    private String writeMinusOperation(final ASTMinus minusNode, final int scope, final MethodManager methodManager) {
        String code = "";

        String optimizedCode = Optimization.writeMinusOperation(minusNode, scope, methodManager);
        if(optimizedCode != null){
            code = optimizedCode;
            return code;
        }

        final SimpleNode childLeft = (SimpleNode) minusNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) minusNode.jjtGetChild(1);

        code += processMethodNodes(childLeft, scope, methodManager);
        code += processMethodNodes(childRight, scope, methodManager);

        code = CodeGeneratorUtils.writeToString(code, "isub\n", scope);

        methodManager.stackPop(2);
        methodManager.addInstruction("isub", "int");

        return code;
    }

    /**
     * Method to write "multiplication" (*) operation to the file
     */
    private String writeMultiOperation(final ASTTimes multiNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final SimpleNode childLeft = (SimpleNode) multiNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) multiNode.jjtGetChild(1);

        code += processMethodNodes(childLeft, scope, methodManager);
        code += processMethodNodes(childRight, scope, methodManager);

        code = CodeGeneratorUtils.writeToString(code, "imul\n", scope);

        methodManager.stackPop(2);
        methodManager.addInstruction("imul", "int");

        return code;
    }

    /**
     * Method to write "division" (/) operation to the file
     */
    private String writeDivOperation(final ASTDividor divNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final SimpleNode childLeft = (SimpleNode) divNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) divNode.jjtGetChild(1);

        code += processMethodNodes(childLeft, scope, methodManager);
        code += processMethodNodes(childRight, scope, methodManager);

        code = CodeGeneratorUtils.writeToString(code, "idiv\n", scope);

        methodManager.stackPop(2);
        methodManager.addInstruction("idiv", "int");

        return code;
    }

    /**
     * Method to write "less than" (<) operation to the file
     */
    private String writeLessThanOperation(ASTLessThan lessThanNode, int scope, MethodManager methodManager) {
        String code = "";

        final String lessLabel = "less" + this.labelCounter;
        final String endLabel = "endLess" + this.labelCounter;

        String optimizedCode = Optimization.writeLessThanOperation(lessThanNode, scope, methodManager, this.labelCounter);
        if(optimizedCode != null){
            code = optimizedCode;
            return code;
        }
        this.labelCounter++;

        final SimpleNode leftChild = (SimpleNode) lessThanNode.jjtGetChild(0);
        final SimpleNode rightChild = (SimpleNode) lessThanNode.jjtGetChild(1);

        code += processMethodNodes(leftChild, scope, methodManager);
        code += processMethodNodes(rightChild, scope, methodManager);

        code = CodeGeneratorUtils.writeToString(code, "if_icmplt " + lessLabel + "\n", scope);
        code = CodeGeneratorUtils.writeToString(code, "iconst_0\n", scope);
        code = CodeGeneratorUtils.writeToString(code, "goto " + endLabel + "\n", scope);
        code = CodeGeneratorUtils.writeToString(code, lessLabel + ":\n", 0);
        code = CodeGeneratorUtils.writeToString(code, "iconst_1\n", scope);
        code = CodeGeneratorUtils.writeToString(code, endLabel + ":\n", 0);

        methodManager.addInstruction("if_icmplt", "void");
        methodManager.addInstruction("iconst", "boolean");

        return code;
    }

    /**
     * Method to write "and" (&&) operation to the file
     */
    private String writeAndOperation(final ASTAnd andNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final SimpleNode childLeft = (SimpleNode) andNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) andNode.jjtGetChild(1);

        code += processMethodNodes(childLeft, scope, methodManager);
        code += processMethodNodes(childRight, scope, methodManager);

        code = CodeGeneratorUtils.writeToString(code, "iand\n", scope);

        methodManager.stackPop(2);
        methodManager.addInstruction("iand", "boolean");

        return code;
    }

    /**
     * Method to write "not" (!) operation to the file
     */
    private String writeNotOperation(final ASTNot notNode, final int scope, final MethodManager methodManager) {
        String code = "";
        final String notLabel = "not" + this.labelCounter;
        final String endLabel = "endNot" + this.labelCounter;
        this.labelCounter++;

        final SimpleNode child = (SimpleNode) notNode.jjtGetChild(0);

        code += processMethodNodes(child, scope, methodManager);
        code = CodeGeneratorUtils.writeToString(code, "ifgt " + notLabel + "\n", scope);
        code = CodeGeneratorUtils.writeToString(code, "iconst_1\n", scope);
        code = CodeGeneratorUtils.writeToString(code, "goto " + endLabel + "\n", scope);
        code = CodeGeneratorUtils.writeToString(code, notLabel + ":\n", 0);
        code = CodeGeneratorUtils.writeToString(code, "iconst_0\n", scope);
        code = CodeGeneratorUtils.writeToString(code, endLabel + ":\n", 0);

        methodManager.stackPop(1);
        methodManager.addInstruction("iconst", "boolean");

        return code;
    }

    /**
     * Method to write "length" array operator to the file
     */
    private String writeLengthOperation(final ASTLength lengthNode, final int scope,
            final MethodManager methodManager) {
        String code = "";

        final SimpleNode child = (SimpleNode) lengthNode.jjtGetChild(0);

        code += processMethodNodes(child, scope, methodManager);

        code = CodeGeneratorUtils.writeToString(code, "arraylength\n", scope);

        methodManager.stackPop(1);
        methodManager.addInstruction("arraylength", "int");

        return code;
    }

    /**
     * Method to write "new" operation to the file
     */
    private String writeNewOperation(final ASTNew newNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final SimpleNode child = (SimpleNode) newNode.jjtGetChild(0);

        if (child instanceof ASTExpression) {
            code += processMethodNodes(child, scope, methodManager);
            code = CodeGeneratorUtils.writeToString(code, "newarray int\n", scope);
            methodManager.stackPop(1);
            methodManager.addInstruction("newarray", "int[]");
        } else { // Child is identifier: aka constructor
            String identifier = ((ASTIdentifier) child).getIdentifier();

            code = CodeGeneratorUtils.writeToString(code, "new " + identifier + "\n", scope);
            code = CodeGeneratorUtils.writeToString(code, "dup\n", scope);
            code = CodeGeneratorUtils.writeToString(code, "invokespecial " + identifier + "/<init>()V\n", scope);

            methodManager.addInstruction("new", identifier);
        }

        return code;
    }

    private static String getJasminIdentifier(String identifier) {
        /**
         * This function exists because identifiers in J-- are allowed to be declared as
         * Jasmin keywords. As sugested, we created a method that sanitizes all
         * identifier's names and transforms all keywords into acceptable names for
         * Jasmin. We tried filtering as many names as possible, but Jasmin's
         * documentation does not make it easy to find all of its keywords.
         */
        identifier = "_" + identifier;

        return identifier;
    }

}