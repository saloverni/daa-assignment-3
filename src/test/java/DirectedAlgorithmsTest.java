import algorithms.Kahn;
import algorithms.Kosaraju;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class DirectedAlgorithmsTest {
    @Test void sccAgreesWithAllPairsReachability() {
        Random random = new Random(123);
        for (int trial = 0; trial < 50; trial++) {
            int n = trial == 49 ? 200 : 1 + random.nextInt(70);
            Graph g = new Graph(n, true);
            for (int i = 0; i < n * (trial % 5); i++) g.addEdge(random.nextInt(n), random.nextInt(n), 1);
            Metrics metrics = new Metrics();
            var result = Kosaraju.compute(g, metrics);
            boolean[][] reach = reachability(g);
            boolean[][] reverseReach = reachability(g.reversed());
            for (int u = 0; u < n; u++) for (int v = 0; v < n; v++)
                assertEquals(reach[u][v] && reverseReach[u][v], result.component(u) == result.component(v));
            assertEquals(result.count(), java.util.Arrays.stream(result.componentOf()).distinct().count());
            assertEquals(2L * g.edgeCount(), metrics.edgeScans());
        }
    }

    private boolean[][] reachability(Graph g) {
        int n = g.vertexCount();
        boolean[][] reach = new boolean[n][n];
        for (int source = 0; source < n; source++) {
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(source);
            reach[source][source] = true;
            while (!queue.isEmpty()) for (var edge : g.outgoing(queue.remove())) {
                if (!reach[source][edge.to()]) {
                    reach[source][edge.to()] = true;
                    queue.add(edge.to());
                }
            }
        }
        return reach;
    }

    @Test void pathOfOneHundredThousandVerticesIsStackSafe() {
        int n = 100_000;
        Graph path = new Graph(n, true);
        for (int v = 1; v < n; v++) path.addEdge(v - 1, v, 1);
        Metrics metrics = new Metrics();
        assertEquals(n, Kosaraju.compute(path, metrics).count());
        assertEquals(n, metrics.maxDepth());
        assertEquals(2L * (n - 1), metrics.edgeScans());
    }

    @Test void validatesTopologicalOrderAndRejectsCycles() throws IOException {
        Graph dag = Graph.read(Path.of("data/dag.txt"));
        Metrics metrics = new Metrics();
        int[] order = Kahn.order(dag, metrics);
        assertTrue(Kahn.isValidOrder(dag, order));
        assertEquals(dag.edgeCount(), metrics.edgeScans());
        assertFalse(Kahn.isValidOrder(dag, new int[]{0, 1, 2}));
        assertFalse(Kahn.isValidOrder(dag, new int[]{0, 1, 2, 3, 4, 4}));
        assertFalse(Kahn.isValidOrder(dag, new int[]{0, 1, 2, 3, 4, 9}));
        assertFalse(Kahn.isValidOrder(dag, new int[]{5, 4, 3, 2, 1, 0}));
        Graph cyclic = Graph.read(Path.of("data/cyclic.txt"));
        assertEquals(3, Kosaraju.compute(cyclic, new Metrics()).count());
        assertThrows(Kahn.CycleDetectedException.class, () -> Kahn.order(cyclic, new Metrics()));
        Graph loop = new Graph(1, true);
        loop.addEdge(0, 0, 1);
        assertThrows(Kahn.CycleDetectedException.class, () -> Kahn.order(loop, new Metrics()));
    }

    @Test void handlesEmptyIsolatedAndParallelEdges() {
        assertEquals(0, Kosaraju.compute(new Graph(0, true), new Metrics()).count());
        assertEquals(0, Kahn.order(new Graph(0, true), new Metrics()).length);
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 1, 2);
        assertEquals(4, Kosaraju.compute(g, new Metrics()).count());
        assertTrue(Kahn.isValidOrder(g, Kahn.order(g, new Metrics())));
        assertThrows(IllegalArgumentException.class, () -> Kosaraju.compute(new Graph(1, false), new Metrics()));
        assertThrows(IllegalArgumentException.class, () -> Kahn.order(new Graph(1, false), new Metrics()));
    }
}
