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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * CodeGenerator
 */
public class CodeGenerator {

    private static final String fileExtension = ".j";
    private static final char space = ' ';
    private static final char tab = '\t';
    private static final String fileSeparator = System.getProperty("file.separator");

    // Identation settings (can be changed later)
    private final String identation = Character.toString(space);
    private final int identationSize = 2;

    private FileOutputStream jFile;
    private final SimpleNode rootNode;
    private final String fileName;
    private final String filePath;

    private final SymbolTable symbolTable;

    private int labelCounter = 0;

    public CodeGenerator(final SimpleNode root, final SymbolTable symbolTable, final String fileName) {
        this.rootNode = root;
        this.symbolTable = symbolTable;
        this.fileName = fileName;
        this.filePath = System.getProperty("user.dir");
    }

    /**
     * Method to start the generation of code
     * 
     * @return
     */
    public boolean start() {
        System.out.println("Code generation started...\n\n");
        if (!generateFile()) {
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
     * Method to generate code file
     */
    private boolean generateFile() {
        System.out.println("Generating code file...\n\n");
        try {
            jFile = new FileOutputStream(filePath + fileSeparator + fileName + fileExtension);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Failed to generate code file!\n\n");
            return false;
        }
        return true;
    }

    /**
     * Method to write code into file
     * 
     * @param code
     * @param scope
     */
    private void writeCode(final String code, final int scope) {
        String identedCode = IntStream.range(0, scope * identationSize).mapToObj(i -> identation)
                .collect(Collectors.joining(""));

        identedCode += code;

        try {
            jFile.write(identedCode.getBytes());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private String writeToString(final String oldString, final String code, final int scope) {
        String identedCode = IntStream.range(0, scope * identationSize).mapToObj(i -> identation)
                .collect(Collectors.joining(""));
        identedCode += code;

        return oldString + identedCode;
    }

    private void writeStringToCode(final String code) {
        try {
            jFile.write(code.getBytes());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to write the end of a method to the file
     * 
     */
    private void endMethod(final int scope) {
        writeCode(".end method\n", scope);
    }

    /**
     * Method to write class initializer
     * 
     */
    private void writeInitializer(final int scope) {
        writeCode("\n; standard initializer\n", scope);
        writeCode(".method public <init>()V\n", scope);
        writeCode("aload_0\n", scope + 1);
        writeCode("invokenonvirtual java/lang/Object/<init>()V\n", scope + 1);
        writeCode("return\n", scope + 1);
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
        writeCode(".class public " + className + "\n", scope);
        writeCode(".super java/lang/Object\n\n", scope);

        // Field
        final int numChildren = classNode.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            final SimpleNode child = (SimpleNode) classNode.jjtGetChild(i);
            final String nodeType = child.getClass().getSimpleName();
            if(nodeType.equals("ASTVarDeclaration")) {
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
                typeString = "L" + type;
                break;
        }

        return typeString;

    }

    private void writeStack(final int numStacks, final int scope) {
        writeCode(".limit stack " + numStacks + "\n", scope);
    }

    private void writeLocals(final int numLocals, final int scope) {
        writeCode(".limit locals " + numLocals + "\n", scope);
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

        writeCode("\n.method public " + methodName + "(" + argsInJasmin + ")" + methodType + "\n", scope);

        final String methodKey = methodNode.getMethodKey();
        final MethodManager methodManager = new MethodManager();
        final List<SymbolVar> locals = prepareLocals(scope + 1, methodKey);

        methodManager.setLocals(locals);

        String code = "";

        final int numChildren = methodNode.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            final SimpleNode child = (SimpleNode) methodNode.jjtGetChild(i);
            code += processMethodNodes(child, scope +1, methodManager);
        }

        writeStack(methodManager.getMaxStackSize(), scope + 1);
        writeLocals(locals.size(), scope + 1);

        writeCode(code, scope);

        endMethod(scope);
    }

    private void writeMain(final ASTMainDeclaration mainMethodNode, final int scope, final SymbolTable scopeTable) {
        writeCode("\n.method public static main([Ljava/lang/String;)V\n", scope);

        final MethodManager methodManager = new MethodManager();
        final List<SymbolVar> locals = prepareLocals(scope + 1, "main");

        methodManager.setLocals(locals);

        String code = "";

        final int numChildren = mainMethodNode.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            final SimpleNode child = (SimpleNode) mainMethodNode.jjtGetChild(i);
            code += processMethodNodes(child, scope +1, methodManager);
        }

        writeStack(methodManager.getMaxStackSize(), scope + 1);
        writeLocals(locals.size(), scope + 1);
        writeCode(code, scope);
        writeCode("return\n", scope + 1);

        endMethod(scope);
    }

    private String processMethodNodes(final SimpleNode currentNode, final int scope,
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
        identifier = JasminSanitizer.getJasminIdentifier(identifier);
        // Field is always declared as private because that is the java default
        writeCode(".field private " + identifier + " " + type + "\n", scope);
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

                
                returnType = this.getFuncReturnType(argsTypes, className, methodName, true);

                codeLine = "invokestatic " + className + "/" + methodName + "(" + args + ")"
                        + this.transformType(returnType) + "\n";
                code = writeToString(code, codeLine, scope);

                methodManager.stackPop(numberArgs);
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

                returnType = this.getFuncReturnType(argsTypes, className, methodName, false);

                codeLine = "invokevirtual " + className + "/" + methodName + "(" + args + ")"
                        + this.transformType(returnType) + "\n";
                code = writeToString(code, codeLine, scope);

                methodManager.stackPop(numberArgs + 1);
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

            returnType = this.getFuncReturnType(argsTypes, className, methodName, false);

            codeLine = "invokevirtual " + className + "/" + methodName + "(" + args + ")"
                    + this.transformType(returnType) + "\n";
            code = writeToString(code, codeLine, scope);

            methodManager.stackPop(numberArgs + 1);
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

        if(this.symbolTable.containsMethod(signature)) {
            return this.symbolTable.getMethodReturn(signature);
        }

        if(this.symbolTable.getImports().containsKey(className)) {
            if (isStatic) {
                return this.symbolTable.getImports().get(className).getStaticMethodType(signature);
            } else {
                return this.symbolTable.getImports().get(className).getMethodType(signature);
            }
        }

        if(this.symbolTable.getClasseName().equals(className)) {
            className = this.symbolTable.getClassExtendsName();

            if(this.symbolTable.getImports().containsKey(className)) {
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

                final String filteredIdentifier = JasminSanitizer.getJasminIdentifier(identifier);

                code = writeToString(code, "getfield " + this.symbolTable.getClasseName() + "/" + filteredIdentifier + " "
                        + transformType(type) + "\n", scope);
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
        if(localIndex >= 0 && localIndex <= 3)
            indexForInstruction = "_";
        indexForInstruction += localIndex;

        if (type.equals("int")) {
            code = writeToString(code, "iload" + indexForInstruction + "\n", scope);
            methodManager.addInstruction("iload", type);
        } else {
            code = writeToString(code, "aload" + indexForInstruction + "\n", scope);
            methodManager.addInstruction("aload", type);
        }

        list.add(code);
        return list;
    }

    /**
     * Method to write "identifier" to the file
     */
    private String writeReturn(final ASTReturn returnNode, final int scope, final MethodManager methodManager) {

        // TODO: check if this method is ok with group
        String code = "";

        final SimpleNode returnExpression = (SimpleNode) returnNode.jjtGetChild(0);

        /*
         * Expression Variable(identifier) literal new function call
         * 
         * TODO:What happens when its "return new Potato()"??? is new Potato in stack?
         * invokevirtual like a function???
         */

        code += processMethodNodes(returnExpression, scope, methodManager);

        if (methodManager.getLastTypeInStack().equals("int")) {
            code = writeToString(code, "ireturn\n", scope);
        } else {
            code = writeToString(code, "areturn\n", scope);
        }

        return code;
    }

    private String writeArrayAccess(final ASTArrayAccess arrayNode, final int scope,
        final MethodManager methodManager) {

        String code = "";

        final SimpleNode arrayNodeName  = (SimpleNode) arrayNode.jjtGetChild(0);
        final SimpleNode arrayNodeIndex = (SimpleNode) arrayNode.jjtGetChild(1);

        processMethodNodes(arrayNodeName,  scope, methodManager);
        final String arrayType = methodManager.getLastTypeInStack();
        final String simpleArrayType = methodManager.getSimpleArrayType(arrayType);
        processMethodNodes(arrayNodeIndex, scope, methodManager);

        if(simpleArrayType.equals("int")) {
            code = writeToString(code, "iaload\n", scope);
            methodManager.stackPop(2);
            methodManager.addInstruction("iaload", "int");
        } else {
            code = writeToString(code, "aaload\n", scope);
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

            String filteredIdentifier = JasminSanitizer.getJasminIdentifier(identifier);

            // ???? aload0 antes ????
            code = writeToString(code, "getfield " + this.symbolTable.getClasseName() + "/" + filteredIdentifier
                    + " " + transformType(type) + "\n", scope);
            methodManager.addInstruction("getfield", type);

            return code;
        }

        // Optimization :)
        String indexForInstruction = " ";
        if(localIndex >= 0 && localIndex <= 3)
            indexForInstruction = "_";
        indexForInstruction += localIndex;

        if (type.equals("int")) {
            code = writeToString(code, "iload" + indexForInstruction + "\n", scope);
            methodManager.addInstruction("iload", type);
        } else {
            code = writeToString(code, "aload" + indexForInstruction + "\n", scope);
            methodManager.addInstruction("aload", type);
        }

        return code;
    }

    /**
     * Method to write equals to the file
     */
    private String writeEquals(final ASTEquals equalsNode, final int scope,
            final MethodManager methodManager) {
        String code = "";

        final SimpleNode childLeft  = (SimpleNode) equalsNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) equalsNode.jjtGetChild(1);
                
        // Store value in non-array variable
        if(childLeft instanceof ASTIdentifier) {

            code += processMethodNodes(childRight,  scope, methodManager);

            final String identifier = ((ASTIdentifier) childLeft).getIdentifier();
            final int localIndex = methodManager.indexOfLocal(identifier);
            String type = methodManager.typeOfLocal(identifier);

            if (type == null) {

                type = this.symbolTable.getVariableType(identifier);
                String filteredIdentifier = JasminSanitizer.getJasminIdentifier(identifier);
                code = writeToString(code, "putfield " + this.symbolTable.getClasseName() + "/" +
                        filteredIdentifier + " " + transformType(type) + "\n", scope);
                methodManager.addInstruction("putfield", type);
    
                return code;
            }
            if (type.equals("int")) {
                code = writeToString(code, "istore " + localIndex + "\n", scope);
                methodManager.addInstruction("istore", type);
            } else {
                code = writeToString(code, "astore " + localIndex + "\n", scope);
                methodManager.addInstruction("astore", type);
            }
        } else { // Store value in array variable
            final SimpleNode arrayName  = (SimpleNode) childLeft.jjtGetChild(0);
            final SimpleNode arrayIndex = (SimpleNode) childLeft.jjtGetChild(1);

            code += processMethodNodes(arrayName,  scope, methodManager);
            final String arrayType = methodManager.getLastTypeInStack();
            final String simpleArrayType = methodManager.getSimpleArrayType(arrayType);
            code += processMethodNodes(arrayIndex, scope, methodManager);
            // In arrays, new value is at the top of the stack
            code += processMethodNodes(childRight,  scope, methodManager);

            if (simpleArrayType.equals("int")) {
                code = writeToString(code, "iastore " + "\n", scope);
                methodManager.addInstruction("iastore", simpleArrayType);
            } else {
                code = writeToString(code, "aastore " + "\n", scope);
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
                code = writeToString(code, "bipush " + stackLiteral + "\n", scope);
                methodManager.addInstruction("bipush", "boolean");
                break;
            case "false":
                stackLiteral = 0;
                code = writeToString(code, "bipush " + stackLiteral + "\n", scope);
                methodManager.addInstruction("bipush", "boolean");
                break;
            case "this":
                code = writeToString(code, "aload_0 \n", scope);
                methodManager.addInstruction("aload", this.symbolTable.getClasseName());
                break;
            default:
                stackLiteral = Integer.parseInt(literal);
                if(stackLiteral >= 0 && stackLiteral <= 5) {
                    code = writeToString(code, "iconst_" + stackLiteral + "\n", scope);
                    methodManager.addInstruction("bipush", "int");
                } else if(stackLiteral == -1) {
                    code = writeToString(code, "iconst_m1\n", scope);
                    methodManager.addInstruction("bipush", "int");
                } else if(stackLiteral > 127) {
                    code = writeToString(code, "ldc_w " + stackLiteral + "\n", scope);
                    methodManager.addInstruction("ldc_w", "long");
                } else {
                    code = writeToString(code, "bipush " + stackLiteral + "\n", scope);
                    methodManager.addInstruction("bipush", "int");
                }
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
        code = writeToString(code, "ifgt correct" + label + "\n", scope);
        methodManager.addInstruction("ifgt", "");
        code += processMethodNodes(elseScope, scope, methodManager);
        code = writeToString(code, "goto endIf" + label + "\n", scope);
        code = writeToString(code, "correct" + label + ":\n", 0);
        code += processMethodNodes(ifScope, scope, methodManager);
        code = writeToString(code, "endIf" + label + ":\n", 0);

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

        code = writeToString(code, "while" + label + ":\n", 0);
        code += processMethodNodes(conditionChild, scope, methodManager);
        code = writeToString(code, "ifle endWhile" + label + "\n", scope);
        methodManager.addInstruction("ifle", "");
        code += processMethodNodes(scopeChild, scope, methodManager);
        code = writeToString(code, "goto while" + label + "\n", scope);
        code = writeToString(code, "endWhile" + label + ":\n", 0);

        return code;
    }

    /**
     * Method to write "addition" (+) operation to the file
     */
    private String writePlusOperation(final ASTPlus plusNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final SimpleNode childLeft  = (SimpleNode) plusNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) plusNode.jjtGetChild(1);

        code += processMethodNodes(childLeft,  scope, methodManager);
        code += processMethodNodes(childRight, scope, methodManager);

        code = writeToString(code, "iadd\n", scope);

        methodManager.stackPop(2);
        methodManager.addInstruction("iadd", "int");

        return code;
    }

    /**
     * Method to write "subtraction" (-) operation to the file
     */
    private String writeMinusOperation(final ASTMinus minusNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final SimpleNode childLeft  = (SimpleNode) minusNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) minusNode.jjtGetChild(1);

        code += processMethodNodes(childLeft,  scope, methodManager);
        code += processMethodNodes(childRight, scope, methodManager);

        code = writeToString(code, "isub\n", scope);

        methodManager.stackPop(2);
        methodManager.addInstruction("isub", "int");

        return code;
    }

    /**
     * Method to write "multiplication" (*) operation to the file
     */
    private String writeMultiOperation(final ASTTimes multiNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final SimpleNode childLeft  = (SimpleNode) multiNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) multiNode.jjtGetChild(1);

        code += processMethodNodes(childLeft,  scope, methodManager);
        code += processMethodNodes(childRight, scope, methodManager);

        code = writeToString(code, "imul\n", scope);

        methodManager.stackPop(2);
        methodManager.addInstruction("imul", "int");

        return code;
    }

    /**
     * Method to write "division" (/) operation to the file
     */
    private String writeDivOperation(final ASTDividor divNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final SimpleNode childLeft  = (SimpleNode) divNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) divNode.jjtGetChild(1);

        code += processMethodNodes(childLeft,  scope, methodManager);
        code += processMethodNodes(childRight, scope, methodManager);

        code = writeToString(code, "idiv\n", scope);

        methodManager.stackPop(2);
        methodManager.addInstruction("idiv", "int");

        return code;
    }

    /**
     * Method to write "less than" (<) operation to the file
     */
    private String writeLessThanOperation(ASTLessThan lessThanNode, int scope, MethodManager methodManager) {
        String code = "";

        final SimpleNode leftChild = (SimpleNode) lessThanNode.jjtGetChild(0);
        final SimpleNode rightChild = (SimpleNode) lessThanNode.jjtGetChild(1);

        // Child order is inverted because dcmp requires it to work 
        // as intended with a < operation
        // Convertion to double is done so that a compare instruction can be
        // used and result of operation is a single value on the stack
        code += processMethodNodes(rightChild, scope, methodManager);
        code = writeToString(code, "i2l\n", scope);

        code += processMethodNodes(leftChild, scope, methodManager);
        code = writeToString(code, "i2l\n", scope);

        code = writeToString(code, "lcmp\n", scope);

        methodManager.stackPop(2);
        methodManager.addInstruction("lcmp", "boolean");
        return code;
    }

    /**
     * Method to write "and" (&&) operation to the file
     */
    private String writeAndOperation(final ASTAnd andNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final SimpleNode childLeft  = (SimpleNode) andNode.jjtGetChild(0);
        final SimpleNode childRight = (SimpleNode) andNode.jjtGetChild(1);

        code += processMethodNodes(childLeft,  scope, methodManager);
        code += processMethodNodes(childRight, scope, methodManager);

        code = writeToString(code, "iand\n", scope);

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

        final SimpleNode child  = (SimpleNode) notNode.jjtGetChild(0);

        code += processMethodNodes(child,  scope, methodManager);
        code = writeToString(code, "ifgt " + notLabel + "\n", scope);
        code = writeToString(code, "iconst_1\n", scope);
        code = writeToString(code, "goto " + endLabel + "\n", scope);
        code = writeToString(code, notLabel + ":\n", 0);
        code = writeToString(code, "iconst_0\n", scope);
        code = writeToString(code, endLabel + ":\n", 0);

        methodManager.stackPop(1);
        methodManager.addInstruction("iconst", "boolean");

        return code;
    }

    /**
     * Method to write "length" array operator to the file
     */
    private String writeLengthOperation(final ASTLength lengthNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final SimpleNode child  = (SimpleNode) lengthNode.jjtGetChild(0);

        code += processMethodNodes(child,  scope, methodManager);

        code = writeToString(code, "arraylength\n", scope);

        methodManager.stackPop(1);
        methodManager.addInstruction("arraylength", "int");

        return code;
    }

    /**
     * Method to write "new" operation to the file
     */
    private String writeNewOperation(final ASTNew newNode, final int scope, final MethodManager methodManager) {
        String code = "";

        final SimpleNode child  = (SimpleNode) newNode.jjtGetChild(0);

        if(child instanceof ASTExpression) {
            code += processMethodNodes(child, scope, methodManager);
            code = writeToString(code, "newarray int\n", scope);
            methodManager.stackPop(1);
            methodManager.addInstruction("newarray", "int[]");
        } else { // Child is identifier: aka constructor
            String identifier = ((ASTIdentifier) child).getIdentifier();

            code = writeToString(code, "new " + identifier + "\n", scope);
            code = writeToString(code, "dup\n", scope);
            code = writeToString(code, "invokespecial " + identifier + "/<init>()V\n", scope);

            methodManager.addInstruction("new", identifier);
        }

        return code;
    }

}