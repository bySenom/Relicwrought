package io.github.bysenom.relicwrought.loot;

import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.item.io.DefinitionJsonReader;
import io.github.bysenom.relicwrought.item.io.JsonReaderSupport;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemCategory;

import java.util.HashMap;
import java.util.Map;

public final class LootProfileDefinitionJsonReader implements DefinitionJsonReader<LootProfileDefinition> {

    @Override
    public LootProfileDefinition read(JsonObject json, String defaultNamespace) {
        DefinitionKey id = DefinitionKey.parse(
                JsonReaderSupport.requiredString(json, "id"), defaultNamespace);

        LootSourceType sourceType = LootSourceType.valueOf(
                JsonReaderSupport.requiredString(json, "source_type").toUpperCase());

        double dropChance = JsonReaderSupport.optionalDouble(json, "drop_chance", 0.0);

        JsonObject dropCount = JsonReaderSupport.optionalObject(json, "drop_count");
        int dropMin = 1;
        int dropMax = 1;
        if (dropCount != null) {
            dropMin = JsonReaderSupport.optionalInt(dropCount, "minimum", 1);
            dropMax = JsonReaderSupport.optionalInt(dropCount, "maximum", 1);
        }

        var allowedCategories = JsonReaderSupport.enumSet(json, "allowed_categories", ItemCategory::valueOf);
        var requiredTags = JsonReaderSupport.stringSet(json, "required_item_tags");
        var excludedTags = JsonReaderSupport.stringSet(json, "excluded_item_tags");

        Map<DefinitionKey, Integer> rarityWeights = new HashMap<>();
        var rarityJson = JsonReaderSupport.optionalObject(json, "rarity_weights");
        if (rarityJson != null) {
            for (var entry : rarityJson.entrySet()) {
                rarityWeights.put(
                        DefinitionKey.parse(entry.getKey(), defaultNamespace),
                        entry.getValue().getAsInt()
                );
            }
        }

        LootItemLevelConfig itemLevel = readLevelConfig(json);

        var dimensions = JsonReaderSupport.stringSet(json, "dimensions");

        boolean requirePlayerKill = JsonReaderSupport.optionalBoolean(json, "require_player_kill", true);

        Map<String, EntityLootOverride> entityOverrides = new HashMap<>();
        var overridesJson = JsonReaderSupport.optionalObject(json, "entity_overrides");
        if (overridesJson != null) {
            for (var entry : overridesJson.entrySet()) {
                entityOverrides.put(entry.getKey(), readOverride(entry.getValue().getAsJsonObject(), defaultNamespace));
            }
        }

        int dataVersion = JsonReaderSupport.optionalInt(json, "data_version", 1);

        return new LootProfileDefinition(
                id, sourceType, dropChance, dropMin, dropMax,
                allowedCategories, requiredTags, excludedTags,
                rarityWeights, itemLevel, dimensions, entityOverrides,
                requirePlayerKill, dataVersion
        );
    }

    private static LootItemLevelConfig readLevelConfig(JsonObject json) {
        var levelJson = JsonReaderSupport.optionalObject(json, "item_level");
        if (levelJson == null) {
            return new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5);
        }
        String typeStr = JsonReaderSupport.optionalString(levelJson, "type", "source_scaled");
        LootItemLevelConfig.LootItemLevelType type;
        try {
            type = LootItemLevelConfig.LootItemLevelType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED;
        }
        int minimum = JsonReaderSupport.optionalInt(levelJson, "minimum", 1);
        int maximum = JsonReaderSupport.optionalInt(levelJson, "maximum", 500);
        int variance = JsonReaderSupport.optionalInt(levelJson, "random_variance", 5);
        return new LootItemLevelConfig(type, minimum, maximum, variance);
    }

    private static EntityLootOverride readOverride(JsonObject json, String defaultNamespace) {
        DefinitionKey profileId = null;
        if (json.has("profile")) {
            profileId = DefinitionKey.parse(json.get("profile").getAsString(), defaultNamespace);
        }
        int levelBonus = JsonReaderSupport.optionalInt(json, "item_level_bonus", 0);
        double chanceBonus = JsonReaderSupport.optionalDouble(json, "drop_chance_bonus", 0.0);
        int extraDrops = JsonReaderSupport.optionalInt(json, "additional_drop_count", 0);
        return new EntityLootOverride(profileId, levelBonus, chanceBonus, extraDrops);
    }
}
