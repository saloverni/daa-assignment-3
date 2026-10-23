package benchmark;

import algorithms.DagShortestPath;
import algorithms.Kahn;
import algorithms.Kosaraju;
import algorithms.MinimumSpanningTree;
import graph.Graph;
import metrics.Metrics;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

/** Small-file demonstration for the assignment defense. */
public final class Main {
    private Main() { }
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: ./gradlew run --args='data/dag.txt'");
            return;
        }
        Graph graph = Graph.read(Path.of(args[0]));
        if (!graph.directed()) {
            var kruskal = MinimumSpanningTree.kruskal(graph, new Metrics());
            var prim = MinimumSpanningTree.prim(graph, new Metrics());
            if (kruskal.totalWeight() != prim.totalWeight()) throw new IllegalStateException("MST mismatch");
            System.out.printf("Forest weight: %d; components: %d%n", kruskal.totalWeight(), kruskal.components());
            kruskal.edges().forEach(System.out::println);
        } else {
            var components = Kosaraju.compute(graph, new Metrics());
            System.out.println("SCC count: " + components.count());
            System.out.println("Component IDs: " + Arrays.toString(components.componentOf()));
            try {
                System.out.println("Topological order: " + Arrays.toString(Kahn.order(graph, new Metrics())));
                var paths = DagShortestPath.compute(graph, new Metrics());
                for (int v = 0; v < graph.vertexCount(); v++)
                    System.out.printf("Vertex %d: distance=%d, path=%s%n", v, paths.distance(v), paths.pathTo(v));
            } catch (Kahn.CycleDetectedException ex) { System.out.println(ex.getMessage()); }
        }
    }
}
