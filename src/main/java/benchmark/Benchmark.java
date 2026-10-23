package benchmark;

import algorithms.DagShortestPath;
import algorithms.Kahn;
import algorithms.Kosaraju;
import algorithms.MinimumSpanningTree;
import graph.Graph;
import metrics.Metrics;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public final class Benchmark {
    private Benchmark() { }
    public static final int[] SIZES = {1_000, 3_162, 10_000, 31_623, 100_000};
    private static final long[] MST = {147970, 30363, 468911, 97589, 1486139, 309550, 4715515, 968517, 15012094, 3056681};
    private static final long[] SCC = {27, 1, 137, 1, 373, 1, 1188, 1, 3923, 1};
    private static final long[] DAG = {396274, 252411, 1271911, 723157, 3941828, 2381342, 12551891, 7376612, 40096539, 23512087};
    private record Measured<T>(T result, double timeMs, Metrics.Snapshot metrics) { }

    /** Validation is outside the timer; each measured run gets new counters and algorithm state. */
    private static <T> Measured<T> measure(Function<Metrics, T> operation,
                                         java.util.function.Consumer<T> validate) {
        long[] nanos = new long[5];
        Metrics.Snapshot expected = null;
        T result = null;
        for (int run = 0; run < nanos.length; run++) {
            Metrics metrics = new Metrics();
            long start = System.nanoTime();
            result = operation.apply(metrics);
            nanos[run] = System.nanoTime() - start;
            validate.accept(result);
            if (expected != null && !expected.equals(metrics.snapshot()))
                throw new IllegalStateException("Non-reproducible operation counters");
            expected = metrics.snapshot();
        }
        Arrays.sort(nanos);
        return new Measured<>(result, nanos[2] / 1_000_000.0, expected);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static void addRow(List<String> rows, String algorithm, String graph, int n, int m,
                               Measured<?> measured, long result) {
        var c = measured.metrics();
        rows.add(String.format(Locale.ROOT, "%s,%s,%d,%d,%.6f,%d,%d,%d,%d,%d", algorithm, graph, n, m,
                measured.timeMs(), c.dsuOps(), c.edgeScans(), c.comparisons(), c.maxDepth(), result));
    }

    public static void main(String[] args) throws IOException {
        Path output = Path.of(args.length == 0 ? "results/results.csv" : args[0]);
        List<String> rows = new ArrayList<>();
        rows.add("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
        System.out.println("n,m,mst_weight,scc_count,dag_distance_sum");
        int index = 0;
        for (int n : SIZES) for (int density : new int[]{4, 20}) {
            int m = n * density;
            String label = density == 4 ? "sparse" : "dense";
            long expectedMst = MST[index], expectedScc = SCC[index], expectedDag = DAG[index];
            Graph undirected = GraphGenerator.generate(n, m, GraphGenerator.Kind.UNDIRECTED);
            var kruskal = measure(metrics -> MinimumSpanningTree.kruskal(undirected, metrics), forest -> {
                require(forest.totalWeight() == expectedMst, "Kruskal reference mismatch at " + n + "/" + m + ": " + forest.totalWeight());
                require(forest.components() == 1 && forest.edges().size() == n - 1, "Invalid Kruskal tree");
            });
            var prim = measure(metrics -> MinimumSpanningTree.prim(undirected, metrics), forest -> {
                require(forest.totalWeight() == kruskal.result().totalWeight(), "MST algorithms disagree");
                require(forest.components() == 1 && forest.edges().size() == n - 1, "Invalid Prim tree");
            });
            require(prim.metrics().edgeScans() == 2L * m, "Prim must scan both orientations");
            addRow(rows, "Kruskal", "undirected_" + label, n, m, kruskal, kruskal.result().totalWeight());
            addRow(rows, "Prim", "undirected_" + label, n, m, prim, prim.result().totalWeight());
            Graph directed = GraphGenerator.generate(n, m, GraphGenerator.Kind.DIRECTED);
            var scc = measure(metrics -> Kosaraju.compute(directed, metrics), result ->
                    require(result.count() == expectedScc, "SCC reference mismatch at " + n + "/" + m + ": " + result.count()));
            require(scc.metrics().edgeScans() == 2L * m, "Kosaraju must scan 2m edges");
            addRow(rows, "Kosaraju", "directed_" + label, n, m, scc, scc.result().count());
            Graph dag = GraphGenerator.generate(n, m, GraphGenerator.Kind.DAG);
            var kahn = measure(metrics -> Kahn.order(dag, metrics), order ->
                    require(Kahn.isValidOrder(dag, order), "Invalid topological order"));
            require(kahn.metrics().edgeScans() == m, "Kahn must scan m edges");
            addRow(rows, "Kahn", "dag_" + label, n, m, kahn, kahn.result().length);
            var paths = measure(metrics -> DagShortestPath.compute(dag, metrics), result ->
                    require(result.sum() == expectedDag, "DAG reference mismatch at " + n + "/" + m + ": " + result.sum()));
            require(paths.metrics().edgeScans() == m, "DAG relaxation must scan m edges");
            addRow(rows, "DagShortestPath", "dag_" + label, n, m, paths, paths.result().sum());
            System.out.printf(Locale.ROOT, "%d,%d,%d,%d,%d%n", n, m, kruskal.result().totalWeight(), scc.result().count(), paths.result().sum());
            index++;
        }
        require(rows.size() == 51, "Expected exactly 50 result rows");
        Files.createDirectories(output.toAbsolutePath().getParent());
        Path temporary = Files.createTempFile(output.toAbsolutePath().getParent(), "benchmark-", ".csv.tmp");
        try {
            Files.write(temporary, rows);
            Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temporary); }
        System.out.println("All references and counters verified; wrote " + output);
    }
}
