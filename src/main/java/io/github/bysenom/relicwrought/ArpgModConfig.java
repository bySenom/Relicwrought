package io.github.bysenom.relicwrought;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        boolean allowAdminLevelCommands,
        boolean enableArpgCombat,
        boolean enableArpgPvpDamage,
        double baseCriticalChance,
        double baseCriticalMultiplier,
        double maximumCriticalChance,
        double maximumPhysicalReduction,
        double maximumElementalResistance,
        double minimumElementalResistance,
        double armorConstant,
        double vanillaArmorConversion,
        double strengthPhysicalDamagePerPoint,
        double strengthArmorPerPoint,
        double dexterityAttackSpeedPerPoint,
        double dexterityCritChancePerPoint,
        double intelligenceElementalDamagePerPoint,
        double intelligenceResistancePerPoint,
        double vitalityLifePerPoint,
        boolean useVanillaAttackCooldown,
        boolean enableCombatDebugLogging,
        
        // Cooldown Server Config
        boolean enableWeaponCooldownGating,
        double minimumAttackSpeed,
        double maximumAttackSpeed,
        int minimumWeaponCooldownTicks,
        boolean resetCooldownOnWeaponSwitch,
        
        // Cooldown Client Config
        boolean showWeaponCooldown,
        boolean hideVanillaAttackIndicatorForArpgWeapons,
        String weaponCooldownPosition,
        int weaponCooldownOffsetX,
        int weaponCooldownOffsetY,
        int weaponCooldownWidth,
        int weaponCooldownHeight,
        boolean weaponCooldownShowReadyFlash,
        boolean weaponCooldownShowPercentage,
        
        // Phase 8.5 UI Config
        boolean enableRelicwroughtHud,
        boolean hideVanillaHearts,
        boolean hideVanillaArmor,
        boolean showHealthNumbers,
        boolean showResourceNumbers,
        boolean enableCombatText,
        boolean showFloatingDamageNumbers,
        boolean showOwnDamageNumbers,
        boolean enableEnemyNameplates,
        boolean showEnemyHealthBars,
        boolean showEnemyHealthNumbers,
        boolean enableUiDebugOverlay,
        
        // Phase 8.6A RPG equipment window config
        boolean enableRpgInventory,
        boolean replaceVanillaInventoryScreen,
        boolean disablePlayerInventoryCrafting,
        boolean showEquipmentSlotLabels,
        boolean allowNonArpgItemsInEquipment,
        boolean dropExtraEquipmentOnDeath,
        boolean debugEquipmentSync
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
                true,
                true,   // enableArpgCombat
                false,  // enableArpgPvpDamage
                0.05,   // baseCriticalChance (5%)
                1.5,    // baseCriticalMultiplier (150%)
                1.0,    // maximumCriticalChance (100%)
                0.85,   // maximumPhysicalReduction (85%)
                0.75,   // maximumElementalResistance (75%)
                -1.0,   // minimumElementalResistance (-100%)
                100.0,  // armorConstant
                100.0,  // vanillaArmorConversion (1 armor = 100 ARPG armor)
                0.002,  // strengthPhysicalDamagePerPoint (+1% per 5 STR = 0.002 per point)
                1.0,    // strengthArmorPerPoint (+1 per STR)
                0.001,  // dexterityAttackSpeedPerPoint (+0.1% per DEX = 0.001 per point)
                0.0005, // dexterityCritChancePerPoint (+0.05% per DEX = 0.0005 per point)
                0.002,  // intelligenceElementalDamagePerPoint (+1% per 5 INT = 0.002 per point)
                0.002,  // intelligenceResistancePerPoint (+0.2% per INT = 0.002 per point)
                5.0,    // vitalityLifePerPoint (+5 life per VIT)
                true,   // useVanillaAttackCooldown
                false,  // enableCombatDebugLogging
                
                true,   // enableWeaponCooldownGating
                0.2,    // minimumAttackSpeed
                10.0,   // maximumAttackSpeed
                2,      // minimumWeaponCooldownTicks
                true,   // resetCooldownOnWeaponSwitch
                
                true,   // showWeaponCooldown
                true,   // hideVanillaAttackIndicatorForArpgWeapons
                "HOTBAR_CENTER", // weaponCooldownPosition
                0,      // weaponCooldownOffsetX
                -20,    // weaponCooldownOffsetY
                100,    // weaponCooldownWidth
                4,      // weaponCooldownHeight
                true,   // weaponCooldownShowReadyFlash
                false,  // weaponCooldownShowPercentage
                
                true,   // enableRelicwroughtHud
                true,   // hideVanillaHearts
                true,   // hideVanillaArmor
                true,   // showHealthNumbers
                true,   // showResourceNumbers
                true,   // enableCombatText
                true,   // showFloatingDamageNumbers
                true,   // showOwnDamageNumbers
                true,   // enableEnemyNameplates
                true,   // showEnemyHealthBars
                true,   // showEnemyHealthNumbers
                true,   // enableUiDebugOverlay
                
                true,   // enableRpgInventory
                false,  // replaceVanillaInventoryScreen (8.6A keeps vanilla inventory intact)
                false,  // disablePlayerInventoryCrafting (8.6A does not enforce crafting blocking)
                true,   // showEquipmentSlotLabels
                false,  // allowNonArpgItemsInEquipment
                true,   // dropExtraEquipmentOnDeath
                true    // debugEquipmentSync
        );
    }

    public static ArpgModConfig load(Path configDir, Logger logger) {
        Path configPath = configDir.resolve(CONFIG_FILE_NAME);
        if (Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath);
                JsonObject root = JsonParser.parseString(content).getAsJsonObject();
                boolean migratedMissingFields = applyMissingDefaults(root, defaults(), configPath, logger);
                ArpgModConfig config = GSON.fromJson(root, ArpgModConfig.class);
                if (config == null) {
                    logger.warn("Invalid config file, using defaults");
                    return save(configPath, defaults(), logger);
                }
                return validate(config, configPath, logger, migratedMissingFields);
            } catch (Exception e) {
                logger.warn("Failed to load config: {}. Using defaults.", e.getMessage());
                return save(configPath, defaults(), logger);
            }
        }
        return save(configPath, defaults(), logger);
    }

    private static ArpgModConfig validate(ArpgModConfig config, Path configPath, Logger logger, boolean modified) {

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

        double maxPhysRed = config.maximumPhysicalReduction();
        if (maxPhysRed < 0.0) { maxPhysRed = 0.0; modified = true; }
        if (maxPhysRed > 1.0) { maxPhysRed = 1.0; modified = true; }

        double maxResist = config.maximumElementalResistance();
        if (maxResist > 1.0) { maxResist = 1.0; modified = true; }

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
                    attrPoints, config.allowAdminLevelCommands(),
                    config.enableArpgCombat(), config.enableArpgPvpDamage(),
                    config.baseCriticalChance(), config.baseCriticalMultiplier(),
                    config.maximumCriticalChance(), maxPhysRed,
                    maxResist, config.minimumElementalResistance(),
                    config.armorConstant(), config.vanillaArmorConversion(),
                    config.strengthPhysicalDamagePerPoint(), config.strengthArmorPerPoint(),
                    config.dexterityAttackSpeedPerPoint(), config.dexterityCritChancePerPoint(),
                    config.intelligenceElementalDamagePerPoint(), config.intelligenceResistancePerPoint(),
                    config.vitalityLifePerPoint(), config.useVanillaAttackCooldown(),
                    config.enableCombatDebugLogging(),
                    config.enableWeaponCooldownGating(),
                    config.minimumAttackSpeed(), config.maximumAttackSpeed(),
                    config.minimumWeaponCooldownTicks(), config.resetCooldownOnWeaponSwitch(),
                    config.showWeaponCooldown(), config.hideVanillaAttackIndicatorForArpgWeapons(),
                    config.weaponCooldownPosition(), config.weaponCooldownOffsetX(),
                    config.weaponCooldownOffsetY(), config.weaponCooldownWidth(),
                    config.weaponCooldownHeight(), config.weaponCooldownShowReadyFlash(),
                    config.weaponCooldownShowPercentage(),
                    config.enableRelicwroughtHud(), config.hideVanillaHearts(),
                    config.hideVanillaArmor(), config.showHealthNumbers(),
                    config.showResourceNumbers(), config.enableCombatText(),
                    config.showFloatingDamageNumbers(), config.showOwnDamageNumbers(),
                    config.enableEnemyNameplates(), config.showEnemyHealthBars(),
                    config.showEnemyHealthNumbers(), config.enableUiDebugOverlay(),
                    config.enableRpgInventory(), config.replaceVanillaInventoryScreen(),
                    config.disablePlayerInventoryCrafting(), config.showEquipmentSlotLabels(),
                    config.allowNonArpgItemsInEquipment(), config.dropExtraEquipmentOnDeath(),
                    config.debugEquipmentSync()
            );
            return save(configPath, validated, logger);
        }
        return config;
    }

    private static boolean applyMissingDefaults(JsonObject root, ArpgModConfig defaults, Path configPath, Logger logger) {
        List<String> missing = new ArrayList<>();
        for (RecordComponent component : ArpgModConfig.class.getRecordComponents()) {
            String name = component.getName();
            if (!root.has(name) || root.get(name).isJsonNull()) {
                try {
                    Object value = component.getAccessor().invoke(defaults);
                    root.add(name, GSON.toJsonTree(value));
                    missing.add(name);
                } catch (ReflectiveOperationException e) {
                    logger.warn("Failed to apply default config value for {}: {}", name, e.getMessage());
                }
            }
        }
        if (!missing.isEmpty()) {
            logger.warn("Config {} was missing {} field(s); migrated defaults for: {}",
                    configPath, missing.size(), String.join(", ", missing));
            return true;
        }
        return false;
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
