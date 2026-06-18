package io.github.bysenom.relicwrought.loot;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemCategory;
import io.github.bysenom.relicwrought.item.model.KeyedDefinition;

import java.util.Map;
import java.util.Set;

public record LootProfileDefinition(
        DefinitionKey id,
        LootSourceType sourceType,
        double dropChance,
        int dropCountMinimum,
        int dropCountMaximum,
        Set<ItemCategory> allowedCategories,
        Set<String> requiredItemTags,
        Set<String> excludedItemTags,
        Map<DefinitionKey, Integer> rarityWeights,
        LootItemLevelConfig itemLevel,
        Set<String> dimensions,
        Map<String, EntityLootOverride> entityOverrides,
        boolean requirePlayerKill,
        int dataVersion
) implements KeyedDefinition {
    public LootProfileDefinition {
        if (dropChance < 0.0 || dropChance > 1.0) {
            throw new IllegalArgumentException("Drop chance must be between 0.0 and 1.0: " + dropChance);
        }
        if (dropCountMinimum < 0) {
            throw new IllegalArgumentException("Drop count minimum must be non-negative: " + dropCountMinimum);
        }
        if (dropCountMaximum < dropCountMinimum) {
            throw new IllegalArgumentException("Drop count maximum must be >= minimum: " + dropCountMaximum);
        }
        if (dropCountMaximum > 10) {
            throw new IllegalArgumentException("Drop count maximum must not exceed 10: " + dropCountMaximum);
        }
        allowedCategories = allowedCategories == null ? Set.of() : Set.copyOf(allowedCategories);
        requiredItemTags = requiredItemTags == null ? Set.of() : Set.copyOf(requiredItemTags);
        excludedItemTags = excludedItemTags == null ? Set.of() : Set.copyOf(excludedItemTags);
        rarityWeights = rarityWeights == null ? Map.of() : Map.copyOf(rarityWeights);
        dimensions = dimensions == null ? Set.of() : Set.copyOf(dimensions);
        entityOverrides = entityOverrides == null ? Map.of() : Map.copyOf(entityOverrides);
        if (dataVersion <= 0) {
            throw new IllegalArgumentException("Loot profile data version must be positive");
        }
    }
}
