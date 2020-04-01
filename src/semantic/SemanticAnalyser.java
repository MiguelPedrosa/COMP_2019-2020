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
        final SimpleNode classNode = (SimpleNode) this.root.jjtGetChild(this.root.jjtGetNumChildren() -1);

        for(int i = 0; i < classNode.jjtGetNumChildren(); i++){
            // First childs are variables
            final SimpleNode childNode = (SimpleNode) classNode.jjtGetChild(i);
            if(childNode instanceof ASTVarDeclaration) {
                processVarNode((ASTVarDeclaration) childNode);
            }
            // Then methods declarations
            else if(childNode instanceof ASTMethodDeclaration || childNode instanceof ASTMainDeclaration){

            }
        }

        return ST;
    }

    private void processVarNode(ASTVarDeclaration childNode) {
        final String type = childNode.getType();
        final String varID = childNode.getVarId();
        this.ST.addVariable(type, varID);
    }
}