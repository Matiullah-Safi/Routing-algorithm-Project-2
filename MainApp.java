package prim;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        // Configure GraphStream to use JavaFX renderer
        System.setProperty("org.graphstream.ui", "javafx");

        // Initialize scanner for user input and graph builder
        Scanner scanner = new Scanner(System.in);
        PrimGraphBuilder builder = new PrimGraphBuilder();

        // Prompt user for graph input method
        System.out.print("Do you want to enter a custom graph? (yes/no): ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("yes")) {
            // Custom graph input
            System.out.print("Enter number of nodes: ");
            int numNodes = Integer.parseInt(scanner.nextLine());

            // Add all nodes first
            for (int i = 0; i < numNodes; i++) {
                System.out.print("Enter node name: ");
                builder.getAdjacencyList().putIfAbsent(scanner.nextLine().trim(), new ArrayList<>());
            }

            // Add edges until user types 'done'
            System.out.println("Enter edges (format: A B weight), type 'done' to finish:");
            while (true) {
                String line = scanner.nextLine().trim();
                if (line.equalsIgnoreCase("done")) break;
                String[] parts = line.split("\\s+");
                if (parts.length != 3) {
                    System.out.println("[ERROR] Invalid format.");
                    continue;
                }
                try {
                    builder.addEdge(parts[0], parts[1], Integer.parseInt(parts[2]));
                } catch (NumberFormatException e) {
                    System.out.println("[ERROR] Invalid weight.");
                }
            }
        } else {
            // Default graph (if user doesn't want custom input)
            builder.addEdge("A", "B", 3);
            builder.addEdge("A", "C", 1);
            builder.addEdge("B", "D", 4);
            builder.addEdge("C", "D", 2);
            builder.addEdge("C", "E", 8);
            builder.addEdge("D", "F", 10);
            builder.addEdge("F", "G", 11);
            builder.addEdge("F", "H", 9);
            builder.addEdge("G", "I", 12);
            builder.addEdge("H", "K", 13);
            builder.addEdge("K", "B", 10);
            builder.addEdge("F", "I", 11);
            builder.addEdge("H", "C", 9);
        }

        // Get the built graph and initialize Prim's algorithm router
        Map<String, List<PrimGraphBuilder.Edge>> graph = builder.getAdjacencyList();
        PrimRouter router = new PrimRouter(graph);

        // Get starting node for Prim's algorithm
        System.out.print("Enter starting node for Prim's algorithm: ");
        AtomicReference<String> rootRef = new AtomicReference<>(scanner.nextLine().trim());
        AtomicReference<List<PrimRouter.MSTEdge>> mstRef = new AtomicReference<>(router.computeMST(rootRef.get()));

        // Initialize visualizer and build initial graph
        Visualizer visualizer = new Visualizer();
        visualizer.buildGraph(graph);

        // Find initial path to visualize
        System.out.print("Enter source node: ");
        String src = scanner.nextLine().trim();
        System.out.print("Enter destination node: ");
        String dst = scanner.nextLine().trim();
        List<String> path = router.findPathInMST(src, dst, mstRef.get());

        // Display path information
        if (path == null || path.isEmpty()) {
            System.out.println("[WARN] No path found from " + src + " to " + dst + ".");
        } else {
            int cost = router.calculatePathCost(path, mstRef.get());
            System.out.println("[PATH] " + String.join(" -> ", path));
            System.out.println("[COST] " + cost);
        }

        // Show the visualization on JavaFX thread
        Platform.runLater(() -> {
            visualizer.show();
            visualizer.rebuildGraph(graph, mstRef.get(), path);
        });

        // Start interactive thread for user commands
        new Thread(() -> {
            while (true) {
                System.out.println("\nChoose an option:");
                System.out.println("[0] Add node");
                System.out.println("[1] Add edge");
                System.out.println("[2] Remove node");
                System.out.println("[3] Remove edge");
                System.out.println("[4] Re-run Prim's algorithm");
                System.out.println("[5] Find MST path");
                System.out.println("[6] Exit");
                System.out.print("Enter choice: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "0" -> { // Add node
                        System.out.print("Enter new node name: ");
                        String newNode = scanner.nextLine().trim();
                        if (!graph.containsKey(newNode)) {
                            graph.put(newNode, new ArrayList<>());
                            Platform.runLater(() -> visualizer.rebuildGraph(graph, mstRef.get(), List.of()));
                        } else {
                            System.out.println("[WARN] Node already exists.");
                        }
                    }
                    case "1" -> { // Add edge
                        System.out.print("Enter new edge (A B weight): ");
                        String[] parts = scanner.nextLine().trim().split("\\s+");
                        if (parts.length == 3) {
                            builder.addEdge(parts[0], parts[1], Integer.parseInt(parts[2]));
                            mstRef.set(router.computeMST(rootRef.get()));
                            Platform.runLater(() -> visualizer.rebuildGraph(graph, mstRef.get(), List.of()));
                        }
                    }
                    case "2" -> { // Remove node
                        System.out.print("Enter node to remove: ");
                        String node = scanner.nextLine().trim();
                        graph.remove(node);
                        // Remove all edges connected to this node
                        for (List<PrimGraphBuilder.Edge> edges : graph.values()) {
                            edges.removeIf(e -> e.dest.equals(node));
                        }
                        mstRef.set(router.computeMST(rootRef.get()));
                        Platform.runLater(() -> visualizer.rebuildGraph(graph, mstRef.get(), List.of()));
                    }
                    case "3" -> { // Remove edge
                        System.out.print("Enter edge to remove (A B): ");
                        String[] parts = scanner.nextLine().trim().split("\\s+");
                        graph.getOrDefault(parts[0], new ArrayList<>()).removeIf(e -> e.dest.equals(parts[1]));
                        graph.getOrDefault(parts[1], new ArrayList<>()).removeIf(e -> e.dest.equals(parts[0]));
                        mstRef.set(router.computeMST(rootRef.get()));
                        Platform.runLater(() -> visualizer.rebuildGraph(graph, mstRef.get(), List.of()));
                    }
                    case "4" -> { // Change Prim's root
                        System.out.print("Enter new root for Prim's algorithm: ");
                        rootRef.set(scanner.nextLine().trim());
                        mstRef.set(router.computeMST(rootRef.get()));
                        Platform.runLater(() -> visualizer.rebuildGraph(graph, mstRef.get(), List.of()));
                    }
                    case "5" -> { // Find new path
                        System.out.print("Enter source: ");
                        String newSrc = scanner.nextLine().trim();
                        System.out.print("Enter destination: ");
                        String newDst = scanner.nextLine().trim();
                        List<String> newPath = router.findPathInMST(newSrc, newDst, mstRef.get());

                        if (newPath == null || newPath.isEmpty()) {
                            System.out.println("[WARN] No path found from " + newSrc + " to " + newDst + ".");
                        } else {
                            int newCost = router.calculatePathCost(newPath, mstRef.get());
                            System.out.println("[PATH] " + String.join(" -> ", newPath));
                            System.out.println("[COST] " + newCost);
                            Platform.runLater(() -> visualizer.rebuildGraph(graph, mstRef.get(), newPath));
                        }
                    }
                    case "6" -> { // Exit
                        System.out.println("[EXIT] Exiting simulator.");
                        System.exit(0);
                    }
                    default -> System.out.println("[WARN] Invalid choice.");
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}