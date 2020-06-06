import java.util.ArrayList;
import java.util.List;

public class Graph {

    private List<GraphNode> nodes;

    public Graph(List<NodeR> statments) {
        this.nodes = new ArrayList<>();
        this.setup(statments);
    }

    private void setup(List<NodeR> statments){
        for(NodeR nodeR: statments){
            List<Integer> activeVars = nodeR.getActiveVars();
            for(Integer activeVar: activeVars)
                if(!containsNode(activeVar))
                    nodes.add(new GraphNode(activeVar));
        }
    }

    private Boolean containsNode(Integer var){
        for(GraphNode node: nodes)
            if(node.getVar() == var)
                return true;

        return false;
    }
}