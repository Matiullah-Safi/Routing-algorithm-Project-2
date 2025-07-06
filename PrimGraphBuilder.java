package prim;

import java.util.*;

public class PrimGraphBuilder {

    public static class Edge {
        String dest;       // The destination vertex of the edge
        int weight;        // The weight/cost associated with the edge

        public Edge(String dest, int weight) {
            this.dest = dest;
            this.weight = weight;
        }
    }

    // The adjacency list to represent the graph
    // Key: Vertex name (String)
    // Value: List of edges connected to that vertex
    private final Map<String, List<Edge>> adjacencyList = new HashMap<>();

    public void addEdge(String u, String v, int weight) {
        // Ensure both vertices exist in the adjacency list
        adjacencyList.putIfAbsent(u, new ArrayList<>());
        adjacencyList.putIfAbsent(v, new ArrayList<>());

        // Add the edge from u to v
        adjacencyList.get(u).add(new Edge(v, weight));
        // Add the edge from v to u (since the graph is undirected)
        adjacencyList.get(v).add(new Edge(u, weight));
    }

    public Map<String, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }
}