package graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Adjacency-list multigraph. Logical undirected edges are stored once in edges(). */
public final class Graph {
    public record Edge(int from, int to, int weight) { }

    private final boolean directed;
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges = new ArrayList<>();
    private final int[] indegree;

    public Graph(int vertices, boolean directed) {
        if (vertices < 0) throw new IllegalArgumentException("Negative vertex count");
        this.directed = directed;
        indegree = new int[vertices];
        adjacency = new ArrayList<>(vertices);
        for (int i = 0; i < vertices; i++) adjacency.add(new ArrayList<>());
    }

    public int vertexCount() { return adjacency.size(); }
    public int edgeCount() { return edges.size(); }
    public boolean directed() { return directed; }
    public List<Edge> edges() { return Collections.unmodifiableList(edges); }
    public List<Edge> outgoing(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }
    public int indegree(int vertex) { checkVertex(vertex); return indegree[vertex]; }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);
        indegree[to]++;
        if (!directed) {
            adjacency.get(to).add(new Edge(to, from, weight));
            indegree[from]++;
        }
    }

    public void requireDirected(boolean expected) {
        if (directed != expected)
            throw new IllegalArgumentException(expected ? "Directed graph required" : "Undirected graph required");
    }

    public void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount())
            throw new IndexOutOfBoundsException("Vertex " + vertex + " outside 0.." + (vertexCount() - 1));
    }

    public Graph reversed() {
        requireDirected(true);
        Graph reverse = new Graph(vertexCount(), true);
        for (Edge e : edges) reverse.addEdge(e.to(), e.from(), e.weight());
        return reverse;
    }

    /** Strict assignment format: n m directed|undirected, followed by exactly m edges. */
    public static Graph read(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null) throw new IOException("Missing graph header");
            String[] fields = header.trim().split("\\s+");
            if (fields.length != 3 || !(fields[2].equals("directed") || fields[2].equals("undirected")))
                throw new IOException("Expected: n m directed|undirected");
            int n = Integer.parseInt(fields[0]);
            int m = Integer.parseInt(fields[1]);
            if (n < 0 || m < 0) throw new IOException("Negative graph size");
            Graph graph = new Graph(n, fields[2].equals("directed"));
            for (int i = 0; i < m; i++) {
                String line = reader.readLine();
                if (line == null) throw new IOException("Missing edge " + i);
                String[] edge = line.trim().split("\\s+");
                if (edge.length != 3) throw new IOException("Invalid edge " + i);
                graph.addEdge(Integer.parseInt(edge[0]), Integer.parseInt(edge[1]), Integer.parseInt(edge[2]));
            }
            String line;
            while ((line = reader.readLine()) != null)
                if (!line.isBlank()) throw new IOException("Unexpected data after " + m + " edges");
            return graph;
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
            throw new IOException("Invalid graph: " + ex.getMessage(), ex);
        }
    }
}
