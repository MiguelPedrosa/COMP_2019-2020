import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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

        return ST;
    }

    /* ---------------------------------SIGN FUNCTIONS--------------------------------------- */

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
        String key = methodNode.getKey();
        String returnType = methodNode.getReturnType();
        String methodName = methodNode.getMethodName();
        LinkedHashMap<String, String> arguments = methodNode.getArguments();

        if(! this.ST.addMethod(key, methodName, arguments, returnType) ) {
            System.out.println(MyUtils.ANSI_RED + "ERROR: Repeted method:" + methodName + MyUtils.ANSI_RESET);
        }

    }

    /* ---------------------------------PROCESS NODES--------------------------------------- */

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
        String key = methodNode.getKey();

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
            
            equalsId = list.get(0);
            equalsIdType = list.get(1);
            equalsValType = this.getExpressionType(methodKey, (SimpleNode) node.jjtGetChild(1));

            if (equalsIdType != null && equalsValType != null && equalsIdType.equals(equalsValType))
                this.ST.initializeVariable(methodKey, equalsId);

            // verifica se filho = pai então aceita apesar de tipos serem diferentes
            else if (equalsIdType != null && equalsValType != null && equalsIdType.equals(this.ST.getClasseName())
                    && equalsValType.equals(this.ST.getClassExtendsName()))
                this.ST.initializeVariable(methodKey, equalsId);

            else
                System.out.println(MyUtils.ANSI_RED + "ERROR: Incorrect types." + MyUtils.ANSI_RESET);

        } else
            System.out.println(
                    MyUtils.ANSI_RED + "ERROR: Incorrect number of childs in equals node." + MyUtils.ANSI_RESET);

    }

    /* --------------------------------- EXTRA --------------------------------------- */

    private ArrayList<String> getEqualsIdType(String methodKey, SimpleNode equalsIdNode) {
        ArrayList<String> list = new ArrayList<String>();
        String equalsId = null;
        String equalsIdType = null;

        if (equalsIdNode instanceof ASTIdentifier) {
            equalsId = ((ASTIdentifier) equalsIdNode).getIdentifier();
            equalsIdType = this.getVarType(methodKey, equalsId);
        }
        else if (equalsIdNode instanceof ASTArrayAccess) {
            // TODO: when equalsId is an array
        }

        list.add(equalsId);
        list.add(equalsIdType);
        return list;
    }

    private String getExpressionType(String methodKey, SimpleNode expression) {
        String type = null;

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

        return type;
    }

    private String getVarType(String methodKey, String VarId) {
        if (this.ST.containsMethodVariable(methodKey, VarId))
            return this.ST.getMethodVariableType(methodKey, VarId);
        else if (this.ST.containsVariable(VarId))
            return this.ST.getVariableType(VarId);

        System.out.println(MyUtils.ANSI_RED + "ERROR: Variable " + VarId + " undefined." + MyUtils.ANSI_RESET);
        return null;
    }
}