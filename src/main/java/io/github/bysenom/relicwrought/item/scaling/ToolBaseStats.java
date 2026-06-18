package io.github.bysenom.relicwrought.item.scaling;

public record ToolBaseStats(double miningSpeed, int miningTier, long maximumDurability) {
    public ToolBaseStats {
        NumberSafety.requireFiniteNonNegative(miningSpeed, "tool mining speed");
        if (miningTier < 0) {
            throw new IllegalArgumentException("Mining tier must not be negative");
        }
        if (maximumDurability < 0L) {
            throw new IllegalArgumentException("Tool durability must not be negative");
        }
    }
}
