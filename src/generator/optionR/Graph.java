import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {

    private HashMap<String, GraphNode> nodes;
    private int Kcolors;

    public Graph(List<NodeR> statments, HashMap<String, Integer> varNames, int Kcolors) {
        this.Kcolors = Kcolors;
        this.nodes = new HashMap<>();
        this.setup(statments, varNames);
    }

    private void setup(List<NodeR> statments, HashMap<String, Integer> varNames) {
        for (Map.Entry<String, Integer> entry : varNames.entrySet()) {
            final GraphNode newNode = new GraphNode();
            nodes.put(entry.getKey(), newNode);
        }

        /**
         * Statment 0: in{}; out{4, 5, ..}
         * Statment 1: in{}; out{4, 5, ..}
         * Statment 2: in{}; out{4, 5, ..}
         */

    }

    public void printNodes() {
        for (Map.Entry<String, GraphNode> entry : nodes.entrySet()) {
            System.out.printf("Node: %s\n",entry.getKey());
            entry.getValue().printNode();
        }
    }
}