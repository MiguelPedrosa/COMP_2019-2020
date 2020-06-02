import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class CodeGeneratorUtils {

    private static FileOutputStream jFile;
    private static final String filePath = System.getProperty("user.dir") + "/jFiles";
    private static String fileName;

    private static final String fileExtension = ".j";
    private static final char tab = '\t';
    private static final char space = ' ';
    private static final String fileSeparator = System.getProperty("file.separator");

    // Identation settings (can be changed later)
    private static final String identation = Character.toString(space);
    private static final int identationSize = 2;

    public static void setFileName(String fileName){
        CodeGeneratorUtils.fileName = fileName;
    }

    /**
     * Method to generate code file
     */
    public static boolean generateFile() {
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
    public static void writeCode(final String code, final int scope) {
        String identedCode = IntStream.range(0, scope * identationSize).mapToObj(i -> identation)
                .collect(Collectors.joining(""));

        identedCode += code;

        try {
            jFile.write(identedCode.getBytes());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static String writeToString(final String oldString, final String code, final int scope) {
        String identedCode = IntStream.range(0, scope * identationSize).mapToObj(i -> identation)
                .collect(Collectors.joining(""));
        identedCode += code;

        return oldString + identedCode;
    }

    public static void writeStringToCode(final String code) {
        try {
            jFile.write(code.getBytes());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}