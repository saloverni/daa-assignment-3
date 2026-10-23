package algorithms;

import graph.Graph;
import metrics.Metrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DagShortestPath {
    private DagShortestPath() { }
    public record Paths(long[] distances, int[] parents) {
        public Paths { distances = distances.clone(); parents = parents.clone(); }
        @Override public long[] distances() { return distances.clone(); }
        @Override public int[] parents() { return parents.clone(); }
        public long distance(int vertex) { return distances[vertex]; }
        public int parent(int vertex) { return parents[vertex]; }
        public long sum() { return Arrays.stream(distances).sum(); }
        public List<Integer> pathTo(int vertex) {
            if (vertex < 0 || vertex >= distances.length) throw new IndexOutOfBoundsException(vertex);
            List<Integer> path = new ArrayList<>();
            for (int v = vertex; v != -1; v = parents[v]) path.add(v);
            Collections.reverse(path);
            return List.copyOf(path);
        }
    }

    /** Timing includes topological sorting; supplied counters measure relaxation only. */
    public static Paths compute(Graph graph, Metrics metrics) {
        int[] order = Kahn.order(graph, new Metrics());
        long[] distance = new long[graph.vertexCount()];
        int[] parent = new int[graph.vertexCount()];
        Arrays.fill(distance, Long.MAX_VALUE);
        Arrays.fill(parent, -1);
        for (int v = 0; v < graph.vertexCount(); v++)
            if (graph.indegree(v) == 0) distance[v] = 0;
        for (int v : order) {
            for (var edge : graph.outgoing(v)) {
                metrics.scanEdge();
                // Every DAG vertex is reachable from at least one zero-indegree source.
                long candidate = distance[v] + edge.weight();
                metrics.compareWeight();
                if (candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    parent[edge.to()] = v;
                }
            }
        }
        return new Paths(distance, parent);
    }
}
