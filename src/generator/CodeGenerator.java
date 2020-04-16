import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * CodeGenerator
 */
public class CodeGenerator {

    private static final String fileExtension = ".j";

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
     */
    private void writeCode(String code) {
        try {
            jFile.write(code.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}