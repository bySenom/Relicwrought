package io.github.bysenom.relicwrought.combat.stats;

public record CharacterCombatStats(
        // Offensive flat
        double flatPhysicalDamage,
        double fireDamage,
        double coldDamage,
        double lightningDamage,
        double poisonDamage,

        // Offensive percent
        double physicalDamagePercent,
        double elementalDamagePercent,

        // Attack & Crit
        double attackSpeedPercent,
        double criticalStrikeChance,
        double criticalStrikeMultiplier,

        // Conditional
        double eliteDamageBonus,
        double bossDamageBonus,

        // Defensive
        double armor,
        double maximumLife,
        double fireResistance,
        double coldResistance,
        double lightningResistance,
        double poisonResistance,

        // Reduction
        double flatDamageReduction,
        double percentDamageReduction,

        // Utility
        double movementSpeed,
        double lifeRegeneration,
        double miningSpeedPercent
) {
    public static CharacterCombatStats empty() {
        return new CharacterCombatStats(
                0, 0, 0, 0, 0,
                0, 0,
                0, 0, 1.5, // 150% base crit multiplier
                0, 0,
                0, 0, 0, 0, 0, 0,
                0, 0,
                0, 0, 0
        );
    }
}
