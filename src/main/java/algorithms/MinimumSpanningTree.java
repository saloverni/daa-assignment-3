package algorithms;

import graph.Graph;
import graph.Graph.Edge;
import metrics.Metrics;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class MinimumSpanningTree {
    private MinimumSpanningTree() { }
    public record Forest(List<Edge> edges, long totalWeight, int components) {
        public Forest { edges = List.copyOf(edges); }
    }

    private static Comparator<Edge> byWeight(Metrics metrics) {
        return (a, b) -> {
            metrics.compareWeight();
            return Integer.compare(a.weight(), b.weight());
        };
    }

    public static Forest kruskal(Graph graph, Metrics metrics) {
        graph.requireDirected(false);
        List<Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort(byWeight(metrics));
        DisjointSetUnion dsu = new DisjointSetUnion(graph.vertexCount(), metrics);
        List<Edge> selected = new ArrayList<>();
        long total = 0;
        for (Edge edge : sorted) {
            if (dsu.components() <= 1) break;
            metrics.scanEdge();
            // union performs both finds and reports whether the endpoints were distinct.
            if (dsu.union(edge.from(), edge.to())) {
                selected.add(edge);
                total += edge.weight();
            }
        }
        return new Forest(selected, total, dsu.components());
    }

    /** Lazy edge-queue Prim: start at 0, then restart at each unvisited component. */
    public static Forest prim(Graph graph, Metrics metrics) {
        graph.requireDirected(false);
        boolean[] visited = new boolean[graph.vertexCount()];
        PriorityQueue<Edge> queue = new PriorityQueue<>(byWeight(metrics));
        List<Edge> selected = new ArrayList<>();
        long total = 0;
        int components = 0;
        for (int root = 0; root < graph.vertexCount(); root++) {
            if (visited[root]) continue;
            components++;
            visit(graph, root, visited, queue, metrics);
            while (!queue.isEmpty()) {
                Edge edge = queue.remove();
                if (visited[edge.to()]) continue;
                selected.add(edge);
                total += edge.weight();
                visit(graph, edge.to(), visited, queue, metrics);
            }
        }
        return new Forest(selected, total, components);
    }

    private static void visit(Graph graph, int vertex, boolean[] visited,
                              PriorityQueue<Edge> queue, Metrics metrics) {
        visited[vertex] = true;
        for (Edge edge : graph.outgoing(vertex)) {
            metrics.scanEdge();
            if (!visited[edge.to()]) queue.add(edge);
        }
    }
}
