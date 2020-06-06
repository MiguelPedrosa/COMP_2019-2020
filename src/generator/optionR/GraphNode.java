import java.util.ArrayList;
import java.util.List;

public class GraphNode {

    private List<GraphNode> neighbours; 
    private int color; 
    private Boolean hidden; 

    public GraphNode() {
        this.color = -1;
        this.hidden = false;
        this.neighbours = new ArrayList<>();
    }

    public void addNeighbour(GraphNode newNeightbour) {
        if(!this.neighbours.contains(newNeightbour)) {
            this.neighbours.add(newNeightbour);
        }
    }

    public void printNode() {
        System.out.printf("Color: %d; Hidden: %s; Neighbours: %s\n",
            this.color,
            (hidden ? "true" : "false"),
            this.neighbours);
    }
}
