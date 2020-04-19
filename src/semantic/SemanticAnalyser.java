import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SemanticAnalyser {

    private SimpleNode root;
    private SymbolTable ST;

    public SemanticAnalyser(SimpleNode root) {
        this.root = root;
        this.ST = new SymbolTable(null);
    }

    public SymbolTable Start() {
        // Handle Imports
        for (int i = 0; i < this.root.jjtGetNumChildren() - 1; i++) {
            // First childs are import
        }
        // class node
        SimpleNode classNode = (SimpleNode) this.root.jjtGetChild(this.root.jjtGetNumChildren() - 1);

        if (classNode instanceof ASTClassDeclaration) {

            this.ST.addClasseName(((ASTClassDeclaration) classNode).getClassId());
            this.ST.addClassExtendsName(((ASTClassDeclaration) classNode).getExtendsId());

            this.signFuntions(classNode);
            this.processClassNode(classNode);
        }

        ErrorHandler.printErrors();
        ErrorHandler.printWarnings();
        return ST;
    }

    /*
     * ---------------------------------SIGN
     * FUNCTIONS---------------------------------------
     */

    private void signFuntions(SimpleNode classNode) {
        for (int i = 0; i < classNode.jjtGetNumChildren(); i++) {

            SimpleNode childNode = (SimpleNode) classNode.jjtGetChild(i);

            // Main declaration
            if (childNode instanceof ASTMainDeclaration) {
                signMainNode((ASTMainDeclaration) childNode);
            }
            // Methods declarations
            else if (childNode instanceof ASTMethodDeclaration) {
                signMethodNode((ASTMethodDeclaration) childNode);
            }
        }
    }

    private void signMainNode(ASTMainDeclaration mainNode) {
        this.ST.addMain();
    }

    private void signMethodNode(ASTMethodDeclaration methodNode) {
        String returnType = methodNode.getReturnType();
        String methodName = methodNode.getMethodName();
        LinkedHashMap<String, String> arguments = methodNode.getArguments();
        String key = this.getMethodKey(methodName, arguments);
        if (!this.ST.addMethod(key, methodName, arguments, returnType)) {
            ErrorHandler.addError("Repeted method:" + methodName);
        }

    }

    /*
     * ---------------------------------PROCESS
     * NODES---------------------------------------
     */

    private void processClassNode(SimpleNode classNode) {
        for (int i = 0; i < classNode.jjtGetNumChildren(); i++) {

            final SimpleNode childNode = (SimpleNode) classNode.jjtGetChild(i);

            // First childs are variables
            if (childNode instanceof ASTVarDeclaration) {
                processVarNode((ASTVarDeclaration) childNode);
            }
            // Main declaration
            else if (childNode instanceof ASTMainDeclaration) {
                processMainNode((ASTMainDeclaration) childNode);
            }
            // Methods declarations
            else if (childNode instanceof ASTMethodDeclaration) {
                processMethodNode((ASTMethodDeclaration) childNode);
            }
        }
    }

    private void processVarNode(ASTVarDeclaration childNode) {
        String type = childNode.getType();
        String varID = childNode.getVarId();
        this.ST.addVariable(type, varID);
    }

    private void processMainNode(ASTMainDeclaration mainNode) {
        for (int i = 0; i < mainNode.jjtGetNumChildren(); i++) {
            final SimpleNode childNode = (SimpleNode) mainNode.jjtGetChild(i);
            // First childs are local variables
            if (childNode instanceof ASTVarDeclaration) {
                processLocalVarDeclaration("main", (ASTVarDeclaration) childNode);
            } else if (childNode instanceof ASTEquals) {
                processEquals("main", (ASTEquals) childNode);
            }
        }
    }

    private void processMethodNode(ASTMethodDeclaration methodNode) {
        String methodName = methodNode.getMethodName();
        LinkedHashMap<String, String> arguments = methodNode.getArguments();
        String key = this.getMethodKey(methodName, arguments);

        for (int i = 0; i < methodNode.jjtGetNumChildren(); i++) {
            final SimpleNode childNode = (SimpleNode) methodNode.jjtGetChild(i);
            // First childs are local variables
            if (childNode instanceof ASTVarDeclaration) {
                processLocalVarDeclaration(key, (ASTVarDeclaration) childNode);
            } else if (childNode instanceof ASTEquals) {
                processEquals(key, (ASTEquals) childNode);
            }
        }
    }

    private void processLocalVarDeclaration(String key, ASTVarDeclaration childNode) {
        String type = childNode.getType();
        String varID = childNode.getVarId();
        this.ST.addLocalVariable(key, type, varID);
    }

    private void processEquals(String methodKey, ASTEquals node) {
        if (node.jjtGetNumChildren() == 2) {

            String equalsId = null;
            String equalsIdType = null;
            String equalsValType = null;

            ArrayList<String> list = getEqualsIdType(methodKey, (SimpleNode) node.jjtGetChild(0));

            if (list.size() == 2) {
                equalsId = list.get(0);
                equalsIdType = list.get(1);
            }

            equalsValType = this.getExpressionType(methodKey, (SimpleNode) node.jjtGetChild(1));

            if (equalsIdType != null && equalsValType != null && equalsIdType.equals(equalsValType))
                this.ST.initializeVariable(methodKey, equalsId);

            // verifica se filho = pai então aceita apesar de tipos serem diferentes
            else if (equalsIdType != null && equalsValType != null && equalsIdType.equals(this.ST.getClasseName())
                    && equalsValType.equals(this.ST.getClassExtendsName()))
                this.ST.initializeVariable(methodKey, equalsId);

            else
                ErrorHandler.addError("Incorrect types.");

        } else
            ErrorHandler.addError("Incorrect number of childs in equals node.");

    }

    /*
     * --------------------------------- EXTRA
     * ---------------------------------------
     */

    public String getMethodKey(String methodName, LinkedHashMap<String, String> arguments) {
        String key = methodName;

        for (Map.Entry<String, String> entry : arguments.entrySet())
            key += entry.getValue();

        return key;
    }

    public String getMethodKey(String methodName, ArrayList<String> arguments) {
        String key = methodName;

        for(int i = 0; i < arguments.size(); i++)
            key += arguments.get(i);

        return key;
    }

    private ArrayList<String> getEqualsIdType(String methodKey, SimpleNode equalsIdNode) {
        ArrayList<String> list = new ArrayList<String>();
        String equalsId = null;
        String equalsIdType = null;

        if (equalsIdNode instanceof ASTIdentifier) {
            equalsId = ((ASTIdentifier) equalsIdNode).getIdentifier();
            equalsIdType = this.getVarType(methodKey, equalsId);
        } else if (equalsIdNode instanceof ASTArrayAccess) {
            SimpleNode firstChild = (SimpleNode) equalsIdNode.jjtGetChild(0);
            SimpleNode secondChild = (SimpleNode) equalsIdNode.jjtGetChild(1);

            // dealing with first child
            /*
             * TODO: verificar se os outros passam na gramatica -> Literal -> identifier
             * DONE -> expressionNew -> (expression)
             */
            if (firstChild instanceof ASTIdentifier) {
                equalsId = ((ASTIdentifier) firstChild).getIdentifier();

            }

            // dealing with second child
            String indexType = this.getExpressionType(methodKey, secondChild);
            if (indexType == null || !indexType.equals("int")) {

                ErrorHandler.addError("Expected int for index in \"" + equalsId + "\" array.");
                list.add(null);
                list.add(null);
                return list;
            } else {
                if (isArrayVar(methodKey, equalsId)) {
                    equalsIdType = this.getVarType(methodKey, equalsId);
                    equalsIdType = this.getSimpleArrayType(equalsIdType);
                } else
                    ErrorHandler.addError("Variable \"" + equalsId + "\" is not an array.");
            }

        }

        list.add(equalsId);
        list.add(equalsIdType);
        return list;
    }

    private String getExpressionType(String methodKey, SimpleNode expression) {
        String type = null;

        if (expression instanceof ASTExpression) {
            SimpleNode childNode = (SimpleNode) expression.jjtGetChild(0);
            type = getExpressionType(methodKey, childNode);
        }

        if (expression instanceof ASTLiteral) {
            type = ((ASTLiteral) expression).getLiteralType();
            if (type.equals("this"))
                return this.ST.getClasseName();
            else
                return type;
        }

        else if (expression instanceof ASTIdentifier) {
            type = getVarType(methodKey, ((ASTIdentifier) expression).getIdentifier());
        }

        else if (expression instanceof ASTNew) {
            SimpleNode childNode = (SimpleNode) expression.jjtGetChild(0);

            if (childNode instanceof ASTExpression) {
                String indexType = getExpressionType(methodKey, childNode);
                if (indexType != null && indexType.equals("int")) {
                    type = ((ASTNew) expression).getType();
                }
            } else if (childNode instanceof ASTIdentifier)
                type = ((ASTIdentifier) childNode).getIdentifier();
        }

        else if (expression instanceof ASTArrayAccess) {
            SimpleNode firstChild = (SimpleNode) expression.jjtGetChild(0);
            SimpleNode secondChild = (SimpleNode) expression.jjtGetChild(1);

            String indexType = this.getExpressionType(methodKey, secondChild);
            String identifier = null;

            // primeiro filho tem que ser um identifier
            if (firstChild instanceof ASTIdentifier) {
                identifier = ((ASTIdentifier) firstChild).getIdentifier();
                // Verifica se o identifier é uma var array
                if (this.isArrayVar(methodKey, identifier)) {
                    type = this.getExpressionType(methodKey, firstChild);
                    type = this.getSimpleArrayType(type);
                } else
                    ErrorHandler.addError("Variable \"" + identifier + "\" is not an array.");
            }

            // segundo filho tem que ser um int
            if (indexType == null || !indexType.equals("int")) {
                ErrorHandler.addError("Expected int for index in \"" + identifier + "\" array.");
            }
        }

        else if (expression instanceof ASTFuncCall) {
            // 3 filhos
            SimpleNode firstChild = (SimpleNode) expression.jjtGetChild(0);
            SimpleNode secondChild = (SimpleNode) expression.jjtGetChild(1);
            SimpleNode thirdChild = (SimpleNode) expression.jjtGetChild(2);

            String classType = this.getExpressionType(methodKey, firstChild);

            //nome do método
            if (secondChild instanceof ASTIdentifier) {
                String methodName = ((ASTIdentifier) secondChild).getIdentifier();
                
                //verificar se existe método em classe (imports e minha classe)
                ArrayList<String> argsTypes = new ArrayList<String>();

                if (thirdChild instanceof ASTFuncArgs) {
                    argsTypes = this.getArgsTypes(methodKey, (ASTFuncArgs) thirdChild);
                    String method = this.getMethodKey(methodName, argsTypes);

                    // If is this class check if methods exists
                    if(classType != null && classType.equals(this.ST.getClasseName())){
                        if(this.ST.containsMethod(method))
                            type = this.ST.getMethodReturn(method);
                        else
                            ErrorHandler.addError("Method " + methodName + argsTypes + " undefined in class " + classType );
                    }
                    // check if method is in imports
                    else{
                        //TODO: check if method is in imports
                        ErrorHandler.addError("Method " + methodName + argsTypes + " undefined in class " + classType );
                    }
                    
                }
            }

        }

        else if (expression instanceof ASTLength) {
            SimpleNode childNode = (SimpleNode) expression.jjtGetChild(0);
            String childType = this.getExpressionType(methodKey, childNode);
            if (childType != null && this.isArrayType(childType))
                type = "int";
            else
                ErrorHandler.addError("Final variable legnth is undefined.");

        }

        return type;
    }

    private String getVarType(String methodKey, String VarId) {
        if (this.ST.containsMethodVariable(methodKey, VarId))
            return this.ST.getMethodVariableType(methodKey, VarId);
        else if (this.ST.containsVariable(VarId))
            return this.ST.getVariableType(VarId);

        ErrorHandler.addError("Variable " + VarId + " undefined.");
        return null;
    }

    private String getSimpleArrayType(String ArrayType) {
        String result = "";

        for (int i = 0; i < ArrayType.length(); i++) {
            if (ArrayType.charAt(i) == '[') {
                break;
            } else {
                result += ArrayType.charAt(i);
            }
        }

        return result;
    }

    private Boolean isArrayVar(String methodKey, String VarId) {
        String type = null;
        if (this.ST.containsMethodVariable(methodKey, VarId)) {
            type = this.ST.getMethodVariableType(methodKey, VarId);
            for (int i = 0; i < type.length(); i++) {
                if (type.charAt(i) == '[') {
                    return true;
                }
            }
        } else if (this.ST.containsVariable(VarId)) {
            type = this.ST.getVariableType(VarId);
            for (int i = 0; i < type.length(); i++) {
                if (type.charAt(i) == '[') {
                    return true;
                }
            }
        } else {
            ErrorHandler.addError("Variable " + VarId + " undefined.");
        }
        return false;
    }

    private Boolean isArrayType(String type) {
        for (int i = 0; i < type.length(); i++) {
            if (type.charAt(i) == '[') {
                return true;
            }
        }
        return false;
    }

    private ArrayList<String> getArgsTypes(String methodKey, ASTFuncArgs argsNode) {
        ArrayList<String> argsTypes = new ArrayList<String>();

        for (int i = 0; i < argsNode.jjtGetNumChildren(); i++) {
            final SimpleNode childNode = (SimpleNode) argsNode.jjtGetChild(i);
            
            argsTypes.add(this.getExpressionType(methodKey, childNode));
        }
        
        return argsTypes;
    }

}