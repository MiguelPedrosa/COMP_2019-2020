import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String args[]) throws ParseException {

        final String filePath = args[0];
        Parser myProgram = new Parser(openFile(filePath));

        SimpleNode root = myProgram.Start();
        root.dump("");
        // Semantic analyser
        SemanticAnalyser semantic = new SemanticAnalyser(root);
        SymbolTable classTable = semantic.Start();
        System.out.println(classTable);
    }

    public static InputStream openFile(String filePath) {
        final File file = new File(filePath);
        try {
            return new FileInputStream(file);
        } catch(FileNotFoundException e) {
            System.out.println("No file given.");
            return null;
        }
    }
}