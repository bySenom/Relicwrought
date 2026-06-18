package io.github.bysenom.relicwrought.loot;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record LootDropResult(
        DefinitionKey profileId,
        LootSourceType sourceType,
        double dropChance,
        boolean didDrop,
        int requestedCount,
        int successfulCount,
        int failedCount,
        List<Integer> itemLevels,
        long seed,
        List<ItemStack> generatedItems,
        LootErrorCode errorCode,
        List<String> warnings
) {
    public LootDropResult {
        itemLevels = itemLevels == null ? List.of() : List.copyOf(itemLevels);
        generatedItems = generatedItems == null ? List.of() : List.copyOf(generatedItems);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public static LootDropResult noDrop(DefinitionKey profileId, LootSourceType sourceType, double dropChance, long seed) {
        return new LootDropResult(profileId, sourceType, dropChance, false, 0, 0, 0, List.of(), seed, List.of(), LootErrorCode.NONE, List.of());
    }

    public static LootDropResult failure(DefinitionKey profileId, LootSourceType sourceType, long seed, LootErrorCode errorCode, List<String> warnings) {
        return new LootDropResult(profileId, sourceType, 0.0, false, 0, 0, 0, List.of(), seed, List.of(), errorCode, warnings);
    }
}
