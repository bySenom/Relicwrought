package io.github.bysenom.relicwrought.loot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LootItemLevelConfigTest {
    @Test
    void clampsMinimumBelowOneToOne() {
        var config = new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.FIXED, 0, 500, 5
        );
        assertEquals(1, config.minimum());
    }

    @Test
    void clampsMaximumAbove950To950() {
        var config = new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.FIXED, 1, 1000, 5
        );
        assertEquals(950, config.maximum());
    }

    @Test
    void adjustsMaximumBelowMinimum() {
        var config = new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.FIXED, 100, 50, 5
        );
        assertEquals(100, config.maximum());
    }

    @Test
    void clampsNegativeVarianceToZero() {
        var config = new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.FIXED, 1, 500, -5
        );
        assertEquals(0, config.randomVariance());
    }

    @Test
    void preservesValidConfig() {
        var config = new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 10, 200, 5
        );
        assertEquals(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, config.type());
        assertEquals(10, config.minimum());
        assertEquals(200, config.maximum());
        assertEquals(5, config.randomVariance());
    }
}
