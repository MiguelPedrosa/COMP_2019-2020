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

	public int getColor() {
		return color;
	}
	public void setColor(int color) {
		this.color = color;
	}

    public Boolean getHidden() {
        return hidden;
    }
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public int calculateColor(int maxColors) {
		for(int i = 0; i < maxColors; i++) {
			Boolean colorAlreadyExists = false;
			for(GraphNode neighbour : neighbours) {
				if(neighbour.getColor() == i) {
					colorAlreadyExists = true;
				}
			}
			if(! colorAlreadyExists) {
				return i;
			}
		}
		return -1;
    }

    public int getTotalVisibleNeighbours() {
        int count = 0;
        for(GraphNode neighbour : neighbours) {
            if(! neighbour.getHidden()) {
                count++;
            }
        }
        return count;
    }
}
