import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String args[]) throws IOException, ParseException {

        final String filePath = args[0];
        Parser myProgram = new Parser(openFile(filePath));

        SimpleNode root = myProgram.Start();
        root.dump("", 0);
        // Semantic analyser
        ErrorHandler.resetHandler();
        SemanticAnalyser semantic = new SemanticAnalyser(root);
        SymbolTable classTable = semantic.Start();
        System.out.println(classTable);

        ErrorHandler.printWarnings();
        ErrorHandler.printErrors();
        if (ErrorHandler.hasErrors()) {
            System.err.println("\nCompilation cannot continue. Found " + MyUtils.ANSI_RED + ErrorHandler.getNumberOfErrors() + MyUtils.ANSI_RESET + " errors.");
            throw new IOException();
        }

        CodeGenerator codeGenerator = new CodeGenerator(root, classTable, "testFileName");
        codeGenerator.start();
    }

    public static InputStream openFile(String filePath) {
        final File file = new File(filePath);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.out.println("No file given.");
            return null;
        }
    }
}