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
            }
        }
    }

    private void processLocalVarNode(String MethodName, ASTVarDeclaration childNode) {
        String type = childNode.getType();
        String varID = childNode.getVarId();
        this.ST.addLocalVariable(MethodName, type, varID);
    }
}