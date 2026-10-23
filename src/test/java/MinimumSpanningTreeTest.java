import algorithms.DisjointSetUnion;
import algorithms.MinimumSpanningTree;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class MinimumSpanningTreeTest {
    @Test void agreesOnOneHundredRandomConnectedGraphs() {
        Random random = new Random(71);
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(70);
            Graph graph = new Graph(n, false);
            for (int v = 1; v < n; v++) graph.addEdge(random.nextInt(v), v, random.nextInt(201) - 100);
            for (int i = 0; i < n * 5; i++) graph.addEdge(random.nextInt(n), random.nextInt(n), random.nextInt(201) - 100);
            var kruskal = MinimumSpanningTree.kruskal(graph, new Metrics());
            var prim = MinimumSpanningTree.prim(graph, new Metrics());
            assertEquals(kruskal.totalWeight(), prim.totalWeight());
            assertTree(n, kruskal);
            assertTree(n, prim);
        }
    }

    private void assertTree(int n, MinimumSpanningTree.Forest forest) {
        assertEquals(n - 1, forest.edges().size());
        assertEquals(1, forest.components());
        DisjointSetUnion dsu = DisjointSetUnion.makeSet(n);
        long sum = 0;
        for (var edge : forest.edges()) {
            assertTrue(dsu.union(edge.from(), edge.to()));
            sum += edge.weight();
        }
        assertEquals(forest.totalWeight(), sum);
        assertEquals(1, dsu.components());
    }

    @Test void agreesWithExhaustiveSpanningTrees() {
        Random random = new Random(9);
        for (int trial = 0; trial < 25; trial++) {
            Graph graph = new Graph(5, false);
            for (int v = 1; v < 5; v++) graph.addEdge(v - 1, v, random.nextInt(21) - 10);
            for (int i = 0; i < 5; i++) graph.addEdge(random.nextInt(5), random.nextInt(5), random.nextInt(21) - 10);
            long best = Long.MAX_VALUE;
            for (int mask = 0; mask < (1 << graph.edgeCount()); mask++) {
                if (Integer.bitCount(mask) != 4) continue;
                DisjointSetUnion dsu = DisjointSetUnion.makeSet(5);
                long weight = 0;
                for (int i = 0; i < graph.edgeCount(); i++) if ((mask & (1 << i)) != 0) {
                    var e = graph.edges().get(i);
                    dsu.union(e.from(), e.to());
                    weight += e.weight();
                }
                if (dsu.components() == 1) best = Math.min(best, weight);
            }
            assertEquals(best, MinimumSpanningTree.kruskal(graph, new Metrics()).totalWeight());
            assertEquals(best, MinimumSpanningTree.prim(graph, new Metrics()).totalWeight());
        }
    }

    @Test void handlesFixturesEmptyGraphsAndOverflow() throws IOException {
        for (String name : new String[]{"disconnected", "connected"}) {
            Graph graph = Graph.read(Path.of("data/" + name + ".txt"));
            var k = MinimumSpanningTree.kruskal(graph, new Metrics());
            Metrics metrics = new Metrics();
            var p = MinimumSpanningTree.prim(graph, metrics);
            assertEquals(name.equals("disconnected") ? 6 : 19, k.totalWeight());
            assertEquals(k.totalWeight(), p.totalWeight());
            assertEquals(name.equals("disconnected") ? 3 : 1, k.components());
            assertEquals(k.components(), p.components());
            assertEquals(graph.vertexCount() - k.components(), k.edges().size());
            assertEquals(2L * graph.edgeCount(), metrics.edgeScans());
        }
        for (int n : new int[]{0, 1, 4}) {
            assertEquals(n, MinimumSpanningTree.kruskal(new Graph(n, false), new Metrics()).components());
            assertEquals(n, MinimumSpanningTree.prim(new Graph(n, false), new Metrics()).components());
        }
        Graph large = new Graph(3, false);
        large.addEdge(0, 1, Integer.MAX_VALUE);
        large.addEdge(1, 2, Integer.MAX_VALUE);
        assertEquals(2L * Integer.MAX_VALUE, MinimumSpanningTree.kruskal(large, new Metrics()).totalWeight());
        assertEquals(2L * Integer.MAX_VALUE, MinimumSpanningTree.prim(large, new Metrics()).totalWeight());
        assertThrows(IllegalArgumentException.class, () -> MinimumSpanningTree.prim(new Graph(1, true), new Metrics()));
        assertThrows(IllegalArgumentException.class, () -> MinimumSpanningTree.kruskal(new Graph(1, true), new Metrics()));
    }
}
