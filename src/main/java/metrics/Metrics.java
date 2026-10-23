package metrics;

/** Per-invocation counters; updated at the operation sites, never estimated. */
public final class Metrics {
    private long findCalls;
    private long successfulUnions;
    private long edgeScans;
    private long comparisons;
    private int maxDepth;

    public void find() { findCalls++; }
    public void union() { successfulUnions++; }
    public void scanEdge() { edgeScans++; }
    public void compareWeight() { comparisons++; }
    public void depth(int depth) { maxDepth = Math.max(maxDepth, depth); }
    public long findCalls() { return findCalls; }
    public long successfulUnions() { return successfulUnions; }
    public long dsuOps() { return findCalls + successfulUnions; }
    public long edgeScans() { return edgeScans; }
    public long comparisons() { return comparisons; }
    public int maxDepth() { return maxDepth; }

    public Snapshot snapshot() {
        return new Snapshot(dsuOps(), edgeScans, comparisons, maxDepth);
    }
    public record Snapshot(long dsuOps, long edgeScans, long comparisons, int maxDepth) { }
}
