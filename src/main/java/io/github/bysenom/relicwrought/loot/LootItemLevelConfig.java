package io.github.bysenom.relicwrought.loot;

public record LootItemLevelConfig(
        LootItemLevelType type,
        int minimum,
        int maximum,
        int randomVariance
) {
    public LootItemLevelConfig {
        if (minimum < 1) minimum = 1;
        if (maximum < minimum) maximum = minimum;
        if (maximum > 950) maximum = 950;
        if (randomVariance < 0) randomVariance = 0;
    }

    public enum LootItemLevelType {
        SOURCE_SCALED,
        FIXED,
        RANDOM
    }
}
