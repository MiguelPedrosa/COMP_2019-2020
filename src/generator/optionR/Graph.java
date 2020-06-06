import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Graph {

    private HashMap<String, GraphNode> nodes;
    private HashMap<Integer, Integer> indexColor;

    private int Kcolors;

    public Graph(List<NodeR> statments, HashMap<String, Integer> varNames, int Kcolors) {
        this.Kcolors = Kcolors;
        this.nodes = new HashMap<>();
        this.indexColor = new HashMap<>();
        this.setup(statments, varNames);
    }

    public void colorGraph(List<String[]> arguments) {
        final Stack<String> nodeStorage = new Stack<>();
        String nextNodeName = null;
        
        while((nextNodeName = getNextVisibleNode()) != null) {
            nodes.get(nextNodeName).setHidden(true);
            nodeStorage.push(nextNodeName);
        }
        int count = 1;
        while(! nodeStorage.empty()) {
            nextNodeName = nodeStorage.pop();
            final GraphNode nextNode = nodes.get(nextNodeName);
            final int nextColor = nextNode.calculateColor(Kcolors);
            nextNode.setColor(nextColor);
            nextNode.setHidden(false);
        }

        //1. Find color of this
        //2. Replace of indexs of same color with 0
        //3. n = 0; index = 1;
        //3. Find color of arg[n]; n++
        //4. If node.index == -1:
        //4.1. Replace of indexs of same color with {index}; {index}++
        //5. l = 0
        //6. Find color of local[l]; l++
        //7. If node.index == -1:
        //7.1. Replace of indexs of same color with {index}; {index}++
        

    }

    public HashMap<String, GraphNode> getNodes() {
        return nodes;
    }

    private void setup(List<NodeR> statments, HashMap<String, Integer> varNames) {
        for (Map.Entry<String, Integer> entry : varNames.entrySet()) {
            final GraphNode newNode = new GraphNode();
            nodes.put(entry.getKey(), newNode);
        }

        for(NodeR statment : statments) {
            final BitSet in = (BitSet) statment.getIn().clone();
            final BitSet out = (BitSet) statment.getOut().clone();
            for(int i = 0; i < in.size() || i < out.size(); i++) {
                final String varName = getVarName(varNames, i);
                if (in.get(i)) {
                    for (int j = 0; j < in.size(); j++) {
                        if(in.get(j) && i != j) {
                            final String neighbourName = getVarName(varNames, j);
                            final GraphNode neighbour = nodes.get(neighbourName);
                            nodes.get(varName).addNeighbour(neighbour);
                        }
                    }
                }
                if(out.get(i)) {
                    for (int j = 0; j < out.size(); j++) {
                        if(out.get(j) && i != j) {
                            final String neighbourName = getVarName(varNames, j);
                            final GraphNode neighbour = nodes.get(neighbourName);
                            nodes.get(varName).addNeighbour(neighbour);
                        }
                    }
                }
            }
        }
    }

    private String getVarName(HashMap<String, Integer> varNames, int index) {
        for (Map.Entry<String, Integer> entry : varNames.entrySet()) {
            if(entry.getValue() == index)
                return entry.getKey();
        }
        return null;
    }

    private String getNextVisibleNode() {
        for (Map.Entry<String, GraphNode> entry : this.nodes.entrySet()) {
            if(! entry.getValue().getHidden() &&
                entry.getValue().getTotalVisibleNeighbours() < Kcolors)
                return entry.getKey();
        }
        return null;
    }

    public void printNodes() {
        for (Map.Entry<String, GraphNode> entry : nodes.entrySet()) {
            System.out.printf("Node: %s\n",entry.getKey());
            entry.getValue().printNode();
        }
    }
}