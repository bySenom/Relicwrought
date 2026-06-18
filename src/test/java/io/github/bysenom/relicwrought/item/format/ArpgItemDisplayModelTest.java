package io.github.bysenom.relicwrought.item.format;

import io.github.bysenom.relicwrought.item.format.ArpgItemDisplayModel.DisplayLine;
import io.github.bysenom.relicwrought.item.model.*;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadResult;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;
import io.github.bysenom.relicwrought.item.registry.InMemoryDataRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class ArpgItemDisplayModelTest {
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

    private static ArpgItemData makeData(DefinitionKey base, int level, Rarity rarity, long seed,
                                          List<AffixRoll> prefixes, List<AffixRoll> suffixes) {
        return new ArpgItemData(
                1, UUID.randomUUID(), base, new ItemLevel(level), 0, rarity, 0, seed, false,
                List.of(), prefixes, suffixes
        );
    }

    private static AffixRoll makeAffixRoll(String id, AffixTier tier) {
        List<AffixComponentRoll> comps = List.of(
                new AffixComponentRoll("test_stat", AffixScope.LOCAL, AffixOperation.ADDITIVE, 0.5, 5.0)
        );
        return new AffixRoll(DefinitionKey.parse(id, "relicwrought"), tier, 0.5, 5.0, comps, 1);
    }

    @Test
    void commonItemHasNoAffixLines() {
        DefinitionKey key = DefinitionKey.parse("test_sword", "relicwrought");
        ArpgItemData data = makeData(key, 50, Rarity.COMMON, 1234L, List.of(), List.of());
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        assertTrue(model.hasArpgData());
        assertTrue(model.prefixLines().isEmpty());
        assertTrue(model.suffixLines().isEmpty());
        assertTrue(model.implicitLines().isEmpty());
    }

    @Test
    void commonItemHasLevelAndNoQuality() {
        DefinitionKey key = DefinitionKey.parse("test_sword", "relicwrought");
        ArpgItemData data = makeData(key, 50, Rarity.COMMON, 1234L, List.of(), List.of());
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        assertEquals(1, model.baseStatLines().size());
        assertTrue(model.baseStatLines().get(0).value().contains("50"));
    }

    @Test
    void magicItemHasAffixLines() {
        DefinitionKey key = DefinitionKey.parse("test_sword", "relicwrought");
        ArpgItemData data = makeData(key, 50, Rarity.MAGIC, 1234L,
                List.of(makeAffixRoll("test_affix", AffixTier.T10)),
                List.of()
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        assertEquals(1, model.prefixLines().size());
        assertTrue(model.prefixLines().get(0).value().contains("T10"));
    }

    @Test
    void magicItemAffixHasValue() {
        DefinitionKey key = DefinitionKey.parse("test_sword", "relicwrought");
        ArpgItemData data = makeData(key, 50, Rarity.MAGIC, 1234L,
                List.of(makeAffixRoll("test_affix", AffixTier.T10)),
                List.of()
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        assertTrue(model.prefixLines().get(0).value().contains("5"));
    }

    @Test
    void rareItemHasMultipleAffixes() {
        DefinitionKey key = DefinitionKey.parse("test_sword", "relicwrought");
        ArpgItemData data = makeData(key, 50, Rarity.RARE, 1234L,
                List.of(
                        makeAffixRoll("prefix_1", AffixTier.T5),
                        makeAffixRoll("prefix_2", AffixTier.T5)
                ),
                List.of(makeAffixRoll("suffix_1", AffixTier.T5))
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        assertEquals(2, model.prefixLines().size());
        assertEquals(1, model.suffixLines().size());
    }

    @Test
    void rareItemPrefixAndSuffixSeparated() {
        DefinitionKey key = DefinitionKey.parse("test_sword", "relicwrought");
        ArpgItemData data = makeData(key, 50, Rarity.RARE, 1234L,
                List.of(makeAffixRoll("prefix_1", AffixTier.T5)),
                List.of(makeAffixRoll("suffix_1", AffixTier.T5))
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        assertEquals(1, model.prefixLines().size());
        assertEquals(1, model.suffixLines().size());
    }

    @Test
    void notArpgItemReturnsHasArpgDataFalse() {
        ArpgItemReadResult result = ArpgItemReadResult.notArpgItem();
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);
        assertFalse(model.hasArpgData());
    }

    @Test
    void invalidDataReturnsCorrupted() {
        ArpgItemReadResult result = ArpgItemReadResult.invalid(List.of("test error"));
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        assertTrue(model.hasArpgData());
        assertEquals("tooltip.relicwrought.corrupted", model.displayName().baseTranslationKey());
    }

    @Test
    void missingDefinitionReturnsStatus() {
        DefinitionKey key = DefinitionKey.parse("missing_base", "relicwrought");
        ArpgItemData data = makeData(key, 1, Rarity.COMMON, 1234L, List.of(), List.of());
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        assertTrue(model.hasArpgData());
        assertEquals("tooltip.relicwrought.missing_definition", model.displayName().baseTranslationKey());
    }

    @Test
    void migratedItemShowsStatus() {
        DefinitionKey key = DefinitionKey.parse("test_sword", "relicwrought");
        ArpgItemData data = makeData(key, 1, Rarity.COMMON, 1234L, List.of(), List.of());
        ArpgItemReadResult result = ArpgItemReadResult.migrated(data, List.of("migrated from v0"));
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        assertTrue(model.tooltipStatus().contains("migrated"));
    }

    @Test
    void technicalLinesContainExpectedFields() {
        DefinitionKey key = DefinitionKey.parse("test_sword", "relicwrought");
        ArpgItemData data = makeData(key, 1, Rarity.COMMON, 1234L, List.of(), List.of());
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        assertTrue(model.technicalLines().stream().anyMatch(l -> l.label().equals("UUID")));
        assertTrue(model.technicalLines().stream().anyMatch(l -> l.label().equals("item_base")));
        assertTrue(model.technicalLines().stream().anyMatch(l -> l.label().equals("data_version")));
        assertTrue(model.technicalLines().stream().anyMatch(l -> l.label().equals("seed")));
        assertTrue(model.technicalLines().stream().anyMatch(l -> l.label().equals("status")));
    }

    @Test
    void noExceptionOnProblematicData() {
        assertDoesNotThrow(() -> {
            ArpgItemReadResult result = ArpgItemReadResult.invalid(List.of("bad data"));
            ArpgItemDisplayModel.fromReadResult(result, registry);
        });
        assertDoesNotThrow(() -> {
            ArpgItemDisplayModel.fromReadResult(ArpgItemReadResult.notArpgItem(), registry);
        });
        assertDoesNotThrow(() -> {
            DefinitionKey key = DefinitionKey.parse("missing_base", "relicwrought");
            ArpgItemData data = makeData(key, 1, Rarity.COMMON, 1234L, List.of(), List.of());
            ArpgItemDisplayModel.fromReadResult(ArpgItemReadResult.valid(data), registry);
        });
    }

    @Test
    void unsupportedVersionHasCorrectStatus() {
        ArpgItemReadResult result = ArpgItemReadResult.unsupportedVersion(99);
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        assertTrue(model.tooltipStatus().contains("unsupported_version"));
        assertEquals("tooltip.relicwrought.corrupted", model.displayName().baseTranslationKey());
    }

    @Test
    void affixLineHasTranslationKey() {
        List<AffixComponentRoll> comps = List.of(
                new AffixComponentRoll("test_stat", AffixScope.LOCAL, AffixOperation.ADDITIVE, 0.5, 5.0)
        );
        AffixRoll roll = new AffixRoll(DefinitionKey.parse("test_affix", "relicwrought"), AffixTier.T5, 0.5, 5.0, comps, 1);
        ArpgItemData data = makeData(DefinitionKey.parse("test_sword", "relicwrought"), 50, Rarity.MAGIC, 1234L,
                List.of(roll), List.of());
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(ArpgItemReadResult.valid(data), registry);

        assertEquals(1, model.prefixLines().size());
        assertNotNull(model.prefixLines().get(0).translationKey());
        assertEquals("stat.relicwrought.test_stat", model.prefixLines().get(0).translationKey());
    }

    @Test
    void qualityLineHasNoPercentSign() {
        DefinitionKey key = DefinitionKey.parse("test_sword", "relicwrought");
        ArpgItemData data = new ArpgItemData(
                1, UUID.randomUUID(), key, new ItemLevel(50), 0, Rarity.COMMON, 7, 1234L, false,
                List.of(), List.of(), List.of()
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        String qualityValue = model.baseStatLines().stream()
                .filter(l -> l.label().equals("quality"))
                .findFirst()
                .map(DisplayLine::value)
                .orElse("");
        assertEquals("7", qualityValue);
    }

    @Test
    void qualityLineNeverContainsDoublePercent() {
        DefinitionKey key = DefinitionKey.parse("test_sword", "relicwrought");
        ArpgItemData data = new ArpgItemData(
                1, UUID.randomUUID(), key, new ItemLevel(50), 0, Rarity.COMMON, 14, 1234L, false,
                List.of(), List.of(), List.of()
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(result, registry);

        for (DisplayLine line : model.baseStatLines()) {
            assertFalse(line.value().contains("%%"), "Base stat value must not contain double percent: " + line.value());
        }
        for (DisplayLine line : model.prefixLines()) {
            assertFalse(line.value().contains("%%"), "Prefix value must not contain double percent: " + line.value());
        }
        for (DisplayLine line : model.suffixLines()) {
            assertFalse(line.value().contains("%%"), "Suffix value must not contain double percent: " + line.value());
        }
    }

    @Test
    void addFlatFormattingShowsSignedValue() {
        List<AffixComponentRoll> comps = List.of(
                new AffixComponentRoll("test_stat", AffixScope.LOCAL, AffixOperation.ADD_FLAT, 0.5, 125.0)
        );
        AffixRoll roll = new AffixRoll(DefinitionKey.parse("test_affix", "relicwrought"), AffixTier.T5, 0.5, 125.0, comps, 1);
        ArpgItemData data = makeData(DefinitionKey.parse("test_sword", "relicwrought"), 50, Rarity.MAGIC, 1234L,
                List.of(roll), List.of());
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(ArpgItemReadResult.valid(data), registry);

        assertTrue(model.prefixLines().get(0).value().contains("+125"));
    }

    @Test
    void additivePercentFormattingShowsPercent() {
        List<AffixComponentRoll> comps = List.of(
                new AffixComponentRoll("test_stat", AffixScope.LOCAL, AffixOperation.ADDITIVE_PERCENT, 0.5, 56.2)
        );
        AffixRoll roll = new AffixRoll(DefinitionKey.parse("test_affix", "relicwrought"), AffixTier.T5, 0.5, 56.2, comps, 1);
        ArpgItemData data = makeData(DefinitionKey.parse("test_sword", "relicwrought"), 50, Rarity.MAGIC, 1234L,
                List.of(roll), List.of());
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(ArpgItemReadResult.valid(data), registry);

        assertTrue(model.prefixLines().get(0).value().contains("+56.2 %"));
    }

    @Test
    void multiplicativePercentFormattingShowsPercent() {
        List<AffixComponentRoll> comps = List.of(
                new AffixComponentRoll("test_stat", AffixScope.LOCAL, AffixOperation.MULTIPLICATIVE_PERCENT, 0.5, 20.0)
        );
        AffixRoll roll = new AffixRoll(DefinitionKey.parse("test_affix", "relicwrought"), AffixTier.T5, 0.5, 20.0, comps, 1);
        ArpgItemData data = makeData(DefinitionKey.parse("test_sword", "relicwrought"), 50, Rarity.MAGIC, 1234L,
                List.of(roll), List.of());
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(ArpgItemReadResult.valid(data), registry);

        assertTrue(model.prefixLines().get(0).value().contains("+20.0 %"));
    }

    @Test
    void additiveOperationFormatsAsSigned() {
        List<AffixComponentRoll> comps = List.of(
                new AffixComponentRoll("test_stat", AffixScope.LOCAL, AffixOperation.ADDITIVE, 0.5, 5.0)
        );
        AffixRoll roll = new AffixRoll(DefinitionKey.parse("test_affix", "relicwrought"), AffixTier.T5, 0.5, 5.0, comps, 1);
        ArpgItemData data = makeData(DefinitionKey.parse("test_sword", "relicwrought"), 50, Rarity.MAGIC, 1234L,
                List.of(roll), List.of());
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(ArpgItemReadResult.valid(data), registry);

        assertTrue(model.prefixLines().get(0).value().contains("+5"));
    }

    @Test
    void multiComponentAffixCreatesSeparateLines() {
        List<AffixComponentRoll> comps = List.of(
                new AffixComponentRoll("maximum_durability", AffixScope.LOCAL, AffixOperation.ADDITIVE_PERCENT, 0.5, 56.2),
                new AffixComponentRoll("armor", AffixScope.LOCAL, AffixOperation.ADD_FLAT, 0.5, 40.0)
        );
        AffixRoll roll = new AffixRoll(DefinitionKey.parse("test_affix", "relicwrought"), AffixTier.T5, 0.5, 96.2, comps, 1);
        ArpgItemData data = makeData(DefinitionKey.parse("test_sword", "relicwrought"), 50, Rarity.MAGIC, 1234L,
                List.of(roll), List.of());
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(ArpgItemReadResult.valid(data), registry);

        assertEquals(2, model.prefixLines().size());
        assertEquals("stat.relicwrought.maximum_durability", model.prefixLines().get(0).translationKey());
        assertEquals("stat.relicwrought.armor", model.prefixLines().get(1).translationKey());
    }
}
