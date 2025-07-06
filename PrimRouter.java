package prim;

import java.util.*;

public class PrimRouter {

    public static class MSTEdge {
        String src;    // Source vertex of the edge
        String dest;   // Destination vertex of the edge
        int weight;   // Weight of the edge

        public MSTEdge(String src, String dest, int weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }
    }

    // The graph represented as an adjacency list
    private final Map<String, List<PrimGraphBuilder.Edge>> graph;


    public PrimRouter(Map<String, List<PrimGraphBuilder.Edge>> graph) {
        this.graph = graph;
    }

    public List<MSTEdge> computeMST(String start) {
        List<MSTEdge> result = new ArrayList<>();  // Stores the final MST edges
        Set<String> visited = new HashSet<>();     // Tracks visited vertices
        // Priority queue to always expand the minimum weight edge
        PriorityQueue<MSTEdge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));

        // Start with the initial vertex
        visited.add(start);
        // Add all edges from the starting vertex to the priority queue
        for (PrimGraphBuilder.Edge edge : graph.getOrDefault(start, new ArrayList<>())) {
            pq.add(new MSTEdge(start, edge.dest, edge.weight));
        }

        while (!pq.isEmpty()) {
            MSTEdge edge = pq.poll();  // Get the edge with minimum weight
            if (visited.contains(edge.dest)) continue;  // Skip if already visited

            visited.add(edge.dest);    // Mark destination as visited
            result.add(edge);         // Add edge to MST

            // Add all edges from the new vertex to the priority queue
            for (PrimGraphBuilder.Edge next : graph.getOrDefault(edge.dest, new ArrayList<>())) {
                if (!visited.contains(next.dest)) {
                    pq.add(new MSTEdge(edge.dest, next.dest, next.weight));
                }
            }
        }

        return result;
    }

    public List<String> findPathInMST(String src, String dest, List<MSTEdge> mst) {
        // Build adjacency list from MST edges
        Map<String, List<String>> adj = new HashMap<>();
        for (MSTEdge e : mst) {
            adj.computeIfAbsent(e.src, k -> new ArrayList<>()).add(e.dest);
            adj.computeIfAbsent(e.dest, k -> new ArrayList<>()).add(e.src);
        }

        // BFS setup
        Queue<List<String>> q = new LinkedList<>();  // Queue for BFS paths
        Set<String> visited = new HashSet<>();      // Visited vertices
        q.add(List.of(src));  // Start with path containing only source

        while (!q.isEmpty()) {
            List<String> path = q.poll();
            String last = path.get(path.size() - 1);

            if (last.equals(dest)) return path;  // Path found

            if (visited.contains(last)) continue;
            visited.add(last);

            // Explore all neighbors
            for (String neighbor : adj.getOrDefault(last, new ArrayList<>())) {
                List<String> newPath = new ArrayList<>(path);
                newPath.add(neighbor);
                q.add(newPath);
            }
        }

        return List.of();  // Return empty list if no path found
    }

    public int calculatePathCost(List<String> path, List<MSTEdge> mst) {
        int cost = 0;
        // Iterate through consecutive vertices in the path
        for (int i = 0; i < path.size() - 1; i++) {
            String u = path.get(i), v = path.get(i + 1);
            // Find the edge between u and v in MST
            for (MSTEdge e : mst) {
                if ((e.src.equals(u) && e.dest.equals(v)) || (e.src.equals(v) && e.dest.equals(u))) {
                    cost += e.weight;
                    break;
                }
            }
        }
        return cost;
    }
}