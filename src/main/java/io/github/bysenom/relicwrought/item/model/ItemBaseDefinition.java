package io.github.bysenom.relicwrought.item.model;

import java.util.List;
import java.util.Set;

public record ItemBaseDefinition(
        DefinitionKey id,
        String translationKey,
        String minecraftItemId,
        ItemCategory category,
        Set<ArpgEquipmentSlot> validSlots,
        BaseStatBlock baseStats,
        List<DefinitionKey> implicitAffixes,
        Set<String> affixTags,
        DefinitionKey scalingProfile,
        ItemBaseScaling scaling,
        Set<DefinitionKey> lootSources,
        int dataVersion
) implements KeyedDefinition {
    public ItemBaseDefinition {
        if (translationKey == null || translationKey.isBlank()) {
            throw new IllegalArgumentException("Item base translation key must not be blank");
        }
        if (minecraftItemId == null || minecraftItemId.isBlank()) {
            throw new IllegalArgumentException("Minecraft item id must not be blank");
        }
        validSlots = Set.copyOf(validSlots);
        if (validSlots.isEmpty()) {
            throw new IllegalArgumentException("Item base must define at least one valid equipment slot");
        }
        implicitAffixes = List.copyOf(implicitAffixes);
        affixTags = Set.copyOf(affixTags);
        if (scaling == null) {
            scaling = ItemBaseScaling.defaults(scalingProfile);
        }
        lootSources = Set.copyOf(lootSources);
        if (dataVersion <= 0) {
            throw new IllegalArgumentException("Item base data version must be positive");
        }
    }
}
