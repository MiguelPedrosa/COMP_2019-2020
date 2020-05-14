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
    private String identation = Character.toString(space);
    private int identationSize = 2;

    private FileOutputStream jFile;
    private SimpleNode rootNode;
    private String fileName;
    private String filePath;

    private SymbolTable symbolTable;

    private int labelCounter = 0;

    public CodeGenerator(SimpleNode root, SymbolTable symbolTable, String fileName) {
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

    private void readNodes(SimpleNode node, int scope, SymbolTable scopeTable) {
        int numChildren = node.jjtGetNumChildren();

        if (numChildren == 0)
            return;

        for (int i = 0; i < numChildren; i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);
            String nodeType = child.getClass().getSimpleName();

            switch (nodeType) {
                case "ASTStart":
                    readNodes(child, scope, scopeTable);
                    break;
                case "ASTClassDeclaration":
                    writeClass((ASTClassDeclaration) child, scope, scopeTable);
                    break;
                case "ASTMethodDeclaration":
                    writeMethod((ASTMethodDeclaration) child, scope, scopeTable);
                    break;
                case "ASTMainDeclaration":
                    writeMain((ASTMainDeclaration) child, scope, scopeTable);
                    break;
                case "ASTReturn":
                    writeReturn((ASTReturn) child, scope);
                    break;
                case "ASTVarDeclaration":
                    writeVarDeclaration((ASTVarDeclaration) child, scope, scopeTable);
                    break;
                default:
                    readNodes(child, scope, scopeTable);
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
        } catch (FileNotFoundException e) {
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
    private void writeCode(String code, int scope) {
        String identedCode = IntStream.range(0, scope * identationSize).mapToObj(i -> identation)
                .collect(Collectors.joining(""));

        identedCode += code;

        try {
            jFile.write(identedCode.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String writeToString(String oldString, String code, int scope) {
        String identedCode = IntStream.range(0, scope * identationSize).mapToObj(i -> identation)
                .collect(Collectors.joining(""));
        identedCode += code;

        return oldString + identedCode;
    }

    private void writeStringToCode(String code) {
        try {
            jFile.write(code.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to write the end of a method to the file
     * 
     */
    private void endMethod(int scope) {
        writeCode(".end method\n\n", scope);
    }

    /**
     * Method to write class initializer
     * 
     */
    private void writeInitializer(int scope) {
        writeCode("; standard initializer\n", scope);
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
    private void writeClass(ASTClassDeclaration classNode, int scope, SymbolTable scopeTable) {
        // System.out.println("Writing class...");
        String className = classNode.getClassId();
        writeCode(".class public " + className + "\n", scope);
        writeCode(".super java/lang/Object\n\n", scope);

        writeInitializer(scope);

        readNodes(classNode, scope, scopeTable);
    }

    /**
     * Method to transform a type string into a type for Jasmin
     */
    private String transformType(String type) {

        String typeString = null;

        switch (type) {
            case "int":
                typeString = "I";
                break;
            case "long":
                typeString = "L";
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
                typeString = "A";
                break;
            case "String":
                typeString = "Ljava/lang/String;";
                break;
            case "boolean":
                typeString = "Z";
                break;
            case "int[]":
                typeString = "[I";
                break;
            case "long[]":
                typeString = "[L";
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
                typeString = "[A";
                break;
            case "String[]":
                typeString = "[Ljava/lang/String;";
                break;
            case "boolean[]":
                typeString = "[Z";
                break;
        }

        return typeString;

    }

    private void writeStack(int numStacks, int scope) {
        writeCode(".limit stack " + numStacks + "\n", scope);
    }

    private List<String> prepareLocals(int scope, String methodKey) {
        if (!this.symbolTable.containsMethod(methodKey) && !methodKey.equals("main")) {
            System.err.println("Can find method " + methodKey + " in symbol table");
            return new ArrayList<>();
        }

        Map<String, SymbolVar> variables;
        if (methodKey.equals("main")) {
            variables = this.symbolTable.getMain().getVariables();
        } else {
            variables = this.symbolTable.getMethodTable(methodKey).getVariables();
        }

        List<String> locals = new ArrayList<>();
        // Add variables to locals container
        for (Map.Entry<String, SymbolVar> entry : variables.entrySet()) {
            locals.add(entry.getKey());
        }

        writeCode(".limit locals " + locals.size() + "\n", scope);

        return locals;
    }

    /**
     * Method to write a Method (or function) into the file
     */
    private void writeMethod(ASTMethodDeclaration methodNode, int scope, SymbolTable scopeTable) {
        final String methodName = methodNode.getMethodName();
        final String methodType = transformType(methodNode.getReturnType());

        List<String[]> arguments = methodNode.getArguments();
        String argsInJasmin = "";

        for (String[] argument : arguments) {
            String argType = transformType(argument[1]);
            argsInJasmin = argsInJasmin.concat(argType);
        }

        writeCode("\n.method public static " + methodName + "(" + argsInJasmin + ")" + methodType + "\n", scope);

        final String methodKey = methodNode.getMethodKey();
        MethodManager methodManager = new MethodManager();
        

        String code = processMethodNodes(methodNode, scope + 1, methodManager);

        writeStack(methodManager.getMaxStackSize(), scope + 1);
        List<String> locals = prepareLocals(scope + 1, methodKey);
        writeCode(code, scope);

        endMethod(scope);
    }

    private void writeMain(ASTMainDeclaration mainMethodNode, int scope, SymbolTable scopeTable) {
        writeCode("\n.method public static main([Ljava/lang/String;)V\n", scope);
        List<String> locals = prepareLocals(scope + 1, "main");
        writeCode("\n", scope);

        MethodManager methodManager = new MethodManager();
        

        String code = processMethodNodes(mainMethodNode, scope + 1, methodManager);

        writeStack(methodManager.getMaxStackSize(), scope + 1);

        writeCode("return\n", scope + 1);
        endMethod(scope);
    }

    private String processMethodNodes(SimpleNode methodNode, int scope, MethodManager methodManager) {
        int numChildren = methodNode.jjtGetNumChildren();
        String code = "";

        if (numChildren == 0)
            return code;

        for (int i = 0; i < numChildren; i++) {
            SimpleNode child = (SimpleNode) methodNode.jjtGetChild(i);
            String nodeType = child.getClass().getSimpleName();

            switch (nodeType) {
                case "ASTScope":
                    code += processMethodNodes(child, scope, methodManager);
                    break;
                case "ASTVarDeclaration":
                    //variable arlready added in locals
                    break;
                case "ASTIF":
                    code += writeIf((ASTIF) child, scope, methodManager);
                    break;
                case "ASTWhile":
                    break;
                case "ASTEquals":
                    break;
                case "ASTReturn":
                    break;

                case "ASTLiteral":
                    code += writeLiteral((ASTLiteral) child, scope, methodManager);
                    break;

                case "ASTFuncCall":
                    break;

                // Expression switches
                case "ASTExpression":
                    code += processMethodNodes(child, scope, methodManager);
                    break;
                case "ASTPlus":
                    writePlusOperation((ASTPlus) child, scope);
                    break;
                case "ASTMinus":
                    //writeMinusOperation((ASTMinus) child, scope, scopeTable);
                    break;
                case "ASTTimes":
                    //writeMultiOperation((ASTTimes) child, scope, scopeTable);
                    break;
                case "ASTDividor":
                    //writeDivOperation((ASTDividor) child, scope, scopeTable);
                    break;
                default:
                    System.out.println("Node not processed");
                    code += processMethodNodes(child, scope, methodManager);
                    break;
            }
        }

        return code;
    }

    private void writeReturn(ASTReturn returnNode, int scope) {
        writeCode("ireturn\n", scope);
    }

    private void writeVarDeclaration(ASTVarDeclaration varDecNode, int scope, SymbolTable scopeTable) {
        String tableType = scopeTable.getClass().getSimpleName();
        switch (tableType) {
            case "SymbolTable":
                writeClassField(varDecNode, scope, scopeTable);
                break;
            default:
                break;
        }
    }

    private void writeClassField(ASTVarDeclaration varDecNode, int scope, SymbolTable scopeTable) {
        String type = transformType(varDecNode.getType());
        writeCode(".field " + varDecNode.getVarId() + " " + type + "\n", scope);
    }

    private void writeNewLine() {

    }

    /**
     * Method to write "if" to the file
     */
    private String writeLiteral(ASTLiteral literalNode, int scope, MethodManager methodManager) {
        String code = "";

        String literal = literalNode.getLiteral();
        int stackLiteral;
        switch (literal) {
            case "true":
                stackLiteral = 1;
                break;
            case "false":
                stackLiteral = 0;
                break;
            default:
                stackLiteral = Integer.parseInt(literal);
                break;
        }

        code = writeToString(code, "bipush " + stackLiteral + "\n", scope);
        methodManager.addInstruction("bipush");

        return code;
    }

    /**
     * Method to write "if" to the file
     */
    private String writeIf(ASTIF ifNode, int scope, MethodManager methodManager) {

        String code = "";
        int label = this.labelCounter;
        this.labelCounter++;

        //escrever codigo
        SimpleNode conditionChild = (SimpleNode) ifNode.jjtGetChild(0);
        SimpleNode ifScope = (SimpleNode) ifNode.jjtGetChild(1);
        SimpleNode elseScope = (SimpleNode) ifNode.jjtGetChild(2);

        String conditionCode = processMethodNodes(conditionChild, scope, methodManager);
        String elseScopeCode = processMethodNodes(elseScope, scope, methodManager);
        String ifScopeCode = processMethodNodes(ifScope, scope, methodManager);

        code += conditionCode;
        //TODO: if_ correto (ir buscar à stack e isso)
        code = writeToString(code, "if_ correct" + label + "\n", scope);
        //TODO:verificar stack de if
        code += elseScopeCode;
        code = writeToString(code, "goto endIf" + label + "\n", scope);
        //TODO:verificar stack de goto
        code = writeToString(code, "correct" + label + ":\n" , 0);
        //TODO:verificar stack de labels
        code += ifScopeCode;
        code = writeToString(code, "endIf" + label + ":\n" , 0);
        //TODO:verificar stack de labels

        return code;
    }

    /**
     * Method to write "addition" (+) operation to the file
     */
    private String writePlusOperation(ASTPlus plusNode, int scope) {
        String code = "";

        writeToString(code, "iadd\n", scope);        
        //readNodes(plusNode, scope, scopeTable);
        if (true) // TODO verify if the operation involves integers of floats
            writeCode("iadd\n", scope);
        else
            writeCode("fadd\n", scope);

        return code;
    }

    /**
     * Method to write "subtraction" (-) operation to the file
     */
    private void writeMinusOperation(ASTMinus minusNode, int scope, SymbolTable scopeTable) {
        readNodes(minusNode, scope, scopeTable);
        if (true) // TODO verify if the operation involves integers of floats
            writeCode("isub\n", scope);
        else
            writeCode("fsub\n", scope);
    }

    /**
     * Method to write "multiplication" (*) operation to the file
     */
    private void writeMultiOperation(ASTTimes multiNode, int scope, SymbolTable scopeTable) {
        readNodes(multiNode, scope, scopeTable);
        if (true) // TODO verify if the operation involves integers of floats
            writeCode("imul\n", scope);
        else
            writeCode("fmul\n", scope);
    }

    /**
     * Method to write "division" (/) operation to the file
     */
    private void writeDivOperation(ASTDividor divNode, int scope, SymbolTable scopeTable) {
        readNodes(divNode, scope, scopeTable);
        if (true) // TODO verify if the operation involves integers of floats
            writeCode("idiv\n", scope);
        else
            writeCode("fdiv\n", scope);
    }

}