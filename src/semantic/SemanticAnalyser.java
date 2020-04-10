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
        for(int i = 0; i < this.root.jjtGetNumChildren() -1; i++){
            // First childs are import
        }
        // class node
        SimpleNode classNode = (SimpleNode) this.root.jjtGetChild(this.root.jjtGetNumChildren() -1);

        for(int i = 0; i < classNode.jjtGetNumChildren(); i++){
            // First childs are variables
            final SimpleNode childNode = (SimpleNode) classNode.jjtGetChild(i);
            if(childNode instanceof ASTVarDeclaration) {
                processVarNode((ASTVarDeclaration) childNode);
            }
            // Main declaration
            else if(childNode instanceof ASTMainDeclaration){
                processMainNode((ASTMainDeclaration) childNode);
            } 
            // Methods declarations
            else if(childNode instanceof ASTMethodDeclaration){
                processMethodNode((ASTMethodDeclaration) childNode);
            }
        }

        return ST;
    }

    private void processVarNode(ASTVarDeclaration childNode) {
        String type = childNode.getType();
        String varID = childNode.getVarId();
        this.ST.addVariable(type, varID);
    }

    private void processMainNode(ASTMainDeclaration mainNode) {
        this.ST.addMain();

        for(int i = 0; i < mainNode.jjtGetNumChildren(); i++){
            final SimpleNode childNode = (SimpleNode) mainNode.jjtGetChild(i);
            // First childs are local variables
            if(childNode instanceof ASTVarDeclaration) {
                processLocalVarNode("main", (ASTVarDeclaration) childNode);
            } else if(childNode instanceof ASTEquals){
                processEquals((ASTEquals) childNode);
            }
        }
    }

    private void processMethodNode(ASTMethodDeclaration methodNode) {
        String returnType = methodNode.getReturnType();
        String methodName = methodNode.getMethodName();
        List<String> argumentTypes = methodNode.getArgumentTypes();
        this.ST.addMethod(returnType, methodName, argumentTypes);

        for(int i = 0; i < methodNode.jjtGetNumChildren(); i++){
            final SimpleNode childNode = (SimpleNode) methodNode.jjtGetChild(i);
            // First childs are local variables
            if(childNode instanceof ASTVarDeclaration) {
                processLocalVarNode(methodName, (ASTVarDeclaration) childNode);
            } else if(childNode instanceof ASTEquals) {
                processEquals((ASTEquals) childNode);
            }
        }
    }

    private void processLocalVarNode(String MethodName, ASTVarDeclaration childNode) {
        String type = childNode.getType();
        String varID = childNode.getVarId();
        this.ST.addLocalVariable(MethodName, type, varID);
    }

    private void processEquals(ASTEquals node) {
        if(node.jjtGetNumChildren() == 2){
            String equalsId;
            Object equalsVal;
/*
            if(node.jjtGetChild(0) instanceof ASTEqualsId){
                ASTEqualsId childNode = (ASTEqualsId) node.jjtGetChild(0);
                equalsId = childNode.getIdentifier();
                System.out.println(equalsId);
            }
            if(node.jjtGetChild(1) instanceof ASTExpression){
                ASTExpression expression = (ASTExpression) node.jjtGetChild(1);
                equalsVal = processExpression(expression);
                System.out.println(equalsVal);
            }
*/
        } else{
            System.out.println(MyUtils.ANSI_RED + "Error num childs of equals node." + MyUtils.ANSI_RESET);
        }
    }
/*
    private Object processExpression(SimpleNode node) {
        Object finalVal = null;
        if(node instanceof ASTLiteral){
            return ((ASTLiteral) node).getLiteral();
        }
        for(int i = 0; i < node.jjtGetNumChildren(); i++){
            SimpleNode childNode = (SimpleNode) node.jjtGetChild(i);
            finalVal = processExpression(childNode);
        }

        return finalVal;
    }
*/
}