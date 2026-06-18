package io.github.bysenom.relicwrought.item.scaling;

public record ArmorBaseStats(double armor, long maximumDurability) {
    public ArmorBaseStats {
        NumberSafety.requireFiniteNonNegative(armor, "armor value");
        if (maximumDurability < 0L) {
            throw new IllegalArgumentException("Armor durability must not be negative");
        }
    }
}
