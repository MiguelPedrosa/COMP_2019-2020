import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {

    /* Attribute that allows for the singleton pattern */
    private static ErrorHandler handler = new ErrorHandler();

    /* Public and static interface */
    public static void addError(String description, int line) {
        handler.addErrorToList(description, line);
    }

    public static void addError(String description) {
        addError(description, -1);
    }

    public static void addWarning(String description, int line) {
        handler.addWarningToList(description, line);
    }

    public static void addWarning(String description) {
        addWarning(description, -1);
    }

    public static void printErrors() {
        handler.printErrorList();
    }

    public static void printWarnings() {
        handler.printWarningList();
    }

    public static Boolean hasErrors() {
        return !handler.errorList.isEmpty();
    }

    public static int getNumberOfErrors() {
        return handler.errorList.size();
    }

    public static Boolean hasWarnings() {
        return !handler.warningList.isEmpty();
    }

    public static void removeLastError() {
        final int size = handler.errorList.size();
        if (size < 1)
            return;
        handler.errorList.remove(size - 1);
    }

    public static void resetHandler() {
        handler.errorList.clear();
        handler.warningList.clear();
    }

    /* Private non-static methods necessary to implement public interface */

    private List<Error> errorList;
    private List<Warning> warningList;

    private ErrorHandler() {
        this.errorList = new ArrayList<>();
        this.warningList = new ArrayList<>();
    }

    private void addErrorToList(String description, int line) {
        Error newError = new Error(description, line);
        errorList.add(newError);
    }

    private void addWarningToList(String description, int line) {
        Warning newWarning = new Warning(description, line);
        warningList.add(newWarning);
    }

    private void printErrorList() {
        for (final Error error : errorList) {
            System.err.println(error);
        }
    }

    private void printWarningList() {
        for (final Warning warning : warningList) {
            System.err.println(warning);
        }
    }

    private class Error {
        private String description;
        private int line;

        public Error(String description, int line) {
            this.description = description;
            this.line = line;
        }

        public Error(String description) {
            this(description, -1);
        }

        public String toString() {
            String message = MyUtils.ANSI_RED + "Error" + MyUtils.ANSI_RESET;

            // Check if error line is specified
            if (this.line != -1)
                message += " in line " + this.line;

            message += ": " + this.description;
            return message;
        }
    }

    private class Warning {
        private String description;
        private int line;

        public Warning(String description, int line) {
            this.description = description;
            this.line = line;
        }

        public Warning(String description) {
            this(description, -1);
        }

        public String toString() {
            String message = MyUtils.ANSI_YELLOW + "Warning" + MyUtils.ANSI_RESET;

            // Check if error line is specified
            if (this.line != -1)
                message += " in line " + this.line;

            message += ": " + this.description;
            return message;
        }
    }
}