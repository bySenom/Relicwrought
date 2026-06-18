package io.github.bysenom.relicwrought.combat.stats;

import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class EquippedItemStatResolver {
    private final ArpgItemStackService stackService;

    public EquippedItemStatResolver(ArpgItemStackService stackService) {
        this.stackService = stackService;
    }

    public List<ArpgItemData> resolveEquippedItems(LivingEntity entity) {
        List<ArpgItemData> equipped = new ArrayList<>();
        
        EquipmentSlot[] slots = {
                EquipmentSlot.MAINHAND,
                EquipmentSlot.OFFHAND,
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        for (EquipmentSlot slot : slots) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stackService.hasArpgData(stack)) {
                stackService.read(stack).data().ifPresent(equipped::add);
            }
        }
        return equipped;
    }

    public CharacterCombatStats collectGlobalStats(LivingEntity entity) {
        List<ArpgItemData> equipped = resolveEquippedItems(entity);
        return combine(equipped);
    }

    public CharacterCombatStats combine(List<ArpgItemData> items) {
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

        double flatDamageReduction = 0;
        double percentDamageReduction = 0;

        double movementSpeed = 0;
        double lifeRegeneration = 0;
        double miningSpeedPercent = 0;

        for (ArpgItemData item : items) {
            CharacterCombatStats stats = GlobalAffixStatCollector.collect(item);
            flatPhysicalDamage += stats.flatPhysicalDamage();
            fireDamage += stats.fireDamage();
            coldDamage += stats.coldDamage();
            lightningDamage += stats.lightningDamage();
            poisonDamage += stats.poisonDamage();

            physicalDamagePercent += stats.physicalDamagePercent();
            elementalDamagePercent += stats.elementalDamagePercent();

            attackSpeedPercent += stats.attackSpeedPercent();
            criticalStrikeChance += stats.criticalStrikeChance();
            criticalStrikeMultiplier += stats.criticalStrikeMultiplier();

            eliteDamageBonus += stats.eliteDamageBonus();
            bossDamageBonus += stats.bossDamageBonus();

            armor += stats.armor();
            maximumLife += stats.maximumLife();
            fireResistance += stats.fireResistance();
            coldResistance += stats.coldResistance();
            lightningResistance += stats.lightningResistance();
            poisonResistance += stats.poisonResistance();

            flatDamageReduction += stats.flatDamageReduction();
            percentDamageReduction += stats.percentDamageReduction(); // Note: combining % reduction purely additively isn't perfectly accurate long-term, but ok for now

            movementSpeed += stats.movementSpeed();
            lifeRegeneration += stats.lifeRegeneration();
            miningSpeedPercent += stats.miningSpeedPercent();
        }

        return new CharacterCombatStats(
                flatPhysicalDamage, fireDamage, coldDamage, lightningDamage, poisonDamage,
                physicalDamagePercent, elementalDamagePercent,
                attackSpeedPercent, criticalStrikeChance, criticalStrikeMultiplier,
                eliteDamageBonus, bossDamageBonus,
                armor, maximumLife, fireResistance, coldResistance, lightningResistance, poisonResistance,
                flatDamageReduction, percentDamageReduction,
                movementSpeed, lifeRegeneration, miningSpeedPercent
        );
    }
}
