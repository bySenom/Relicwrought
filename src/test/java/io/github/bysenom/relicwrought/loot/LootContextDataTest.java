package io.github.bysenom.relicwrought.loot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LootContextDataTest {
    private static final double DELTA = 1e-9;

    @Test
    void computeBaseStrengthUsesFormula() {
        var data = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 100.0, 20.0, 10.0, true, 0
        );
        double expected = (100.0 * 0.4) + (20.0 * 0.3) + (10.0 * 0.3);
        assertEquals(expected, data.computeBaseStrength(), DELTA);
    }

    @Test
    void computeBaseStrengthZeroWhenAllZero() {
        var data = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 0.0, 0.0, 0.0, false, 0
        );
        assertEquals(0.0, data.computeBaseStrength(), DELTA);
    }

    @Test
    void computeBaseStrengthPartialValues() {
        var data = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 50.0, 0.0, 0.0, true, 3
        );
        assertEquals(20.0, data.computeBaseStrength(), DELTA);
    }
}
