package io.github.bysenom.relicwrought.item.format;

import io.github.bysenom.relicwrought.item.format.ArpgItemDisplayNameService.ArpgItemDisplayName;
import io.github.bysenom.relicwrought.item.model.*;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadResult;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;
import io.github.bysenom.relicwrought.item.registry.InMemoryDataRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class ArpgItemDisplayNameServiceTest {
    private final DataRegistry<ItemBaseDefinition> registry = createTestRegistry();

    private static DataRegistry<ItemBaseDefinition> createTestRegistry() {
        InMemoryDataRegistry<ItemBaseDefinition> reg = new InMemoryDataRegistry<>();
        reg.register(new ItemBaseDefinition(
                DefinitionKey.parse("test_sword", "relicwrought"),
                "item_base.relicwrought.test_sword",
                "minecraft:iron_sword",
                ItemCategory.SWORD,
                Set.of(ArpgEquipmentSlot.MAIN_HAND),
                new BaseStatBlock(5, 10, 1.6, 0, 250, 0, 0),
                List.of(),
                Set.of(),
                DefinitionKey.parse("weapon_damage_profile", "relicwrought"),
                null,
                Set.of(),
                1
        ));
        return reg;
    }

    @Test
    void commonItemShowsBaseTranslationKey() {
        ArpgItemData data = ArpgItemData.emptyGenerated(
                DefinitionKey.parse("test_sword", "relicwrought"),
                new ItemLevel(1),
                Rarity.COMMON,
                1234L
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayName name = ArpgItemDisplayNameService.resolve(result, registry);
        assertEquals("item_base.relicwrought.test_sword", name.baseTranslationKey());
        assertEquals("rarity.relicwrought.common", name.rarityTranslationKey());
    }

    @Test
    void magicItemShowsMagicRarity() {
        ArpgItemData data = ArpgItemData.emptyGenerated(
                DefinitionKey.parse("test_sword", "relicwrought"),
                new ItemLevel(1),
                Rarity.MAGIC,
                5678L
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayName name = ArpgItemDisplayNameService.resolve(result, registry);
        assertEquals("rarity.relicwrought.magic", name.rarityTranslationKey());
    }

    @Test
    void rareItemShowsRareRarity() {
        ArpgItemData data = ArpgItemData.emptyGenerated(
                DefinitionKey.parse("test_sword", "relicwrought"),
                new ItemLevel(1),
                Rarity.RARE,
                9012L
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayName name = ArpgItemDisplayNameService.resolve(result, registry);
        assertEquals("rarity.relicwrought.rare", name.rarityTranslationKey());
    }

    @Test
    void rarityColorIsCorrectForCommon() {
        ArpgItemData data = ArpgItemData.emptyGenerated(
                DefinitionKey.parse("test_sword", "relicwrought"),
                new ItemLevel(1),
                Rarity.COMMON,
                1234L
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayName name = ArpgItemDisplayNameService.resolve(result, registry);
        assertEquals(0xFFFFFF, name.rarityColor());
    }

    @Test
    void rarityColorIsCorrectForMagic() {
        ArpgItemData data = ArpgItemData.emptyGenerated(
                DefinitionKey.parse("test_sword", "relicwrought"),
                new ItemLevel(1),
                Rarity.MAGIC,
                1234L
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayName name = ArpgItemDisplayNameService.resolve(result, registry);
        assertEquals(0x5555FF, name.rarityColor());
    }

    @Test
    void rarityColorIsCorrectForRare() {
        ArpgItemData data = ArpgItemData.emptyGenerated(
                DefinitionKey.parse("test_sword", "relicwrought"),
                new ItemLevel(1),
                Rarity.RARE,
                1234L
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayName name = ArpgItemDisplayNameService.resolve(result, registry);
        assertEquals(0xFFFF55, name.rarityColor());
    }

    @Test
    void missingItemBaseUsesMissingDefinitionKey() {
        ArpgItemData data = ArpgItemData.emptyGenerated(
                DefinitionKey.parse("unknown_base", "relicwrought"),
                new ItemLevel(1),
                Rarity.COMMON,
                1234L
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayName name = ArpgItemDisplayNameService.resolve(result, registry);
        assertEquals("tooltip.relicwrought.missing_definition", name.baseTranslationKey());
        assertFalse(name.hasBaseDefinition());
    }

    @Test
    void corruptedDataShowsCorruptedKey() {
        ArpgItemReadResult result = ArpgItemReadResult.invalid(List.of("corrupted data"));
        ArpgItemDisplayName name = ArpgItemDisplayNameService.resolve(result, registry);
        assertEquals("tooltip.relicwrought.corrupted", name.baseTranslationKey());
    }

    @Test
    void normalDisplayUsesTranslationKeyNotRawId() {
        ArpgItemData data = ArpgItemData.emptyGenerated(
                DefinitionKey.parse("test_sword", "relicwrought"),
                new ItemLevel(1),
                Rarity.COMMON,
                1234L
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayName name = ArpgItemDisplayNameService.resolve(result, registry);
        assertEquals("item_base.relicwrought.test_sword", name.baseTranslationKey());
        assertTrue(name.hasBaseDefinition());
    }

    @Test
    void notArpgItemReturnsFallback() {
        ArpgItemReadResult result = ArpgItemReadResult.notArpgItem();
        ArpgItemDisplayName name = ArpgItemDisplayNameService.resolve(result, registry);
        assertEquals("item_base.relicwrought.unknown_item", name.baseTranslationKey());
        assertFalse(name.hasBaseDefinition());
    }
}
