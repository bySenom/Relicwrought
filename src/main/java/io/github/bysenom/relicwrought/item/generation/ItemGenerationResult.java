package io.github.bysenom.relicwrought.item.generation;

import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ItemGenerationResult(
        boolean success,
        ArpgItemData itemData,
        ItemStack itemStack,
        DefinitionKey selectedBaseId,
        ItemLevel itemLevel,
        int quality,
        DefinitionKey rarityId,
        int prefixCount,
        int suffixCount,
        long seed,
        List<String> messages,
        GenerationErrorCode errorCode
) {
    public ItemGenerationResult {
        messages = messages == null ? List.of() : List.copyOf(messages);
    }

    public static ItemGenerationResult failure(GenerationErrorCode errorCode, List<String> messages) {
        return new ItemGenerationResult(false, null, null, null, null, 0, null, 0, 0, 0, messages, errorCode);
    }

    public static ItemGenerationResult failure(GenerationErrorCode errorCode, String message) {
        return failure(errorCode, List.of(message));
    }
}
