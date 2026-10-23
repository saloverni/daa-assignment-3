import algorithms.DisjointSetUnion;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class DisjointSetUnionTest {
    @Test void randomUnionsAgreeWithRelabelingReference() {
        int n = 80;
        int[] labels = java.util.stream.IntStream.range(0, n).toArray();
        DisjointSetUnion dsu = DisjointSetUnion.makeSet(n);
        Random random = new Random(15);
        for (int step = 0; step < 600; step++) {
            int a = random.nextInt(n), b = random.nextInt(n);
            int old = labels[b], replacement = labels[a];
            assertEquals(old != replacement, dsu.union(a, b));
            for (int i = 0; i < n; i++) if (labels[i] == old) labels[i] = replacement;
            assertEquals(Arrays.stream(labels).distinct().count(), dsu.components());
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    assertEquals(labels[i] == labels[j], dsu.find(i) == dsu.find(j));
        }
    }

    @Test void countsFindCallsAndOnlySuccessfulUnions() {
        Metrics metrics = new Metrics();
        DisjointSetUnion dsu = new DisjointSetUnion(3, metrics);
        assertTrue(dsu.union(0, 1));
        assertFalse(dsu.union(1, 0));
        dsu.find(1);
        assertEquals(5, metrics.findCalls());
        assertEquals(1, metrics.successfulUnions());
        assertEquals(6, metrics.dsuOps());
        assertEquals(2, dsu.components());
        assertEquals(0, DisjointSetUnion.makeSet(0).components());
        assertThrows(IndexOutOfBoundsException.class, () -> dsu.find(3));
        assertThrows(IllegalArgumentException.class, () -> DisjointSetUnion.makeSet(-1));
    }
}
