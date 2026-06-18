package io.github.bysenom.relicwrought.item.scaling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ItemQualityTest {
    @Test
    void acceptsBoundaryValues() {
        assertEquals(0, ItemQuality.of(0).percent());
        assertEquals(20, ItemQuality.of(20).percent());
    }

    @Test
    void rejectsValuesOutsideAllowedRange() {
        assertThrows(IllegalArgumentException.class, () -> ItemQuality.of(-1));
        assertThrows(IllegalArgumentException.class, () -> ItemQuality.of(21));
    }

    @Test
    void exposesDeterministicMultiplier() {
        assertEquals(1.0D, ItemQuality.of(0).multiplier());
        assertEquals(1.2D, ItemQuality.of(20).multiplier(), 0.00001D);
    }
}
