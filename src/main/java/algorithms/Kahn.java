package algorithms;

import graph.Graph;
import metrics.Metrics;
import java.util.ArrayDeque;

public final class Kahn {
    private Kahn() { }
    public static final class CycleDetectedException extends IllegalArgumentException {
        public CycleDetectedException() { super("Directed graph contains a cycle; no topological order exists"); }
    }

    public static int[] order(Graph graph, Metrics metrics) {
        graph.requireDirected(true);
        int n = graph.vertexCount();
        int[] degree = new int[n];
        int[] order = new int[n];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            degree[v] = graph.indegree(v);
            if (degree[v] == 0) queue.add(v);
        }
        int count = 0;
        while (!queue.isEmpty()) {
            int v = queue.remove();
            order[count++] = v;
            for (var edge : graph.outgoing(v)) {
                metrics.scanEdge();
                if (--degree[edge.to()] == 0) queue.add(edge.to());
            }
        }
        if (count != n) throw new CycleDetectedException();
        return order;
    }

    public static boolean isValidOrder(Graph graph, int[] order) {
        graph.requireDirected(true);
        int n = graph.vertexCount();
        if (order.length != n) return false;
        int[] position = new int[n];
        java.util.Arrays.fill(position, -1);
        for (int i = 0; i < n; i++) {
            int v = order[i];
            if (v < 0 || v >= n || position[v] != -1) return false;
            position[v] = i;
        }
        for (var edge : graph.edges()) if (position[edge.from()] >= position[edge.to()]) return false;
        return true;
    }
}
