package io.github.bysenom.relicwrought.loot;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class LootProfileDefinitionTest {
    private static final DefinitionKey ID = DefinitionKey.parse("test_profile", Relicwrought.MOD_ID);

    @Test
    void acceptsValidProfile() {
        assertDoesNotThrow(() -> new LootProfileDefinition(
                ID, LootSourceType.NORMAL_MOB, 0.08, 1, 1,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5),
                Set.of("minecraft:overworld"), Map.of(), true, 1
        ));
    }

    @Test
    void rejectsNegativeDropChance() {
        assertThrows(IllegalArgumentException.class, () -> new LootProfileDefinition(
                ID, LootSourceType.NORMAL_MOB, -0.1, 1, 1,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5),
                Set.of(), Map.of(), true, 1
        ));
    }

    @Test
    void rejectsDropChanceAboveOne() {
        assertThrows(IllegalArgumentException.class, () -> new LootProfileDefinition(
                ID, LootSourceType.NORMAL_MOB, 1.5, 1, 1,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5),
                Set.of(), Map.of(), true, 1
        ));
    }

    @Test
    void acceptsDropChanceBoundaries() {
        assertDoesNotThrow(() -> new LootProfileDefinition(
                ID, LootSourceType.NORMAL_MOB, 0.0, 1, 1,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5),
                Set.of(), Map.of(), true, 1
        ));
        assertDoesNotThrow(() -> new LootProfileDefinition(
                ID, LootSourceType.NORMAL_MOB, 1.0, 1, 1,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5),
                Set.of(), Map.of(), true, 1
        ));
    }

    @Test
    void rejectsDropCountMaxBeyondTen() {
        assertThrows(IllegalArgumentException.class, () -> new LootProfileDefinition(
                ID, LootSourceType.NORMAL_MOB, 0.5, 1, 11,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5),
                Set.of(), Map.of(), true, 1
        ));
    }

    @Test
    void rejectsDropCountMaxBelowMin() {
        assertThrows(IllegalArgumentException.class, () -> new LootProfileDefinition(
                ID, LootSourceType.NORMAL_MOB, 0.5, 5, 3,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5),
                Set.of(), Map.of(), true, 1
        ));
    }

    @Test
    void rejectsNegativeDropCountMin() {
        assertThrows(IllegalArgumentException.class, () -> new LootProfileDefinition(
                ID, LootSourceType.NORMAL_MOB, 0.5, -1, 1,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5),
                Set.of(), Map.of(), true, 1
        ));
    }

    @Test
    void rejectsInvalidDataVersion() {
        assertThrows(IllegalArgumentException.class, () -> new LootProfileDefinition(
                ID, LootSourceType.NORMAL_MOB, 0.5, 1, 1,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5),
                Set.of(), Map.of(), true, -1
        ));
    }

    @Test
    void nullifyEmptySetsAndMaps() {
        var def = new LootProfileDefinition(
                ID, LootSourceType.NORMAL_MOB, 0.5, 1, 1,
                null, null, null, null,
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5),
                null, null, true, 1
        );
        assertEquals(Set.of(), def.allowedCategories());
        assertEquals(Set.of(), def.requiredItemTags());
        assertEquals(Set.of(), def.excludedItemTags());
        assertEquals(Map.of(), def.rarityWeights());
        assertEquals(Set.of(), def.dimensions());
        assertEquals(Map.of(), def.entityOverrides());
    }
}
