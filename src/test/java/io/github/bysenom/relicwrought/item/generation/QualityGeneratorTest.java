package io.github.bysenom.relicwrought.item.generation;

import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

class QualityGeneratorTest {
    final QualityGenerator generator = new QualityGenerator();

    @Test
    void explicitQualityIsPreserved() {
        assertEquals(5, generator.resolveQuality(5, new SplittableRandom(42)));
        assertEquals(0, generator.resolveQuality(0, new SplittableRandom(42)));
        assertEquals(20, generator.resolveQuality(20, new SplittableRandom(42)));
    }

    @Test
    void rejectsInvalidExplicitQuality() {
        assertThrows(IllegalArgumentException.class, () ->
                generator.resolveQuality(-1, new SplittableRandom(42))
        );
        assertThrows(IllegalArgumentException.class, () ->
                generator.resolveQuality(21, new SplittableRandom(42))
        );
    }

    @Test
    void automaticQualityIsInRange() {
        for (long seed = 1; seed < 1000; seed++) {
            int q = generator.generateQuality(new SplittableRandom(seed));
            assertTrue(q >= 0 && q <= 20, "Quality out of range for seed " + seed + ": " + q);
        }
    }

    @Test
    void deterministicQuality() {
        int q1 = generator.resolveQuality(null, new SplittableRandom(12345));
        int q2 = generator.resolveQuality(null, new SplittableRandom(12345));
        assertEquals(q1, q2);
    }

    @Test
    void quality20IsPossible() {
        boolean found = false;
        for (long seed = 0; seed < 50000; seed++) {
            if (generator.generateQuality(new SplittableRandom(seed)) == 20) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Quality 20 should be reachable within 50000 seeds");
    }
}
