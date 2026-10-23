package benchmark;

import graph.Graph;
import java.util.Random;

/** Assignment generator: every graph starts with a fresh java.util.Random(42). */
public final class GraphGenerator {
    private GraphGenerator() { }
    public enum Kind { UNDIRECTED, DIRECTED, DAG }

    public static Graph generate(int n, int m, Kind kind) {
        if (n < 0 || m < 0 || (n < 2 && m != 0)) throw new IllegalArgumentException("Invalid graph size");
        if (kind == Kind.UNDIRECTED && m < n - 1) throw new IllegalArgumentException("Not enough edges for spanning tree");
        Random random = new Random(42);
        Graph graph = new Graph(n, kind != Kind.UNDIRECTED);
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[i] = i;
        if (kind != Kind.DIRECTED) {
            for (int i = n - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int temp = rank[i]; rank[i] = rank[j]; rank[j] = temp;
            }
        }
        if (kind == Kind.UNDIRECTED) {
            for (int i = 1; i < n; i++)
                graph.addEdge(rank[random.nextInt(i)], rank[i], 1 + random.nextInt(1000));
        }
        while (graph.edgeCount() < m) {
            int a = random.nextInt(n), b = random.nextInt(n);
            while (b == a) b = random.nextInt(n);
            if (kind == Kind.DAG && rank[a] > rank[b]) { int temp = a; a = b; b = temp; }
            graph.addEdge(a, b, 1 + random.nextInt(1000));
        }
        return graph;
    }
}
