package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.io.ArpgDataBootstrap;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.registry.DefinitionLoadResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ItemStatScalerTest {
    private static DefinitionLoadResult definitions;
    private static ItemStatScaler scaler;

    @BeforeAll
    static void loadDefinitions() {
        definitions = ArpgDataBootstrap.loadBundledDefinitions(Relicwrought.MOD_ID, LoggerFactory.getLogger("arpgmod-test"));
        assertTrue(definitions.errors().isEmpty(), () -> String.join("\n", definitions.errors()));
        scaler = new ItemStatScaler(definitions.scalingProfiles());
    }

    @Test
    void weaponScalingProducesValidDamageRangeAndTargetArea() {
        ItemBaseDefinition axe = itemBase("iron_war_axe");

        WeaponBaseStats stats = scaler.scaleWeaponBaseStats(axe, ScalingContext.of(ItemLevel.of(950), ItemQuality.of(0)));

        assertTrue(stats.minimumDamage() <= stats.maximumDamage());
        assertTrue(stats.minimumDamage() >= 250000.0D);
        assertTrue(stats.maximumDamage() >= 360000.0D);
        assertTrue(stats.maximumDurability() > 0L);
    }

    @Test
    void weaponQualityImprovesDamageButNotDurability() {
        ItemBaseDefinition sword = itemBase("starter_training_sword");

        WeaponBaseStats normal = scaler.scaleWeaponBaseStats(sword, ScalingContext.of(ItemLevel.of(500), ItemQuality.of(0)));
        WeaponBaseStats quality = scaler.scaleWeaponBaseStats(sword, ScalingContext.of(ItemLevel.of(500), ItemQuality.of(20)));

        assertTrue(quality.minimumDamage() > normal.minimumDamage());
        assertTrue(quality.maximumDamage() > normal.maximumDamage());
        assertEquals(normal.maximumDurability(), quality.maximumDurability());
    }

    @Test
    void differentWeaponMultipliersProduceDifferentResults() {
        WeaponBaseStats sword = scaler.scaleWeaponBaseStats(itemBase("starter_training_sword"), ScalingContext.of(ItemLevel.of(300), ItemQuality.of(0)));
        WeaponBaseStats axe = scaler.scaleWeaponBaseStats(itemBase("iron_war_axe"), ScalingContext.of(ItemLevel.of(300), ItemQuality.of(0)));

        assertTrue(axe.maximumDamage() > sword.maximumDamage());
    }

    @Test
    void armorSlotMultipliersAndQualityWork() {
        ArmorBaseStats helmet = scaler.scaleArmorBaseStats(itemBase("scout_helmet"), ScalingContext.of(ItemLevel.of(500), ItemQuality.of(0)));
        ArmorBaseStats chest = scaler.scaleArmorBaseStats(itemBase("guard_chestplate"), ScalingContext.of(ItemLevel.of(500), ItemQuality.of(0)));
        ArmorBaseStats qualityChest = scaler.scaleArmorBaseStats(itemBase("guard_chestplate"), ScalingContext.of(ItemLevel.of(500), ItemQuality.of(20)));

        assertTrue(chest.armor() > helmet.armor());
        assertTrue(qualityChest.armor() > chest.armor());
        assertEquals(chest.maximumDurability(), qualityChest.maximumDurability());
    }

    @Test
    void toolScalingUsesMiningSpeedQualityAndDiscreteMiningTier() {
        ItemBaseDefinition pickaxe = itemBase("starter_pickaxe");

        ToolBaseStats early = scaler.scaleToolBaseStats(pickaxe, ScalingContext.of(ItemLevel.of(99), ItemQuality.of(0)));
        ToolBaseStats mid = scaler.scaleToolBaseStats(pickaxe, ScalingContext.of(ItemLevel.of(250), ItemQuality.of(0)));
        ToolBaseStats endgame = scaler.scaleToolBaseStats(pickaxe, ScalingContext.of(ItemLevel.of(950), ItemQuality.of(20)));
        ToolBaseStats noQuality = scaler.scaleToolBaseStats(pickaxe, ScalingContext.of(ItemLevel.of(950), ItemQuality.of(0)));

        assertEquals(1, early.miningTier());
        assertEquals(3, mid.miningTier());
        assertEquals(7, endgame.miningTier());
        assertTrue(endgame.miningSpeed() > noQuality.miningSpeed());
        assertEquals(noQuality.maximumDurability(), endgame.maximumDurability());
        assertTrue(endgame.miningSpeed() < NumberSafety.MAX_SCALED_VALUE);
    }

    @Test
    void identicalInputsProduceIdenticalResults() {
        ScalingContext context = ScalingContext.of(ItemLevel.of(614), ItemQuality.of(12));
        WeaponBaseStats first = scaler.scaleWeaponBaseStats(itemBase("iron_war_axe"), context);
        WeaponBaseStats second = scaler.scaleWeaponBaseStats(itemBase("iron_war_axe"), context);

        assertEquals(first, second);
    }

    private static ItemBaseDefinition itemBase(String path) {
        return definitions.itemBases().get(DefinitionKey.parse(path, Relicwrought.MOD_ID)).orElseThrow();
    }
}
