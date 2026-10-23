import algorithms.DagShortestPath;
import algorithms.Kahn;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class DagShortestPathTest {
    @Test void agreesWithBellmanFordOnRandomDagsIncludingNegativeWeights() {
        Random random = new Random(22);
        for (int trial = 0; trial < 100; trial++) {
            int n = 1 + random.nextInt(35);
            int[] perm = java.util.stream.IntStream.range(0, n).toArray();
            for (int i = n - 1; i > 0; i--) { int j = random.nextInt(i + 1); int t = perm[i]; perm[i] = perm[j]; perm[j] = t; }
            Graph g = new Graph(n, true);
            for (int a = 0; a < n; a++) for (int b = a + 1; b < n; b++)
                if (random.nextDouble() < 0.15) g.addEdge(perm[a], perm[b], random.nextInt(101) - 50);
            Metrics metrics = new Metrics();
            var paths = DagShortestPath.compute(g, metrics);
            assertArrayEquals(bellmanFord(g), paths.distances());
            assertEquals(g.edgeCount(), metrics.edgeScans());
            for (int target = 0; target < n; target++) {
                var path = paths.pathTo(target);
                assertEquals(target, path.getLast());
                assertEquals(0, g.indegree(path.getFirst()));
                assertTrue(path.size() <= n);
                for (int i = 1; i < path.size(); i++) {
                    int from = path.get(i - 1), to = path.get(i);
                    assertEquals(from, paths.parent(to));
                    assertTrue(g.outgoing(from).stream().anyMatch(e -> e.to() == to &&
                            paths.distance(from) + e.weight() == paths.distance(to)));
                }
            }
        }
    }

    private long[] bellmanFord(Graph graph) {
        long[] distance = new long[graph.vertexCount()];
        Arrays.fill(distance, Long.MAX_VALUE);
        for (int v = 0; v < distance.length; v++) if (graph.indegree(v) == 0) distance[v] = 0;
        for (int pass = 1; pass < distance.length; pass++) {
            boolean changed = false;
            for (var edge : graph.edges()) {
                if (distance[edge.from()] != Long.MAX_VALUE &&
                        distance[edge.from()] + edge.weight() < distance[edge.to()]) {
                    distance[edge.to()] = distance[edge.from()] + edge.weight();
                    changed = true;
                }
            }
            if (!changed) break;
        }
        return distance;
    }

    @Test void restoresFixturePathAndRejectsCycles() throws IOException {
        var paths = DagShortestPath.compute(Graph.read(Path.of("data/dag.txt")), new Metrics());
        assertArrayEquals(new long[]{0, 0, -2, 1, 5, 3}, paths.distances());
        assertEquals(java.util.List.of(1, 2, 3, 5), paths.pathTo(5));
        assertEquals(7, paths.sum());
        assertThrows(Kahn.CycleDetectedException.class,
                () -> DagShortestPath.compute(Graph.read(Path.of("data/cyclic.txt")), new Metrics()));
    }

    @Test void handlesEmptyGraphsMultipleSourcesParallelEdgesAndLargeWeights() {
        assertEquals(0, DagShortestPath.compute(new Graph(0, true), new Metrics()).sum());
        assertEquals(java.util.List.of(2), DagShortestPath.compute(new Graph(3, true), new Metrics()).pathTo(2));
        Graph g = new Graph(4, true);
        g.addEdge(0, 2, Integer.MAX_VALUE);
        g.addEdge(1, 2, Integer.MAX_VALUE);
        g.addEdge(1, 2, Integer.MAX_VALUE - 1);
        g.addEdge(2, 3, Integer.MAX_VALUE);
        assertEquals(2L * Integer.MAX_VALUE - 1, DagShortestPath.compute(g, new Metrics()).distance(3));
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.compute(new Graph(2, false), new Metrics()));
    }
}
