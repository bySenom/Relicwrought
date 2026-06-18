package io.github.bysenom.relicwrought.item.scaling;

public record WeaponBaseStats(double minimumDamage, double maximumDamage, double attacksPerSecond, long maximumDurability) {
    public WeaponBaseStats {
        NumberSafety.requireFiniteNonNegative(minimumDamage, "weapon minimum damage");
        NumberSafety.requireFiniteNonNegative(maximumDamage, "weapon maximum damage");
        NumberSafety.requireFiniteNonNegative(attacksPerSecond, "weapon attacks per second");
        if (maximumDamage < minimumDamage) {
            throw new IllegalArgumentException("Weapon maximum damage must be >= minimum damage");
        }
        if (maximumDurability < 0L) {
            throw new IllegalArgumentException("Weapon durability must not be negative");
        }
    }
}
