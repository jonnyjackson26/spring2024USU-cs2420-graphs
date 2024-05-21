import java.io.File;
import java.util.*;

public class Graph {
    private int vertexCt;  // Number of vertices in the graph.
    private int[][] capacity;  // Adjacency  matrix
    private int[][] residual; // residual matrix
    private int[][] edgeCost; // cost of edges in the matrix
    private String graphName;  //The file from which the graph was created.
    private int totalFlow; // total achieved flow
    private int source = 0; // start of all paths
    private int sink; // end of all paths
    private int[] pred; // Predecessor array
    private boolean[] visited;

    /** Consturctor. takes a file path and builds Graph object **/
    public Graph(String fileName) {
        this.vertexCt = 0;
        source  = 0;
        this.graphName = "";
        makeGraph(fileName);
        pred = new int[vertexCt];
        Arrays.fill(pred,-1);
        this.visited = new boolean[vertexCt];
        Arrays.fill(visited, false);
    }

    /**
     * Method to add an edge
     *
     * @param source      start of edge
     * @param destination end of edge
     * @param cap         capacity of edge
     * @param weight      weight of edge, if any
     * @return edge created
     */
    private boolean addEdge(int source, int destination, int cap, int weight) {
        if (source < 0 || source >= vertexCt) return false;
        if (destination < 0 || destination >= vertexCt) return false;
        capacity[source][destination] = cap;
        residual[source][destination] = cap;
        edgeCost[source][destination] = weight;
        edgeCost[destination][source] = -weight;
        return true;
    }

    /**
     * Method to get a visual of the graph
     *
     * @return the visual
     */
    public String printMatrix(String label, int[][] m) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n " + label+ " \n     ");
        for (int i=0; i < vertexCt; i++)
            sb.append(String.format("%5d", i));
        sb.append("\n");
        for (int i = 0; i < vertexCt; i++) {
            sb.append(String.format("%5d",i));
            for (int j = 0; j < vertexCt; j++) {
                sb.append(String.format("%5d",m[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Method to make the graph
     *
     * @param filename of file containing data
     */
    private void makeGraph(String filename) {
        try {
            graphName = filename;
            System.out.println("\n****Find Flow " + filename);
            Scanner reader = new Scanner(new File(filename));
            vertexCt = reader.nextInt();
            capacity = new int[vertexCt][vertexCt];
            residual = new int[vertexCt][vertexCt];
            edgeCost = new int[vertexCt][vertexCt];
            for (int i = 0; i < vertexCt; i++) {
                for (int j = 0; j < vertexCt; j++) {
                    capacity[i][j] = 0;
                    residual[i][j] = 0;
                    edgeCost[i][j] = 0;
                }
            }

            // If weights, need to grab them from file
            while (reader.hasNextInt()) {
                int v1 = reader.nextInt();
                int v2 = reader.nextInt();
                int cap = reader.nextInt();
                int weight = reader.nextInt();
                if (!addEdge(v1, v2, cap, weight))
                    throw new Exception();
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        sink = vertexCt - 1;
        //System.out.println( printMatrix("Edge Cost" ,edgeCost));
    }


    public void minCostMaxFlow(){
        System.out.println( printMatrix("Capacity", capacity));
        findWeightedFlow();
        System.out.println(printMatrix("Residual", residual));
        //finalEdgeFlow();
        System.out.println(fordFulkerson());
    }

    /**
     *
     * @return if there is an augmenting cheapest path or not
     */
    private boolean hasAugmentingCheapestPath() {
        // Clear predecessors
        Arrays.fill(pred, -1);

        // Set costs to high value
        int[] costs = new int[vertexCt];
        Arrays.fill(costs, Integer.MAX_VALUE);
        costs[this.source] = 0;

        // Loop over vertices
        for (int i = 0; i < vertexCt; i++) {
            for (int u = 0; u < vertexCt; u++) {
                for (int v = 0; v < vertexCt; v++) {
                    // Check if edge from u to v exists and creates a cheaper path
                    if (capacity[u][v] > 0 && costs[u] != Integer.MAX_VALUE && costs[v] > costs[u] + edgeCost[u][v]) {
                        costs[v] = costs[u] + edgeCost[u][v];
                        pred[v] = u;
                    }
                }
            }
        }

        // Check if a path to t exists
        return pred[this.sink] != -1;
    }

    /*
    goes through once and prints the first best path to take from source to sink.
     */
    public void findWeightedFlow() { //bellmanFord
        if (!hasAugmentingCheapestPath()) {
            System.out.println("Theres no augmenting path");
            return;
        }

        // Reconstruct path
        int current = this.sink;
        while (current != this.source) {
            int previous = pred[current];
            System.out.println(String.format("Edge from %d to %d", previous, current));
            current = previous;
        }
    }

    public int fordFulkerson() { //maxFlow
        //from her notes
//        totalFlow= 0;
//        while (hasAugmentingPath(G, s, t)) {
//        // compute Maximum possible flow starting terminal node
//            double availFlow = Double.POSITIVE_INFINITY;
//            for (int v = t; v != s; v=prev){
//                prev = pred[v];
//                availFlow = Math.min(availFlow, residual[prev][v])
//            }
//        // Actually push the flow (forward and backward edges residual[prev][v])
//            for (int v = t; v != s; v=prev){
//                prev = pred[v];
//                update residual by availFlow on forward AND backward edges.
//            }
//            totalFlow += availFlow;
//        }
        int maxFlow = 0;

        while (hasAugmentingCheapestPath()) {
            // Find minimum residual capacity along the path
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = pred[v]) {
                int u = pred[v];
                pathFlow = Math.min(pathFlow, capacity[u][v]);
            }

            // Update residual capacities and augment flow
            for (int v = sink; v != source; v = pred[v]) {
                int u = pred[v];
                capacity[u][v] -= pathFlow;
                capacity[v][u] += pathFlow;
            }

            // Add path flow to overall flow
            maxFlow += pathFlow;
        }

        return maxFlow;
    }

    /**
     * Prints the best flows to take for the graph in this format
     * Flow 0 -> 1 fits: 5, cost: 0
     */
    public void printFinalFlowOnEachEdge() {
        System.out.println("Final flow on each edge");
        while (hasAugmentingCheapestPath()) {
            // Reconstruct path
            int current = this.sink;
            while (current != this.source) {
                int previous = pred[current];
                int cost= edgeCost[previous][current];
                int cap=capacity[previous][current];
                capacity[previous][current]-=cap;
                System.out.println(String.format("Flow %d -> %d fits: %d, cost: %d", previous, current, cap, cost));
                current = previous;
            }
        }
    }




    public void bonus() {
        System.out.println("bonus problem: mincut of flow10");

    }

    public static void main(String[] args) {
        String[] files = {"txtFiles/transport0.txt", "txtFiles/transport1.txt", "txtFiles/transport2.txt", "txtFiles/transport3.txt", "txtFiles/flow10.txt"};
        for (String fileName : files) {
            Graph graph = new Graph(fileName);
            System.out.println(graph.printMatrix(fileName+" edge cost",graph.edgeCost));
            System.out.println(graph.printMatrix(fileName+" residual",graph.residual));
            System.out.println(graph.printMatrix(fileName+" capacity",graph.capacity));
            graph.printFinalFlowOnEachEdge();
        }

        //bonus: min cut for Flow10
        Graph graph = new Graph("txtFiles/flow10.txt");
        graph.bonus();
    }
}