package prim;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

import java.util.*;

public class Visualizer {
    private final Graph graph;  // The GraphStream graph object for visualization


    public Visualizer() {
        // Set the GraphStream UI renderer to JavaFX
        System.setProperty("org.graphstream.ui", "javafx");
        // Create a new single graph with a title
        graph = new SingleGraph("Prim Routing Simulator");
        // Enable quality rendering and anti-aliasing for better visuals
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
    }

    public void buildGraph(Map<String, List<PrimGraphBuilder.Edge>> adjList) {
        // Create all nodes first
        for (String node : adjList.keySet()) {
            if (graph.getNode(node) == null) {
                Node n = graph.addNode(node);
                n.setAttribute("ui.label", node);  // Set node label
                // Style nodes with yellow color, black border, etc.
                n.setAttribute("ui.style",
                        "fill-color: #fdd835; size: 45px; text-size: 20px; " +
                                "text-color: white; stroke-mode: plain; stroke-color: black;");
            }
        }

        // Track added edges to avoid duplicates (since graph is undirected)
        Set<String> added = new HashSet<>();
        // Create all edges
        for (String src : adjList.keySet()) {
            for (PrimGraphBuilder.Edge e : adjList.get(src)) {
                String id = src + "-" + e.dest;
                String rev = e.dest + "-" + src;
                // Only add edge if not already added (in either direction)
                if (!added.contains(id) && !added.contains(rev)) {
                    Edge edge = graph.addEdge(id, src, e.dest, true);  // true = undirected
                    edge.setAttribute("ui.label", e.weight);  // Show edge weight
                    // Style edges with gray color
                    edge.setAttribute("ui.style",
                            "fill-color: #9e9e9e; size: 1px; text-size: 14px;");
                    added.add(id);
                }
            }
        }
    }

    public void highlightMST(List<PrimRouter.MSTEdge> edges) {
        for (PrimRouter.MSTEdge e : edges) {
            // Try both edge directions since graph is undirected
            Edge edge = graph.getEdge(e.src + "-" + e.dest);
            if (edge == null) edge = graph.getEdge(e.dest + "-" + e.src);
            if (edge != null) {
                // Style MST edges with green color and thicker line
                edge.setAttribute("ui.style",
                        "fill-color: #43a047; size: 3px; text-size: 14px;");
            }
        }
    }

    public void highlightBestPath(List<String> path) {
        for (int i = 0; i < path.size(); i++) {
            // Highlight nodes in the path with blue color
            Node node = graph.getNode(path.get(i));
            if (node != null) {
                node.setAttribute("ui.style",
                        "fill-color: #1e88e5; size: 45px; text-size: 20px; " +
                                "text-color: white; stroke-mode: plain; stroke-color: black;");
            }

            // Highlight edges between path nodes with red color
            if (i < path.size() - 1) {
                String u = path.get(i), v = path.get(i + 1);
                String id1 = u + "-" + v;
                String id2 = v + "-" + u;
                Edge edge = graph.getEdge(id1);
                if (edge == null) edge = graph.getEdge(id2);
                if (edge != null) {
                    edge.setAttribute("ui.style",
                            "fill-color: #e53935; size: 4px; text-size: 14px;");
                }
            }
        }
    }

    public void rebuildGraph(Map<String, List<PrimGraphBuilder.Edge>> adjList,
                             List<PrimRouter.MSTEdge> mstEdges,
                             List<String> path) {
        graph.clear();  // Clear existing visualization
        buildGraph(adjList);  // Rebuild base graph
        highlightMST(mstEdges);  // Highlight MST
        highlightBestPath(path);  // Highlight path
    }

    public void show() {
        graph.display();  // Show the visualization
    }
}