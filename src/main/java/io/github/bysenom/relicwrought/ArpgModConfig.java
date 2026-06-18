package io.github.bysenom.relicwrought;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public record ArpgModConfig(
        boolean enableArpgMobDrops,
        double normalMobDropChanceMultiplier,
        boolean requirePlayerKill,
        double lootingDropChanceMultiplier,
        boolean keepVanillaEquipmentDrops,
        boolean disableVanillaEquipmentRecipes,
        Set<String> disabledRecipeCategories,
        boolean enableDebugLootLogging,
        int maximumArpgDropsPerSource,
        boolean enableClassSelection,
        boolean showClassScreenOnFirstJoin,
        boolean allowCommandClassSelection,
        boolean grantStarterKit,
        boolean autoEquipStarterArmor,
        boolean dropStarterItemsWhenInventoryFull,
        boolean allowAdminClassReset,
        boolean disableVanillaEquipmentRecipesAfterSelection,
        boolean enableCharacterProgression,
        int maximumCharacterLevel,
        boolean requirePlayerKillForXp,
        double xpMultiplier,
        boolean showXpGainMessages,
        boolean showLevelUpMessages,
        int attributePointsPerLevel,
        boolean allowAdminLevelCommands
) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "relicwrought.json";

    public static ArpgModConfig defaults() {
        return new ArpgModConfig(
                false,
                1.0,
                true,
                0.10,
                true,
                false,
                new HashSet<>(),
                false,
                10,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                100,
                true,
                1.0,
                true,
                true,
                1,
                true
        );
    }

    public static ArpgModConfig load(Path configDir, Logger logger) {
        Path configPath = configDir.resolve(CONFIG_FILE_NAME);
        if (Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath);
                ArpgModConfig config = GSON.fromJson(content, ArpgModConfig.class);
                if (config == null) {
                    logger.warn("Invalid config file, using defaults");
                    return save(configPath, defaults(), logger);
                }
                return validate(config, configPath, logger);
            } catch (Exception e) {
                logger.warn("Failed to load config: {}. Using defaults.", e.getMessage());
                return save(configPath, defaults(), logger);
            }
        }
        return save(configPath, defaults(), logger);
    }

    private static ArpgModConfig validate(ArpgModConfig config, Path configPath, Logger logger) {
        boolean modified = false;

        double chance = config.normalMobDropChanceMultiplier();
        if (chance < 0.0) { chance = 0.0; modified = true; }
        if (chance > 1.0) { chance = 1.0; modified = true; }

        double looting = config.lootingDropChanceMultiplier();
        if (looting < 0.0) { looting = 0.0; modified = true; }

        int maxDrops = config.maximumArpgDropsPerSource();
        if (maxDrops < 0) { maxDrops = 0; modified = true; }
        if (maxDrops > 10) { maxDrops = 10; modified = true; }

        Set<String> categories = config.disabledRecipeCategories();
        if (categories == null) { categories = new HashSet<>(); modified = true; }

        int maxLevel = config.maximumCharacterLevel();
        if (maxLevel < 1) { maxLevel = 100; modified = true; }
        if (maxLevel > 100) { maxLevel = 100; modified = true; }

        double xpMult = config.xpMultiplier();
        if (xpMult < 0.0) { xpMult = 0.0; modified = true; }

        int attrPoints = config.attributePointsPerLevel();
        if (attrPoints < 0) { attrPoints = 0; modified = true; }

        if (modified) {
            ArpgModConfig validated = new ArpgModConfig(
                    config.enableArpgMobDrops(), chance, config.requirePlayerKill(),
                    looting, config.keepVanillaEquipmentDrops(),
                    config.disableVanillaEquipmentRecipes(), categories,
                    config.enableDebugLootLogging(), maxDrops,
                    config.enableClassSelection(), config.showClassScreenOnFirstJoin(),
                    config.allowCommandClassSelection(), config.grantStarterKit(),
                    config.autoEquipStarterArmor(), config.dropStarterItemsWhenInventoryFull(),
                    config.allowAdminClassReset(), config.disableVanillaEquipmentRecipesAfterSelection(),
                    config.enableCharacterProgression(), maxLevel,
                    config.requirePlayerKillForXp(), xpMult,
                    config.showXpGainMessages(), config.showLevelUpMessages(),
                    attrPoints, config.allowAdminLevelCommands()
            );
            return save(configPath, validated, logger);
        }
        return config;
    }

    private static ArpgModConfig save(Path configPath, ArpgModConfig config, Logger logger) {
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(config));
        } catch (IOException e) {
            logger.warn("Failed to save config: {}", e.getMessage());
        }
        return config;
    }
}
