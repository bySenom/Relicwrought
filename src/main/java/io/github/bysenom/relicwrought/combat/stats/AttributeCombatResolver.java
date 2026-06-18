package io.github.bysenom.relicwrought.combat.stats;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.progression.CharacterAttribute;

import java.util.Map;

public final class AttributeCombatResolver {

    private AttributeCombatResolver() {
    }

    public static CharacterCombatStats resolve(Map<CharacterAttribute, Integer> totalAttributes, ArpgModConfig config) {
        int strength = totalAttributes.getOrDefault(CharacterAttribute.STRENGTH, 0);
        int dexterity = totalAttributes.getOrDefault(CharacterAttribute.DEXTERITY, 0);
        int intelligence = totalAttributes.getOrDefault(CharacterAttribute.INTELLIGENCE, 0);
        int vitality = totalAttributes.getOrDefault(CharacterAttribute.VITALITY, 0);

        double physDamagePercent = strength * config.strengthPhysicalDamagePerPoint();
        double armor = strength * config.strengthArmorPerPoint();

        double attackSpeedPercent = dexterity * config.dexterityAttackSpeedPerPoint();
        double critChance = dexterity * config.dexterityCritChancePerPoint();

        double elemDamagePercent = intelligence * config.intelligenceElementalDamagePerPoint();
        double resistance = intelligence * config.intelligenceResistancePerPoint();

        double life = vitality * config.vitalityLifePerPoint();
        double lifeRegen = vitality * 0.1; // Hardcoded fallback or could add config later

        return new CharacterCombatStats(
                0, 0, 0, 0, 0,
                physDamagePercent, elemDamagePercent,
                attackSpeedPercent, critChance, 0,
                0, 0,
                armor, life, resistance, resistance, resistance, resistance,
                0, 0,
                0, lifeRegen, 0
        );
    }
}
