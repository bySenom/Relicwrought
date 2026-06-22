package io.github.bysenom.relicwrought;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ArpgModConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void missingPhaseEightHudFieldsAreMigratedToDevelopmentDefaults() throws Exception {
        Path configPath = tempDir.resolve("relicwrought.json");
        Files.writeString(configPath, """
                {
                  "enableArpgMobDrops": false,
                  "normalMobDropChanceMultiplier": 1.0,
                  "requirePlayerKill": true,
                  "lootingDropChanceMultiplier": 0.1,
                  "keepVanillaEquipmentDrops": true,
                  "disableVanillaEquipmentRecipes": false,
                  "disabledRecipeCategories": [],
                  "enableDebugLootLogging": false,
                  "maximumArpgDropsPerSource": 10
                }
                """);

        ArpgModConfig config = ArpgModConfig.load(tempDir, LoggerFactory.getLogger("test"));

        assertTrue(config.enableRelicwroughtHud());
        assertTrue(config.hideVanillaHearts());
        assertTrue(config.hideVanillaArmor());
        assertTrue(config.showHealthNumbers());
        assertTrue(config.showResourceNumbers());
        assertTrue(config.enableEnemyNameplates());
        assertTrue(config.showEnemyHealthBars());
        assertTrue(config.showEnemyHealthNumbers());
        assertTrue(config.enableCombatText());
        assertTrue(config.showFloatingDamageNumbers());
        assertTrue(config.showOwnDamageNumbers());
        assertTrue(config.enableUiDebugOverlay());
        assertTrue(config.enableRpgInventory());
        assertFalse(config.replaceVanillaInventoryScreen());
        assertFalse(config.disablePlayerInventoryCrafting());
        assertTrue(config.showEquipmentSlotLabels());
        assertFalse(config.allowNonArpgItemsInEquipment());
        assertTrue(config.dropExtraEquipmentOnDeath());
        assertTrue(config.debugEquipmentSync());

        String migrated = Files.readString(configPath);
        assertTrue(migrated.contains("enableUiDebugOverlay"));
        assertTrue(migrated.contains("showFloatingDamageNumbers"));
        assertTrue(migrated.contains("enableRpgInventory"));
        assertTrue(migrated.contains("replaceVanillaInventoryScreen"));
    }

    @Test
    void explicitHudFalseIsRespected() throws Exception {
        Path configPath = tempDir.resolve("relicwrought.json");
        Files.writeString(configPath, """
                {
                  "enableRelicwroughtHud": false,
                  "hideVanillaHearts": false,
                  "hideVanillaArmor": false,
                  "showFloatingDamageNumbers": false
                }
                """);

        ArpgModConfig config = ArpgModConfig.load(tempDir, LoggerFactory.getLogger("test"));

        assertFalse(config.enableRelicwroughtHud());
        assertFalse(config.hideVanillaHearts());
        assertFalse(config.hideVanillaArmor());
        assertFalse(config.showFloatingDamageNumbers());
        assertTrue(config.enableUiDebugOverlay());
    }
}
