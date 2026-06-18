package io.github.bysenom.relicwrought.item.model;

import io.github.bysenom.relicwrought.item.ItemDataVersions;

import java.util.List;
import java.util.UUID;

public record ArpgItemData(
        int dataVersion,
        UUID itemId,
        DefinitionKey itemBaseId,
        ItemLevel itemLevel,
        int requiredCharacterLevel,
        Rarity rarity,
        int quality,
        long seed,
        boolean starterItem,
        List<AffixRoll> implicitAffixes,
        List<AffixRoll> prefixes,
        List<AffixRoll> suffixes
) {
    public static final int MIN_QUALITY = 0;
    public static final int MAX_QUALITY = 20;

    public ArpgItemData {
        if (dataVersion <= 0) {
            throw new IllegalArgumentException("Item data version must be positive");
        }
        if (requiredCharacterLevel < 0) {
            throw new IllegalArgumentException("Required character level must not be negative");
        }
        if (quality < MIN_QUALITY || quality > MAX_QUALITY) {
            throw new IllegalArgumentException("Quality must be between " + MIN_QUALITY + " and " + MAX_QUALITY + ": " + quality);
        }
        implicitAffixes = List.copyOf(implicitAffixes);
        prefixes = List.copyOf(prefixes);
        suffixes = List.copyOf(suffixes);
        if (prefixes.size() > 3) {
            throw new IllegalArgumentException("Items cannot have more than three prefixes");
        }
        if (suffixes.size() > 3) {
            throw new IllegalArgumentException("Items cannot have more than three suffixes");
        }
    }

    public static ArpgItemData emptyGenerated(DefinitionKey itemBaseId, ItemLevel itemLevel, Rarity rarity, long seed) {
        return new ArpgItemData(
                ItemDataVersions.CURRENT,
                new UUID(seed, seed ^ 0x5DEECE66DL),
                itemBaseId,
                itemLevel,
                0,
                rarity,
                0,
                seed,
                false,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
