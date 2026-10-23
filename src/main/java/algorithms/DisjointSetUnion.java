package algorithms;

import metrics.Metrics;
import java.util.Objects;

/** Union by size with iterative full path compression. */
public final class DisjointSetUnion {
    private final int[] parent;
    private final int[] size;
    private final Metrics metrics;
    private int components;

    public DisjointSetUnion(int n, Metrics metrics) {
        if (n < 0) throw new IllegalArgumentException("Negative set count");
        this.metrics = Objects.requireNonNull(metrics);
        parent = new int[n];
        size = new int[n];
        components = n;
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
    }

    public static DisjointSetUnion makeSet(int n) { return new DisjointSetUnion(n, new Metrics()); }
    public int components() { return components; }

    public int find(int x) {
        if (x < 0 || x >= parent.length) throw new IndexOutOfBoundsException("Invalid element " + x);
        metrics.find();
        int root = x;
        while (root != parent[root]) root = parent[root];
        while (x != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    public boolean union(int x, int y) {
        int a = find(x), b = find(y);
        if (a == b) return false;
        if (size[a] < size[b]) { int temp = a; a = b; b = temp; }
        parent[b] = a;
        size[a] += size[b];
        components--;
        metrics.union();
        return true;
    }
}
