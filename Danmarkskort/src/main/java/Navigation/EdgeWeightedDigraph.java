package Navigation;

import java.util.HashSet;

public class EdgeWeightedDigraph {
    private static final String NEWLINE = System.getProperty("line.separator");

    private final int V;                // number of vertices in this digraph
    private int E;                      // number of edges in this digraph
    private HashSet<DirectedEdge>[] adj; // adj[v] = adjacency set for vertex v
    private int[] indegree;             // indegree[v] = indegree of vertex v

    public EdgeWeightedDigraph(int V) {
        if (V < 0) throw new IllegalArgumentException("Number of vertices in a Digraph must be non-negative");
        this.V = V;
        this.E = 0;
        this.indegree = new int[V];
        adj = (HashSet<DirectedEdge>[]) new HashSet[V];
        for (int v = 0; v < V; v++) //for hver vertex, instantiér et array med adjacent edges for vertex'et
            adj[v] = new HashSet<>();
    }

    public void addEdge(DirectedEdge e) {
        int v = e.from();
        int w = e.to();
        validateVertex(v);
        validateVertex(w);
        if (adj[v].add(e)) { // Only increment E if the edge is newly added
            indegree[w]++;
            E++;
        }
    }

    public Iterable<DirectedEdge> adj(int v) {
        validateVertex(v);
        return adj[v];
    }

    public Iterable<DirectedEdge> edges() {
        HashSet<DirectedEdge> list = new HashSet<>();
        for (int v = 0; v < V; v++) {
            list.addAll(adj[v]);
        }
        return list;
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }


    public int getV(){
        return V;
    }
}
