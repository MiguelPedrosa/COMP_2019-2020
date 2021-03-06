import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SemanticAnalyser {

    private SimpleNode root;
    private SymbolTable ST;
    private String intType = "int";
    private String booleanType = "boolean";

    public SemanticAnalyser(SimpleNode root) {
        this.root = root;
        this.ST = new SymbolTable(null);
    }

    public SymbolTable Start() {
        // Handle Imports
        for (int i = 0; i < this.root.jjtGetNumChildren() - 1; i++) {
            // First childs are import
            if (this.root.jjtGetChild(i) instanceof ASTImportDeclaration) {
                this.ST.addImport((ASTImportDeclaration) this.root.jjtGetChild(i));
            }
        }
        // class node
        SimpleNode classNode = (SimpleNode) this.root.jjtGetChild(this.root.jjtGetNumChildren() - 1);

        if (classNode instanceof ASTClassDeclaration) {

            this.ST.addClasseName(((ASTClassDeclaration) classNode).getClassId());
            this.ST.addClassExtendsName(((ASTClassDeclaration) classNode).getExtendsId());

            List<Integer> list = this.signFuntions(classNode);
            this.processClassNode(classNode, list);
        }

        return ST;
    }

    /*
     * ---------------------------------SIGN
     * FUNCTIONS---------------------------------------
     */

    private List<Integer> signFuntions(SimpleNode classNode) {

        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < classNode.jjtGetNumChildren(); i++) {

            SimpleNode childNode = (SimpleNode) classNode.jjtGetChild(i);

            // First childs are variables
            if (childNode instanceof ASTVarDeclaration) {
                list.add(i);
            }
            // Main declaration
            if (childNode instanceof ASTMainDeclaration) {
                if (signMainNode((ASTMainDeclaration) childNode))
                    list.add(i);
            }
            // Methods declarations
            else if (childNode instanceof ASTMethodDeclaration) {
                if (signMethodNode((ASTMethodDeclaration) childNode))
                    list.add(i);
            }
        }

        return list;
    }

    private boolean signMainNode(ASTMainDeclaration mainNode) {
        if (this.ST.addMain(mainNode.argumentName))
            return true;

        ErrorHandler.addError("Repeated main method.", mainNode.getLine());
        return false;
    }

    private boolean signMethodNode(ASTMethodDeclaration methodNode) {
        String returnType = methodNode.getReturnType();
        String methodName = methodNode.getMethodName();
        List<String[]> arguments = methodNode.getArguments();
        String key = this.getMethodKey(methodName, arguments);

        if (!this.ST.addMethod(key, methodName, arguments, returnType)) {
            ErrorHandler.addError("Repeated method:" + methodName, methodNode.getLine());
            return false;
        }
        else {
            for (String[] argument: arguments)        
                if (classExists(this.getSimpleArrayType(argument[1]), true)) {
                    if (!argument[1].equals("void"))
                        this.ST.addLocalVariable(key, argument[1], argument[0], -1);
                    else
                        ErrorHandler.addError("Variable " + argument[0] + " cant be type void");
                } else
                    ErrorHandler.addError("Class " + argument[1] + " is undefined.");
        }

        this.ST.initializeAllVariables(key);

        return true;
    }

    /*
     * ---------------------------------PROCESS
     * NODES---------------------------------------
     */

    private void processClassNode(SimpleNode classNode, List<Integer> list) {

        for (Integer i : list) {
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

        if (classExists(this.getSimpleArrayType(type), true)) {
            if (!type.equals("void"))
                this.ST.addVariable(type, varID, childNode.getLine());
            else
                ErrorHandler.addError("Variable " + varID + " cant be type void", childNode.getLine());
        } else
            ErrorHandler.addError("Class " + type + " is undefined.", childNode.getLine());
    }

    private void processMainNode(ASTMainDeclaration mainNode) {
        this.processNodes("main", (SimpleNode) mainNode, true);
    }

    private void processMethodNode(ASTMethodDeclaration methodNode) {
        String methodName = methodNode.getMethodName();
        List<String[]> arguments = methodNode.getArguments();
        String key = this.getMethodKey(methodName, arguments);

        this.processNodes(key, (SimpleNode) methodNode, true);
    }

    private void processNodes(String methodKey, SimpleNode node, boolean initialize) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode childNode = (SimpleNode) node.jjtGetChild(i);
            // First childs are local variables
            if (childNode instanceof ASTVarDeclaration) {
                processLocalVarDeclaration(methodKey, (ASTVarDeclaration) childNode);
            } else if (childNode instanceof ASTEquals) {
                processEquals(methodKey, (ASTEquals) childNode, initialize);
            } else if (childNode instanceof ASTWhile) {
                processWhile(methodKey, (ASTWhile) childNode);
            } else if (childNode instanceof ASTScope) {
                this.processNodes(methodKey, childNode, initialize);
            } else if (childNode instanceof ASTIF) {
                this.processIf(methodKey, (ASTIF) childNode);
            } else if (childNode instanceof ASTExpression) {
                this.getExpressionType(methodKey, childNode);
            } else if (childNode instanceof ASTReturn) {
                SimpleNode returnExpression = (SimpleNode) childNode.jjtGetChild(0);
                String foundType = this.getExpressionType(methodKey, returnExpression);
                String expectedType = this.ST.getMethodReturn(methodKey);
                if (foundType == null || !foundType.equals(expectedType))
                    ErrorHandler.addError("Found return type of " + foundType + ". Expected type of " + expectedType
                            + " in method " + this.ST.getMethodName(methodKey), childNode.getLine());
            }
        }
    }

    private void processLocalVarDeclaration(String key, ASTVarDeclaration childNode) {
        String type = childNode.getType();
        String varID = childNode.getVarId();

        if (classExists(this.getSimpleArrayType(type), true)) {
            if (!type.equals("void"))
                this.ST.addLocalVariable(key, type, varID, childNode.getLine());
            else
                ErrorHandler.addError("Variable " + varID + " cant be type void", childNode.getLine());
        } else
            ErrorHandler.addError("Class " + type + " is undefined.", childNode.getLine());
    }

    private void processEquals(String methodKey, ASTEquals node, boolean initialize) {
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

            if (equalsIdType != null && equalsValType != null && equalsIdType.equals(equalsValType)) {
                if (initialize)
                    this.ST.initializeVariable(methodKey, equalsId, 2);
                if (!initialize)
                    this.ST.initializeVariable(methodKey, equalsId, 1);
            }

            else
                ErrorHandler.addError("Incorrect types.", node.getLine());

        } else
            ErrorHandler.addError("Incorrect number of childs in equals node.", node.getLine());

    }

    private void processWhile(String methodKey, ASTWhile node) {
        if (node.jjtGetNumChildren() == 2) {
            String conditionType = this.getExpressionType(methodKey, (SimpleNode) node.jjtGetChild(0));
            if (conditionType == null || !conditionType.equals("boolean"))
                ErrorHandler.addError("Condition type in while must be a boolean.", node.getLine());

            SimpleNode scope = (SimpleNode) node.jjtGetChild(1);
            if (scope instanceof ASTScope)
                this.processNodes(methodKey, (ASTScope) scope, false);

        } else
            ErrorHandler.addError("Incorrect number of childs in while node.", node.getLine());
    }

    private void processIf(String methodKey, ASTIF node) {
        if (node.jjtGetNumChildren() == 3) {
            String conditionType = this.getExpressionType(methodKey, (SimpleNode) node.jjtGetChild(0));
            if (conditionType == null || !conditionType.equals("boolean"))
                ErrorHandler.addError("Condition type in if must be a boolean.", node.getLine());

            SimpleNode ifScope = (SimpleNode) node.jjtGetChild(1);
            this.processNodes(methodKey, ifScope, false);

            SimpleNode elseScope = (SimpleNode) node.jjtGetChild(2);
            this.processNodes(methodKey, elseScope, false);

        } else
            ErrorHandler.addError("Incorrect number of childs in if node.", node.getLine());
    }

    /*
     * --------------------------------- EXTRA
     * ---------------------------------------
     */

    public String getMethodKey(String methodName, List<String[]> arguments) {
        String key = methodName;

        for(String[] argument: arguments)
            key += ";" + argument[1];

        return key;
    }

    public String getMethodKey(String methodName, ArrayList<String> arguments) {
        String key = methodName;

        for (int i = 0; i < arguments.size(); i++)
            key += ";" + arguments.get(i);

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
            if (firstChild instanceof ASTIdentifier) {
                equalsId = ((ASTIdentifier) firstChild).getIdentifier();

            }

            // dealing with second child
            String indexType = this.getExpressionType(methodKey, secondChild);
            if (indexType == null || !indexType.equals(intType)) {

                ErrorHandler.addError("Expected int for index in \"" + equalsId + "\" array.", equalsIdNode.getLine());
                list.add(null);
                list.add(null);
                return list;
            } else {
                if (isArrayVar(methodKey, equalsId)) {
                    equalsIdType = this.getVarType(methodKey, equalsId);
                    equalsIdType = this.getSimpleArrayType(equalsIdType);
                } else
                    ErrorHandler.addError("Variable \"" + equalsId + "\" is not an array.", equalsIdNode.getLine());
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
            if (type.equals("this")) {
                if (methodKey.equals("main"))
                    ErrorHandler.addError("Impossible to use 'this' in static method main.", expression.getLine());
                else
                    return this.ST.getClasseName();
            } else
                return type;
        }

        else if (expression instanceof ASTIdentifier) {
            checkVarInitialized(methodKey, (ASTIdentifier) expression);
            type = getVarType(methodKey, ((ASTIdentifier) expression).getIdentifier());
        }

        else if (expression instanceof ASTNew) {
            SimpleNode childNode = (SimpleNode) expression.jjtGetChild(0);

            if (childNode instanceof ASTExpression) {
                String indexType = getExpressionType(methodKey, childNode);
                if (indexType != null && indexType.equals(intType)) {
                    type = ((ASTNew) expression).getType();
                } else {
                    ErrorHandler.addError("Expected integer on array index.", expression.getLine());                    
                }
            } else if (childNode instanceof ASTIdentifier) {
                type = ((ASTIdentifier) childNode).getIdentifier();

                if (classExists(this.getSimpleArrayType(type), true)) {
                    if (type.equals("void"))
                        ErrorHandler.addError("New usage can't be type void", expression.getLine());
                } else
                    ErrorHandler.addError("Class " + type + " is undefined.", expression.getLine());
            }
        }

        else if (expression instanceof ASTArrayAccess) {
            SimpleNode firstChild = (SimpleNode) expression.jjtGetChild(0);
            SimpleNode secondChild = (SimpleNode) expression.jjtGetChild(1);

            String indexType = this.getExpressionType(methodKey, secondChild);
            String identifier = null;

            // primeiro filho tem que ser um identifier
            if (firstChild instanceof ASTIdentifier) {
                identifier = ((ASTIdentifier) firstChild).getIdentifier();
                // Verifica se o identifier ?? uma var array
                if (this.isArrayVar(methodKey, identifier)) {
                    type = this.getExpressionType(methodKey, firstChild);
                    type = this.getSimpleArrayType(type);
                } else
                    ErrorHandler.addError("Variable \"" + identifier + "\" is not an array.", expression.getLine());
            }

            // segundo filho tem que ser um int
            if (indexType == null || !indexType.equals(intType)) {
                ErrorHandler.addError("Expected int for index in \"" + identifier + "\" array.", expression.getLine());
            }
        }

        else if (expression instanceof ASTFuncCall) {
            // 3 filhos
            SimpleNode firstChild = (SimpleNode) expression.jjtGetChild(0);
            SimpleNode secondChild = (SimpleNode) expression.jjtGetChild(1);
            SimpleNode thirdChild = (SimpleNode) expression.jjtGetChild(2);

            String classType = null;
            boolean isStatic = false;

            if (firstChild instanceof ASTIdentifier) {
                String name = ((ASTIdentifier) firstChild).getIdentifier();

                // checks if it's a var and its type
                classType = this.getExpressionType(methodKey, firstChild);

                // check if is a static class
                if (classType == null && !name.equals(this.ST.getClasseName()) && this.classExists(name, false)) {
                    ErrorHandler.removeLastError();
                    isStatic = true;
                    classType = name;
                } else if(classType == null && name.equals(this.ST.getClasseName())){
                    ErrorHandler.removeLastError();
                    ErrorHandler.addError(name + " does not have static methods.", expression.getLine());
                }
            } else {
                String name = this.getExpressionType(methodKey, firstChild);

                if (this.classExists(name, true))
                    classType = name;
                else
                    ErrorHandler.addError("Undefined class " + classType, expression.getLine());
            }

            // nome do m??todo
            if (secondChild instanceof ASTIdentifier) {
                String methodName = ((ASTIdentifier) secondChild).getIdentifier();

                // verificar se existe m??todo em classe (imports e minha classe)
                ArrayList<String> argsTypes = new ArrayList<String>();

                if (thirdChild instanceof ASTFuncArgs) {
                    argsTypes = this.getArgsTypes(methodKey, (ASTFuncArgs) thirdChild);
                    String method = this.getMethodKey(methodName, argsTypes);
                    // If is this class check if methods exists
                    if (classType != null && classType.equals(this.ST.getClasseName())) {
                        if (this.ST.containsMethod(method))
                            type = this.ST.getMethodReturn(method);
                        else if (this.isMethodInImports(this.ST.getClassExtendsName(), method, isStatic)) {
                            if (isStatic)
                                type = this.ST.getImports().get(this.ST.getClassExtendsName())
                                        .getStaticMethodType(method);
                            else
                                type = this.ST.getImports().get(this.ST.getClassExtendsName()).getMethodType(method);
                        } else
                            ErrorHandler.addError(
                                    "Method " + methodName + argsTypes + " undefined in class " + classType,
                                    secondChild.getLine());
                    }
                    // check if method is in imports
                    else if (classType != null) {

                        if (this.isMethodInImports(classType, method, isStatic)) {
                            if (isStatic)
                                type = this.ST.getImports().get(classType).getStaticMethodType(method);
                            else
                                type = this.ST.getImports().get(classType).getMethodType(method);
                        }

                        else
                            ErrorHandler.addError(
                                    "Method " + methodName + argsTypes + " undefined in class " + classType,
                                    secondChild.getLine());

                    }
                }
            }

        }

        else if (expression instanceof ASTLength) {
            SimpleNode childNode = (SimpleNode) expression.jjtGetChild(0);
            String childType = this.getExpressionType(methodKey, childNode);
            if (childType != null && this.isArrayType(childType))
                type = intType;
            else
                ErrorHandler.addError("Lenght was called on a none array object.", expression.getLine());
        }

        else if (expression instanceof ASTNot) {
            SimpleNode child = (SimpleNode) expression.jjtGetChild(0);
            String childType = this.getExpressionType(methodKey, child);
            if (childType != null && childType.equals(booleanType))
                type = booleanType;
            else
                ErrorHandler.addError("Expected boolean to use '!' operator", expression.getLine());
        }

        else if (expression instanceof ASTTimes || expression instanceof ASTDividor || expression instanceof ASTPlus
                || expression instanceof ASTMinus || expression instanceof ASTLessThan) {
            SimpleNode firstChild = (SimpleNode) expression.jjtGetChild(0);
            SimpleNode secondChild = (SimpleNode) expression.jjtGetChild(1);

            String firstChildType = this.getExpressionType(methodKey, firstChild);
            String secondChildType = this.getExpressionType(methodKey, secondChild);

            if (firstChildType == null || secondChildType == null || !firstChildType.equals(intType)
                    || !secondChildType.equals(intType)) {
                if (expression instanceof ASTTimes)
                    ErrorHandler.addError("Expected the use of ints for the operand '*'.", expression.getLine());
                else if (expression instanceof ASTDividor)
                    ErrorHandler.addError("Expected the use of ints for the operand '/'.", expression.getLine());
                else if (expression instanceof ASTPlus)
                    ErrorHandler.addError("Expected the use of ints for the operand '+'.", expression.getLine());
                else if (expression instanceof ASTMinus)
                    ErrorHandler.addError("Expected the use of ints for the operand '-'.", expression.getLine());
                else if (expression instanceof ASTLessThan)
                    ErrorHandler.addError("Expected the use of ints for the operand '<'.", expression.getLine());
            } else if (expression instanceof ASTLessThan)
                type = booleanType;
            else
                type = intType;
        }

        else if (expression instanceof ASTAnd) {
            SimpleNode firstChild = (SimpleNode) expression.jjtGetChild(0);
            SimpleNode secondChild = (SimpleNode) expression.jjtGetChild(1);

            String firstChildType = this.getExpressionType(methodKey, firstChild);
            String secondChildType = this.getExpressionType(methodKey, secondChild);

            if (firstChildType == null || secondChildType == null || !firstChildType.equals(booleanType)
                    || !secondChildType.equals(booleanType))
                ErrorHandler.addError("Expected the use of booleans for the operand '&&'.", expression.getLine());
            else
                type = booleanType;
        }

        return type;
    }

    private String getVarType(String methodKey, String VarId) {
        
        if (this.ST.containsMethodVariable(methodKey, VarId)) 
            return this.ST.getMethodVariableType(methodKey, VarId);
        else if (this.ST.containsVariable(VarId)) 
            return this.ST.getVariableType(VarId);
    

        ErrorHandler.addError(VarId + " is undefined.");
        return null;
    }

    private void checkVarInitialized(String methodKey, ASTIdentifier var) {
        String varId = ((ASTIdentifier) var).getIdentifier();
        if (this.ST.containsMethodVariable(methodKey, varId)) {
            if (this.ST.getMethodVariable(methodKey, varId).getInitialized() == 0)
                ErrorHandler.addError(
                        "Variable " + this.ST.getMethodVariable(methodKey, varId).getName() + " was not initialized.", var.getLine());
            else if (this.ST.getMethodVariable(methodKey, varId).getInitialized() == 1)
                ErrorHandler.addWarning("Variable " + this.ST.getMethodVariable(methodKey, varId).getName()
                        + " might not be initialized.", var.getLine());

        } else if (this.ST.containsVariable(varId)) {
            if (this.ST.getVariable(varId).getInitialized() == 0)
                ErrorHandler.addError(
                        "Variable " + this.ST.getVariable(varId).getName() + " was not initialized.", var.getLine());
            else if (this.ST.getVariable(varId).getInitialized() == 1)
                ErrorHandler.addWarning("Variable " + this.ST.getVariable(varId).getName()
                        + " might not be initialized.", var.getLine());

        }
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

    private boolean classExists(String className, boolean needsConstructor) {

        if (!needsConstructor) {
            if (this.ST.getImports().containsKey(className))
                return true;
        }

        if (needsConstructor && this.ST.canObjectBeCreated(className))
            return true;

        if (className.equals(this.ST.getClasseName()) || className.equals(this.ST.getClassExtendsName())
                || className.equals(intType) || className.equals("boolean") || className.equals("String")
                || className.equals("void"))
            return true;

        return false;
    }

    private boolean isMethodInImports(String className, String methodKey, boolean isStatic) {

        if (!this.ST.getImports().containsKey(className))
            return false;

        if (isStatic && this.ST.getImports().get(className).hasStaticMethod(methodKey))
            return true;

        if (!isStatic && this.ST.getImports().get(className).hasMethod(methodKey))
            return true;

        return false;
    }

}