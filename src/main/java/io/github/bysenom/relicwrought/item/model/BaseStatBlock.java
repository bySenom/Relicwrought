package io.github.bysenom.relicwrought.item.model;

public record BaseStatBlock(
        double damageMin,
        double damageMax,
        double attackSpeed,
        double armor,
        int durability,
        double miningSpeed,
        int miningTier
) {
    public BaseStatBlock {
        if (damageMin < 0.0D || damageMax < 0.0D || damageMax < damageMin) {
            throw new IllegalArgumentException("Invalid damage range");
        }
        if (attackSpeed < 0.0D || armor < 0.0D || durability < 0 || miningSpeed < 0.0D || miningTier < 0) {
            throw new IllegalArgumentException("Base stats must not be negative");
        }
    }

    public static BaseStatBlock empty() {
        return new BaseStatBlock(0.0D, 0.0D, 0.0D, 0.0D, 0, 0.0D, 0);
    }
}
