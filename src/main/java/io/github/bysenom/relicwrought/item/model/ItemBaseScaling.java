package io.github.bysenom.relicwrought.item.model;

public record ItemBaseScaling(
        DefinitionKey damageProfile,
        DefinitionKey armorProfile,
        DefinitionKey durabilityProfile,
        DefinitionKey miningSpeedProfile,
        DefinitionKey miningTierProfile,
        double minimumDamageMultiplier,
        double maximumDamageMultiplier,
        double armorMultiplier,
        double durabilityMultiplier,
        double miningSpeedMultiplier
) {
    public ItemBaseScaling {
        requirePositive(minimumDamageMultiplier, "minimum damage multiplier");
        requirePositive(maximumDamageMultiplier, "maximum damage multiplier");
        requirePositive(armorMultiplier, "armor multiplier");
        requirePositive(durabilityMultiplier, "durability multiplier");
        requirePositive(miningSpeedMultiplier, "mining speed multiplier");
        if (maximumDamageMultiplier < minimumDamageMultiplier) {
            throw new IllegalArgumentException("Maximum damage multiplier must be >= minimum damage multiplier");
        }
    }

    public static ItemBaseScaling defaults(DefinitionKey fallbackProfile) {
        return new ItemBaseScaling(
                fallbackProfile,
                fallbackProfile,
                fallbackProfile,
                fallbackProfile,
                fallbackProfile,
                0.85D,
                1.15D,
                1.0D,
                1.0D,
                1.0D
        );
    }

    private static void requirePositive(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0.0D) {
            throw new IllegalArgumentException(name + " must be positive and finite: " + value);
        }
    }
}
