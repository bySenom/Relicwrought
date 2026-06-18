package io.github.bysenom.relicwrought.item.format;

import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.Rarity;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadResult;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadStatus;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;

public final class ArpgItemDisplayNameService {
    private static final String FALLBACK_TRANSLATION_KEY = "item_base.relicwrought.unknown_item";

    private ArpgItemDisplayNameService() {
    }

    public static ArpgItemDisplayName resolve(ArpgItemReadResult readResult, DataRegistry<ItemBaseDefinition> itemBases) {
        if (readResult.status() == ArpgItemReadStatus.NOT_ARPG_ITEM) {
            return new ArpgItemDisplayName(FALLBACK_TRANSLATION_KEY, "", false, 0xFFFFFF, false, false);
        }

        ArpgItemData data = readResult.data().orElse(null);
        if (data == null) {
            return new ArpgItemDisplayName("tooltip.relicwrought.corrupted", "", false, 0xFF5555, false, true);
        }

        ItemBaseDefinition base = itemBases.get(data.itemBaseId()).orElse(null);
        String baseKey = base != null ? base.translationKey() : "tooltip.relicwrought.missing_definition";
        String rarityKey = resolveRarityTranslationKey(data.rarity());
        int color = resolveRarityColor(data.rarity());
        boolean hasBaseDef = base != null;
        boolean isCorrupted = readResult.status() != ArpgItemReadStatus.VALID
                && readResult.status() != ArpgItemReadStatus.MIGRATED;

        return new ArpgItemDisplayName(baseKey, rarityKey, hasBaseDef, color, data.starterItem(), isCorrupted);
    }

    private static String resolveRarityTranslationKey(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "rarity.relicwrought.common";
            case MAGIC -> "rarity.relicwrought.magic";
            case RARE -> "rarity.relicwrought.rare";
            case LEGENDARY -> "rarity.relicwrought.legendary";
            case UNIQUE -> "rarity.relicwrought.unique";
        };
    }

    private static int resolveRarityColor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0xFFFFFF;
            case MAGIC -> 0x5555FF;
            case RARE -> 0xFFFF55;
            case LEGENDARY -> 0xFFAA00;
            case UNIQUE -> 0xFF55FF;
        };
    }

    public record ArpgItemDisplayName(
            String baseTranslationKey,
            String rarityTranslationKey,
            boolean hasBaseDefinition,
            int rarityColor,
            boolean isStarter,
            boolean isCorrupted
    ) {
    }
}
