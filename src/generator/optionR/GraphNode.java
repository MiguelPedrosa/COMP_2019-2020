import java.util.ArrayList;
import java.util.List;

public class GraphNode {

    private int var; 
    private List<GraphNode> neighbours; 
    private int color; 
    private Boolean hidden; 

    public GraphNode(int var) {
        this.var = var;
        this.color = -1;
        this.hidden = false;
        this.neighbours = new ArrayList<>();
    }

    public int getVar(){
        return var;
    }

}
