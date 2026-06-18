package io.github.bysenom.relicwrought.combat.stats;

import io.github.bysenom.relicwrought.ArpgModConfig;

public final class CharacterCombatStatResolver {

    private CharacterCombatStatResolver() {
    }

    public static CharacterCombatStats combine(CharacterCombatStats base, CharacterCombatStats equipped) {
        return new CharacterCombatStats(
                base.flatPhysicalDamage() + equipped.flatPhysicalDamage(),
                base.fireDamage() + equipped.fireDamage(),
                base.coldDamage() + equipped.coldDamage(),
                base.lightningDamage() + equipped.lightningDamage(),
                base.poisonDamage() + equipped.poisonDamage(),
                base.physicalDamagePercent() + equipped.physicalDamagePercent(),
                base.elementalDamagePercent() + equipped.elementalDamagePercent(),
                base.attackSpeedPercent() + equipped.attackSpeedPercent(),
                base.criticalStrikeChance() + equipped.criticalStrikeChance(),
                // base already has 1.5 multiplier, equipped adds flat to it
                base.criticalStrikeMultiplier() + equipped.criticalStrikeMultiplier(),
                base.eliteDamageBonus() + equipped.eliteDamageBonus(),
                base.bossDamageBonus() + equipped.bossDamageBonus(),
                base.armor() + equipped.armor(),
                base.maximumLife() + equipped.maximumLife(),
                base.fireResistance() + equipped.fireResistance(),
                base.coldResistance() + equipped.coldResistance(),
                base.lightningResistance() + equipped.lightningResistance(),
                base.poisonResistance() + equipped.poisonResistance(),
                base.flatDamageReduction() + equipped.flatDamageReduction(),
                base.percentDamageReduction() + equipped.percentDamageReduction(),
                base.movementSpeed() + equipped.movementSpeed(),
                base.lifeRegeneration() + equipped.lifeRegeneration(),
                base.miningSpeedPercent() + equipped.miningSpeedPercent()
        );
    }
}
