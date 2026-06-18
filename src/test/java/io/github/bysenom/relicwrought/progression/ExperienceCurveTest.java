package io.github.bysenom.relicwrought.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ExperienceCurveTest {
    private final ExperienceCurve curve = new ExperienceCurve(100, 1.65);

    @Test
    void level1Requires0Xp() {
        assertEquals(0, curve.xpForLevel(1));
    }

    @Test
    void level2RequiresPositiveXp() {
        assertTrue(curve.xpForLevel(2) > 0);
    }

    @Test
    void level50RequiresXp() {
        long xp = curve.xpForLevel(50);
        assertTrue(xp > 10_000);
        assertTrue(xp < 10_000_000);
    }

    @Test
    void level99RequiresXp() {
        long xp = curve.xpForLevel(99);
        assertTrue(xp > 100_000);
    }

    @Test
    void monotonicGrowth() {
        long prev = 0;
        for (int i = 2; i <= 100; i++) {
            long current = curve.xpForLevel(i);
            assertTrue(current > prev, "XP must increase at level " + i);
            prev = current;
        }
    }

    @Test
    void noNegativeXp() {
        for (int i = 1; i <= 100; i++) {
            assertTrue(curve.xpForLevel(i) >= 0, "XP must not be negative at level " + i);
        }
    }

    @Test
    void noOverflow() {
        long xp = curve.xpForLevel(100);
        assertTrue(xp > 0 && xp < Long.MAX_VALUE);
    }

    @Test
    void sameProfileSameValues() {
        ExperienceCurve same = new ExperienceCurve(100, 1.65);
        for (int i = 1; i <= 100; i++) {
            assertEquals(curve.xpForLevel(i), same.xpForLevel(i));
        }
    }

    @Test
    void invalidProfileIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ExperienceCurve(0, 1.65));
        assertThrows(IllegalArgumentException.class, () -> new ExperienceCurve(-1, 1.65));
        assertThrows(IllegalArgumentException.class, () -> new ExperienceCurve(100, 0));
        assertThrows(IllegalArgumentException.class, () -> new ExperienceCurve(100, -1));
    }

    @Test
    void maxLevelReturnedForMaxLevelXp() {
        assertEquals(100, curve.maxLevelForXp(Long.MAX_VALUE));
    }

    @Test
    void totalXpForLevel1Is0() {
        assertEquals(0, curve.totalXpForLevel(1));
    }
}
