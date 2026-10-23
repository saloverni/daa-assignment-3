import algorithms.DagShortestPath;
import algorithms.Kahn;
import algorithms.Kosaraju;
import algorithms.MinimumSpanningTree;
import benchmark.GraphGenerator;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BenchmarkTest {
    @Test void reproducesSmallReferenceValuesAndExactScanCounts() {
        for (int density : new int[]{4, 20}) {
            int n = 1000, m = density * n;
            Graph undirected = GraphGenerator.generate(n, m, GraphGenerator.Kind.UNDIRECTED);
            long weight = density == 4 ? 147970 : 30363;
            Metrics kruskal = new Metrics(), prim = new Metrics(), scc = new Metrics(), kahn = new Metrics(), paths = new Metrics();
            assertEquals(weight, MinimumSpanningTree.kruskal(undirected, kruskal).totalWeight());
            assertEquals(weight, MinimumSpanningTree.prim(undirected, prim).totalWeight());
            assertEquals(2L * kruskal.edgeScans() + n - 1, kruskal.dsuOps());
            assertEquals(2L * m, prim.edgeScans());
            Graph directed = GraphGenerator.generate(n, m, GraphGenerator.Kind.DIRECTED);
            assertEquals(density == 4 ? 27 : 1, Kosaraju.compute(directed, scc).count());
            assertEquals(2L * m, scc.edgeScans());
            Graph dag = GraphGenerator.generate(n, m, GraphGenerator.Kind.DAG);
            assertTrue(Kahn.isValidOrder(dag, Kahn.order(dag, kahn)));
            assertEquals(m, kahn.edgeScans());
            assertEquals(density == 4 ? 396274 : 252411, DagShortestPath.compute(dag, paths).sum());
            assertEquals(m, paths.edgeScans());
            assertEquals(m, paths.comparisons());
        }
    }

    @Test void generatorIsDeterministicAndUsesRequiredWeightRange() {
        for (var kind : GraphGenerator.Kind.values()) {
            Graph a = GraphGenerator.generate(80, 320, kind);
            Graph b = GraphGenerator.generate(80, 320, kind);
            assertEquals(a.edges(), b.edges());
            assertEquals(320, a.edgeCount());
            assertTrue(a.edges().stream().allMatch(e -> e.from() != e.to() && e.weight() >= 1 && e.weight() <= 1000));
        }
        assertThrows(IllegalArgumentException.class, () -> GraphGenerator.generate(1, 1, GraphGenerator.Kind.DAG));
        assertThrows(IllegalArgumentException.class, () -> GraphGenerator.generate(5, 2, GraphGenerator.Kind.UNDIRECTED));
    }
}
