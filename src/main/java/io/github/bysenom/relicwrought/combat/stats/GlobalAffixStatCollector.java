package io.github.bysenom.relicwrought.combat.stats;

import io.github.bysenom.relicwrought.item.model.AffixComponentRoll;
import io.github.bysenom.relicwrought.item.model.AffixOperation;
import io.github.bysenom.relicwrought.item.model.AffixRoll;
import io.github.bysenom.relicwrought.item.model.AffixScope;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;

public final class GlobalAffixStatCollector {

    private GlobalAffixStatCollector() {
    }

    public static CharacterCombatStats collect(ArpgItemData itemData) {
        double flatPhysicalDamage = 0;
        double fireDamage = 0;
        double coldDamage = 0;
        double lightningDamage = 0;
        double poisonDamage = 0;

        double physicalDamagePercent = 0;
        double elementalDamagePercent = 0;

        double attackSpeedPercent = 0;
        double criticalStrikeChance = 0;
        double criticalStrikeMultiplier = 0;

        double eliteDamageBonus = 0;
        double bossDamageBonus = 0;

        double armor = 0;
        double maximumLife = 0;
        double fireResistance = 0;
        double coldResistance = 0;
        double lightningResistance = 0;
        double poisonResistance = 0;
        double allElementalResistance = 0;

        double movementSpeed = 0;

        for (AffixRoll roll : itemData.implicitAffixes()) {
            for (AffixComponentRoll comp : roll.componentRolls()) {
                if (comp.scope() != AffixScope.GLOBAL) continue;
                double val = comp.value();
                switch (comp.stat()) {
                    case "physical_damage" -> {
                        if (isFlat(comp)) flatPhysicalDamage += val;
                        else if (isPercent(comp)) physicalDamagePercent += (val / 100.0);
                    }
                    case "fire_damage" -> {
                        if (isFlat(comp)) fireDamage += val;
                        else if (isPercent(comp)) elementalDamagePercent += (val / 100.0);
                    }
                    case "cold_damage" -> {
                        if (isFlat(comp)) coldDamage += val;
                        else if (isPercent(comp)) elementalDamagePercent += (val / 100.0);
                    }
                    case "lightning_damage" -> {
                        if (isFlat(comp)) lightningDamage += val;
                        else if (isPercent(comp)) elementalDamagePercent += (val / 100.0);
                    }
                    case "poison_damage" -> {
                        if (isFlat(comp)) poisonDamage += val;
                    }
                    case "elemental_damage" -> {
                        if (isPercent(comp)) elementalDamagePercent += (val / 100.0);
                    }
                    case "attack_speed" -> {
                        if (isPercent(comp)) attackSpeedPercent += (val / 100.0);
                    }
                    case "critical_chance" -> {
                        if (isFlat(comp) || isPercent(comp)) criticalStrikeChance += (val / 100.0); // usually flat % additions
                    }
                    case "critical_damage" -> {
                        if (isPercent(comp) || isFlat(comp)) criticalStrikeMultiplier += (val / 100.0);
                    }
                    case "armor", "armor_percent", "armor_flat" -> {
                        if (isFlat(comp)) armor += val;
                    }
                    case "maximum_life" -> {
                        if (isFlat(comp)) maximumLife += val;
                    }
                    case "fire_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) fireResistance += (val / 100.0);
                    }
                    case "cold_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) coldResistance += (val / 100.0);
                    }
                    case "lightning_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) lightningResistance += (val / 100.0);
                    }
                    case "poison_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) poisonResistance += (val / 100.0);
                    }
                    case "all_elemental_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) allElementalResistance += (val / 100.0);
                    }
                    case "movement_speed" -> {
                        if (isPercent(comp)) movementSpeed += (val / 100.0);
                        else if (isFlat(comp)) movementSpeed += (val / 100.0); // assuming stored as 20 for 20%
                    }
                }
            }
        }
        for (AffixRoll roll : itemData.prefixes()) {
            for (AffixComponentRoll comp : roll.componentRolls()) {
                if (comp.scope() != AffixScope.GLOBAL) continue;
                double val = comp.value();
                switch (comp.stat()) {
                    case "physical_damage" -> {
                        if (isFlat(comp)) flatPhysicalDamage += val;
                        else if (isPercent(comp)) physicalDamagePercent += (val / 100.0);
                    }
                    case "fire_damage" -> {
                        if (isFlat(comp)) fireDamage += val;
                        else if (isPercent(comp)) elementalDamagePercent += (val / 100.0);
                    }
                    case "cold_damage" -> {
                        if (isFlat(comp)) coldDamage += val;
                        else if (isPercent(comp)) elementalDamagePercent += (val / 100.0);
                    }
                    case "lightning_damage" -> {
                        if (isFlat(comp)) lightningDamage += val;
                        else if (isPercent(comp)) elementalDamagePercent += (val / 100.0);
                    }
                    case "poison_damage" -> {
                        if (isFlat(comp)) poisonDamage += val;
                    }
                    case "elemental_damage" -> {
                        if (isPercent(comp)) elementalDamagePercent += (val / 100.0);
                    }
                    case "attack_speed" -> {
                        if (isPercent(comp)) attackSpeedPercent += (val / 100.0);
                    }
                    case "critical_chance" -> {
                        if (isFlat(comp) || isPercent(comp)) criticalStrikeChance += (val / 100.0);
                    }
                    case "critical_damage" -> {
                        if (isPercent(comp) || isFlat(comp)) criticalStrikeMultiplier += (val / 100.0);
                    }
                    case "armor", "armor_percent", "armor_flat" -> {
                        if (isFlat(comp)) armor += val;
                    }
                    case "maximum_life" -> {
                        if (isFlat(comp)) maximumLife += val;
                    }
                    case "fire_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) fireResistance += (val / 100.0);
                    }
                    case "cold_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) coldResistance += (val / 100.0);
                    }
                    case "lightning_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) lightningResistance += (val / 100.0);
                    }
                    case "poison_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) poisonResistance += (val / 100.0);
                    }
                    case "all_elemental_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) allElementalResistance += (val / 100.0);
                    }
                    case "movement_speed" -> {
                        if (isPercent(comp)) movementSpeed += (val / 100.0);
                        else if (isFlat(comp)) movementSpeed += (val / 100.0);
                    }
                }
            }
        }
        for (AffixRoll roll : itemData.suffixes()) {
            for (AffixComponentRoll comp : roll.componentRolls()) {
                if (comp.scope() != AffixScope.GLOBAL) continue;
                double val = comp.value();
                switch (comp.stat()) {
                    case "physical_damage" -> {
                        if (isFlat(comp)) flatPhysicalDamage += val;
                        else if (isPercent(comp)) physicalDamagePercent += (val / 100.0);
                    }
                    case "fire_damage" -> {
                        if (isFlat(comp)) fireDamage += val;
                        else if (isPercent(comp)) elementalDamagePercent += (val / 100.0);
                    }
                    case "cold_damage" -> {
                        if (isFlat(comp)) coldDamage += val;
                        else if (isPercent(comp)) elementalDamagePercent += (val / 100.0);
                    }
                    case "lightning_damage" -> {
                        if (isFlat(comp)) lightningDamage += val;
                        else if (isPercent(comp)) elementalDamagePercent += (val / 100.0);
                    }
                    case "poison_damage" -> {
                        if (isFlat(comp)) poisonDamage += val;
                    }
                    case "elemental_damage" -> {
                        if (isPercent(comp)) elementalDamagePercent += (val / 100.0);
                    }
                    case "attack_speed" -> {
                        if (isPercent(comp)) attackSpeedPercent += (val / 100.0);
                    }
                    case "critical_chance" -> {
                        if (isFlat(comp) || isPercent(comp)) criticalStrikeChance += (val / 100.0);
                    }
                    case "critical_damage" -> {
                        if (isPercent(comp) || isFlat(comp)) criticalStrikeMultiplier += (val / 100.0);
                    }
                    case "armor", "armor_percent", "armor_flat" -> {
                        if (isFlat(comp)) armor += val;
                    }
                    case "maximum_life" -> {
                        if (isFlat(comp)) maximumLife += val;
                    }
                    case "fire_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) fireResistance += (val / 100.0);
                    }
                    case "cold_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) coldResistance += (val / 100.0);
                    }
                    case "lightning_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) lightningResistance += (val / 100.0);
                    }
                    case "poison_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) poisonResistance += (val / 100.0);
                    }
                    case "all_elemental_resistance" -> {
                        if (isFlat(comp) || isPercent(comp)) allElementalResistance += (val / 100.0);
                    }
                    case "movement_speed" -> {
                        if (isPercent(comp)) movementSpeed += (val / 100.0);
                        else if (isFlat(comp)) movementSpeed += (val / 100.0);
                    }
                }
            }
        }

        return new CharacterCombatStats(
                flatPhysicalDamage, fireDamage, coldDamage, lightningDamage, poisonDamage,
                physicalDamagePercent, elementalDamagePercent,
                attackSpeedPercent, criticalStrikeChance, criticalStrikeMultiplier,
                eliteDamageBonus, bossDamageBonus,
                armor, maximumLife,
                fireResistance + allElementalResistance,
                coldResistance + allElementalResistance,
                lightningResistance + allElementalResistance,
                poisonResistance + allElementalResistance, // Usually poison isn't part of all elem, but keeping it simple or omitting it depending on ARPG rules. Usually it is.
                0, 0,
                movementSpeed, 0, 0
        );
    }

    private static boolean isFlat(AffixComponentRoll comp) {
        return comp.operation() == AffixOperation.ADD_FLAT || comp.operation() == AffixOperation.ADDITIVE;
    }

    private static boolean isPercent(AffixComponentRoll comp) {
        return comp.operation() == AffixOperation.ADDITIVE_PERCENT || comp.operation() == AffixOperation.MULTIPLICATIVE_PERCENT;
    }
}
