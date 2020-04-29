import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

    public CodeGenerator(SimpleNode root, String fileName) {
        this.rootNode = root;
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

        readNodes(rootNode, 0);

        return true;
    }

    private void readNodes(SimpleNode node, int scope) {
        int numChildren = node.jjtGetNumChildren();

        if (numChildren == 0)
            return;

        for (int i = 0; i < numChildren; i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);
            String nodeType = child.getClass().getSimpleName();

            switch (nodeType) {
                case "ASTStart":
                    readNodes(child, scope);
                    break;
                case "ASTClassDeclaration":
                    writeClass((ASTClassDeclaration) child, scope);
                    break;
                case "ASTMethodDeclaration":
                    writeMethod((ASTMethodDeclaration) child, scope);
                    break;
                case "ASTMainDeclaration":

                    break;
                default:
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
    private void writeClass(ASTClassDeclaration classNode, int scope) {
        System.out.println("Writing class...");
        String className = classNode.getClassId();
        writeCode(".class public " + className + "\n", scope);
        writeCode(".super java/lang/Object\n\n", scope);

        writeInitializer(scope);

        readNodes(classNode, scope);
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

        }

        return typeString;

    }

    private void writeStack(int scope) {
        int value = 99;
        writeCode(".limit stack " + value + "\n",scope);
    }

    private void writeLocals(int scope) {
        int value = 99;
        writeCode(".limit locals " + value + "\n",scope);
    }

    /**
     * Method to write a Method (or function) into the file
     */
    private void writeMethod(ASTMethodDeclaration methodNode, int scope) {
        System.out.println("Writing function...");
        String methodName = methodNode.getMethodName();
        String methodType = transformType(methodNode.getReturnType());

        LinkedHashMap<String, String> arguments = methodNode.getArguments();
        String argsInJasmin = "";

        Set set = arguments.entrySet();

        Iterator i = set.iterator();

        while (i.hasNext()) {
            Map.Entry ma = (Map.Entry) i.next();
            String argType = transformType(ma.getValue().toString());
            argsInJasmin = argsInJasmin.concat(argType);
        }

        writeCode(".method public static " + methodName + "(" + argsInJasmin + ")" + methodType, scope);

        readNodes(methodNode, scope + 1);

        endMethod(scope);

    }

    private void writeMain(ASTMainDeclaration mainMethodNode, int scope) {
        writeCode(".method public static main([Ljava/lang/String;)V\n", scope);
        writeStack(scope);
        writeLocals();
        readNodes(mainMethodNode, scope+1);
        endMethod(scope);
    }

}