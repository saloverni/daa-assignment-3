package algorithms;

import graph.Graph;
import metrics.Metrics;
import java.util.Arrays;

/** Two stack-safe DFS passes; one adjacency edge scan per pass. */
public final class Kosaraju {
    private Kosaraju() { }
    public record Components(int[] componentOf, int count) {
        public Components { componentOf = componentOf.clone(); }
        @Override public int[] componentOf() { return componentOf.clone(); }
        public int component(int vertex) { return componentOf[vertex]; }
    }

    public static Components compute(Graph graph, Metrics metrics) {
        graph.requireDirected(true);
        int n = graph.vertexCount();
        boolean[] seen = new boolean[n];
        int[] finish = new int[n];
        int[] stack = new int[n];
        int[] nextEdge = new int[n];
        int finished = 0;
        for (int root = 0; root < n; root++) {
            if (seen[root]) continue;
            int depth = 1;
            stack[0] = root;
            nextEdge[0] = 0;
            seen[root] = true;
            metrics.depth(depth);
            while (depth > 0) {
                int vertex = stack[depth - 1];
                var outgoing = graph.outgoing(vertex);
                if (nextEdge[depth - 1] < outgoing.size()) {
                    int to = outgoing.get(nextEdge[depth - 1]++).to();
                    metrics.scanEdge();
                    if (!seen[to]) {
                        seen[to] = true;
                        stack[depth] = to;
                        nextEdge[depth] = 0;
                        metrics.depth(++depth);
                    }
                } else {
                    finish[finished++] = vertex;
                    depth--;
                }
            }
        }
        // Construction work is timed, but edgeScans measures DFS edge visits only.
        Graph reversed = graph.reversed();
        int[] component = new int[n];
        Arrays.fill(component, -1);
        int count = 0;
        for (int i = n - 1; i >= 0; i--) {
            int root = finish[i];
            if (component[root] != -1) continue;
            int depth = 1;
            stack[0] = root;
            nextEdge[0] = 0;
            component[root] = count;
            metrics.depth(depth);
            while (depth > 0) {
                int vertex = stack[depth - 1];
                var outgoing = reversed.outgoing(vertex);
                if (nextEdge[depth - 1] < outgoing.size()) {
                    int to = outgoing.get(nextEdge[depth - 1]++).to();
                    metrics.scanEdge();
                    if (component[to] == -1) {
                        component[to] = count;
                        stack[depth] = to;
                        nextEdge[depth] = 0;
                        metrics.depth(++depth);
                    }
                } else depth--;
            }
            count++;
        }
        return new Components(component, count);
    }
}
