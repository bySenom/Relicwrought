package io.github.bysenom.relicwrought.loot;

import com.google.gson.JsonParser;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LootProfileDefinitionJsonReaderTest {
    private static final String NS = Relicwrought.MOD_ID;
    private final LootProfileDefinitionJsonReader reader = new LootProfileDefinitionJsonReader();

    @Test
    void readsFullProfile() {
        var profile = reader.read(JsonParser.parseString("""
                {
                  "id": "test_profile",
                  "source_type": "normal_mob",
                  "drop_chance": 0.08,
                  "drop_count": {
                    "minimum": 1,
                    "maximum": 2
                  },
                  "allowed_categories": ["sword", "helmet"],
                  "required_item_tags": ["tag_a"],
                  "excluded_item_tags": ["tag_b"],
                  "rarity_weights": {
                    "common": 650,
                    "magic": 300
                  },
                  "item_level": {
                    "type": "source_scaled",
                    "minimum": 1,
                    "maximum": 500,
                    "random_variance": 5
                  },
                  "dimensions": ["minecraft:overworld"],
                  "entity_overrides": {
                    "minecraft:zombie": {
                      "profile": "zombie_override",
                      "item_level_bonus": 10,
                      "drop_chance_bonus": 0.05,
                      "additional_drop_count": 1
                    }
                  },
                  "require_player_kill": true,
                  "data_version": 1
                }
                """).getAsJsonObject(), NS);

        assertEquals(DefinitionKey.parse("test_profile", NS), profile.id());
        assertEquals(LootSourceType.NORMAL_MOB, profile.sourceType());
        assertEquals(0.08, profile.dropChance());
        assertEquals(1, profile.dropCountMinimum());
        assertEquals(2, profile.dropCountMaximum());
        assertEquals(2, profile.allowedCategories().size());
        assertTrue(profile.requiredItemTags().contains("tag_a"));
        assertTrue(profile.excludedItemTags().contains("tag_b"));
        assertEquals(2, profile.rarityWeights().size());
        assertEquals(650, profile.rarityWeights().get(DefinitionKey.parse("common", NS)));
        assertEquals(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, profile.itemLevel().type());
        assertEquals(1, profile.dimensions().size());
        assertTrue(profile.dimensions().contains("minecraft:overworld"));
        assertTrue(profile.requirePlayerKill());
        assertEquals(1, profile.entityOverrides().size());
        assertNotNull(profile.entityOverrides().get("minecraft:zombie"));
        assertEquals(10, profile.entityOverrides().get("minecraft:zombie").itemLevelBonus());
        assertEquals(0.05, profile.entityOverrides().get("minecraft:zombie").dropChanceBonus());
        assertEquals(1, profile.entityOverrides().get("minecraft:zombie").additionalDropCount());
        assertEquals(1, profile.dataVersion());
    }

    @Test
    void readsMinimalProfile() {
        var profile = reader.read(JsonParser.parseString("""
                {
                  "id": "minimal_profile",
                  "source_type": "boss",
                  "drop_chance": 1.0,
                  "require_player_kill": false,
                  "data_version": 1
                }
                """).getAsJsonObject(), NS);

        assertEquals(DefinitionKey.parse("minimal_profile", NS), profile.id());
        assertEquals(LootSourceType.BOSS, profile.sourceType());
        assertEquals(1.0, profile.dropChance());
        assertEquals(1, profile.dropCountMinimum());
        assertEquals(1, profile.dropCountMaximum());
        assertTrue(profile.allowedCategories().isEmpty());
        assertEquals(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, profile.itemLevel().type());
        assertEquals(1, profile.itemLevel().minimum());
        assertEquals(500, profile.itemLevel().maximum());
        assertFalse(profile.requirePlayerKill());
    }

    @Test
    void readsFixedItemLevelType() {
        var profile = reader.read(JsonParser.parseString("""
                {
                  "id": "fixed_level",
                  "source_type": "boss",
                  "drop_chance": 1.0,
                  "item_level": {
                    "type": "fixed",
                    "minimum": 100,
                    "maximum": 100,
                    "random_variance": 0
                  },
                  "require_player_kill": false,
                  "data_version": 1
                }
                """).getAsJsonObject(), NS);

        assertEquals(LootItemLevelConfig.LootItemLevelType.FIXED, profile.itemLevel().type());
        assertEquals(100, profile.itemLevel().minimum());
    }

    @Test
    void readsRandomItemLevelType() {
        var profile = reader.read(JsonParser.parseString("""
                {
                  "id": "random_level",
                  "source_type": "boss",
                  "drop_chance": 1.0,
                  "item_level": {
                    "type": "random",
                    "minimum": 50,
                    "maximum": 200,
                    "random_variance": 10
                  },
                  "require_player_kill": false,
                  "data_version": 1
                }
                """).getAsJsonObject(), NS);

        assertEquals(LootItemLevelConfig.LootItemLevelType.RANDOM, profile.itemLevel().type());
        assertEquals(50, profile.itemLevel().minimum());
        assertEquals(200, profile.itemLevel().maximum());
        assertEquals(10, profile.itemLevel().randomVariance());
    }

    @Test
    void throwsOnMissingSourceType() {
        assertThrows(IllegalArgumentException.class, () -> reader.read(JsonParser.parseString("""
                {
                  "id": "bad",
                  "drop_chance": 0.5,
                  "require_player_kill": true,
                  "data_version": 1
                }
                """).getAsJsonObject(), NS));
    }

    @Test
    void throwsOnInvalidSourceType() {
        assertThrows(IllegalArgumentException.class, () -> reader.read(JsonParser.parseString("""
                {
                  "id": "bad",
                  "source_type": "invalid_type",
                  "drop_chance": 0.5,
                  "require_player_kill": true,
                  "data_version": 1
                }
                """).getAsJsonObject(), NS));
    }
}
