import graph.Graph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class GraphTest {
    @TempDir Path temp;

    @Test void readsFixturesAndKeepsLogicalEdges() throws IOException {
        Graph g = Graph.read(Path.of("data/disconnected.txt"));
        assertEquals(6, g.vertexCount());
        assertEquals(5, g.edgeCount());
        assertEquals(0, g.outgoing(5).size());
        assertEquals(10, java.util.stream.IntStream.range(0, 6).map(v -> g.outgoing(v).size()).sum());
        assertFalse(g.directed());
        assertTrue(Graph.read(Path.of("data/cyclic.txt")).directed());
        assertEquals(6, Graph.read(Path.of("data/dag.txt")).edgeCount());
        assertEquals(6, Graph.read(Path.of("data/connected.txt")).edgeCount());
        assertThrows(UnsupportedOperationException.class, () -> g.edges().clear());
    }

    @Test void reversesEdgesAndIndegrees() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, -3);
        g.addEdge(0, 1, 7);
        g.addEdge(1, 1, 1);
        assertEquals(3, g.indegree(1));
        Graph r = g.reversed();
        assertEquals(2, r.indegree(0));
        assertEquals(3, r.outgoing(1).size());
        assertEquals(-3, r.outgoing(1).getFirst().weight());
    }

    @Test void rejectsMalformedFiles() throws IOException {
        for (String contents : new String[]{"", "2 0 invalid", "-1 0 directed", "2 -1 directed",
                "2 1 directed\n0 2 4", "2 1 directed", "2 0 directed\n0 1 2", "2 1 directed\nx 1 2"}) {
            Path file = temp.resolve("invalid.txt");
            Files.writeString(file, contents);
            assertThrows(IOException.class, () -> Graph.read(file), contents);
        }
    }
}
