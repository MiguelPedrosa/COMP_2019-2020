import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * CodeGenerator
 */
public class CodeGenerator {

    private static final String fileExtension = ".j";
    private static final char space = ' ';
    private static final char tab = '\t';

    //Identation settings (can be changed later)
    private String identation = Character.toString(space);
    private int identationSize = 2;

    private FileOutputStream jFile;
    private SimpleNode rootNode;
    private String fileName;
    private String filePath;

    public CodeGenerator(SimpleNode root, String fileName) {
        this.rootNode = root;
        this.fileName = fileName;
    }

    /**
     * Method to start the generation of code
     * 
     * @return
     */
    public boolean start() {
        if (!generateFile()) {
            return false;
        }
        
        return true;
    }

    /**
     * Method to generate code file
     */
    public boolean generateFile() {
        try {
            jFile = new FileOutputStream(filePath + fileName + fileExtension);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
        String identedCode = IntStream.range(0, scope*identationSize).mapToObj(i -> identation).collect(Collectors.joining(""));
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
        writeCode("aload_0\n", scope);
        writeCode("invokenonvirtual java/lang/Object/<init>()V\n", scope);
        writeCode("return\n", scope);
        endMethod(scope);
    }

    /**
     * Method to write a class into the file
     * 
     * @param classNode
     */
    public void writeClass(SimpleNode classNode, int scope) {
        String className;
        writeCode(".class public " + className + "\n", scope);
    }

}