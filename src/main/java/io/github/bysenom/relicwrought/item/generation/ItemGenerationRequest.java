package io.github.bysenom.relicwrought.item.generation;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemCategory;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public record ItemGenerationRequest(
        DefinitionKey itemBaseId,
        ItemLevel itemLevel,
        long seed,
        DefinitionKey rarityId,
        Integer quality,
        Set<ItemCategory> allowedCategories,
        Set<String> requiredTags,
        Set<String> excludedTags,
        Set<DefinitionKey> allowedBaseIds,
        ItemStack targetStack,
        boolean persistentWrite,
        String sourceIdentifier
) {
    public ItemGenerationRequest(
            DefinitionKey itemBaseId,
            ItemLevel itemLevel,
            long seed
    ) {
        this(itemBaseId, itemLevel, seed, null, null, null, null, null, null, null, true, null);
    }

    public ItemGenerationRequest {
        if (itemLevel == null) {
            throw new IllegalArgumentException("Item level must not be null");
        }
        if (seed == 0) {
            throw new IllegalArgumentException("Seed must not be zero");
        }
    }
}
