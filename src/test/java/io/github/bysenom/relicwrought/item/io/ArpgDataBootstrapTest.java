package io.github.bysenom.relicwrought.item.io;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.model.AffixTier;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.loot.LootSourceType;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.registry.DefinitionLoadResult;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArpgDataBootstrapTest {
    @Test
    void loadsBundledItemBasesAndAffixes() {
        DefinitionLoadResult result = ArpgDataBootstrap.loadBundledDefinitions(
                Relicwrought.MOD_ID,
                LoggerFactory.getLogger("arpgmod-test")
        );

        assertTrue(result.errors().isEmpty(), () -> String.join("\n", result.errors()));
        assertEquals(11, result.itemBases().size());
        assertEquals(25, result.affixes().size());
        assertEquals(23, result.affixGroups().size());
        assertEquals(8, result.scalingProfiles().size());
        assertEquals(5, result.rarities().size());
        assertEquals(5, result.lootProfiles().size());
        assertTrue(result.itemBases().get(DefinitionKey.parse("starter_pickaxe", Relicwrought.MOD_ID)).isPresent());
        assertTrue(result.scalingProfiles().get(DefinitionKey.parse("weapon_damage_default", Relicwrought.MOD_ID)).isPresent());
        assertTrue(result.rarities().get(DefinitionKey.parse("common", Relicwrought.MOD_ID)).isPresent());
        assertTrue(result.rarities().get(DefinitionKey.parse("rare", Relicwrought.MOD_ID)).isPresent());
        assertEquals(4, result.classes().size());
        assertEquals(4, result.starterKits().size());
        assertTrue(result.classes().get(DefinitionKey.parse("warrior", Relicwrought.MOD_ID)).isPresent());
        assertTrue(result.starterKits().get(DefinitionKey.parse("warrior_starter", Relicwrought.MOD_ID)).isPresent());
    }

    @Test
    void loadedRaritiesHaveCorrectWeights() {
        DefinitionLoadResult result = ArpgDataBootstrap.loadBundledDefinitions(
                Relicwrought.MOD_ID,
                LoggerFactory.getLogger("arpgmod-test")
        );

        var common = result.rarities().get(DefinitionKey.parse("common", Relicwrought.MOD_ID)).orElseThrow();
        var magic = result.rarities().get(DefinitionKey.parse("magic", Relicwrought.MOD_ID)).orElseThrow();
        var rare = result.rarities().get(DefinitionKey.parse("rare", Relicwrought.MOD_ID)).orElseThrow();
        var legendary = result.rarities().get(DefinitionKey.parse("legendary", Relicwrought.MOD_ID)).orElseThrow();
        var unique = result.rarities().get(DefinitionKey.parse("unique", Relicwrought.MOD_ID)).orElseThrow();

        assertEquals(650, common.weight());
        assertTrue(common.allowedAffixCounts().isEmpty());

        assertEquals(300, magic.weight());
        assertEquals(3, magic.allowedAffixCounts().size());

        assertEquals(50, rare.weight());
        assertEquals(10, rare.minimumItemLevel());
        assertEquals(7, rare.allowedAffixCounts().size());

        assertEquals(0, legendary.weight());
        assertFalse(legendary.isGeneratable());

        assertEquals(0, unique.weight());
        assertFalse(unique.isGeneratable());
    }

    @Test
    void loadedLootProfilesHaveCorrectContent() {
        DefinitionLoadResult result = ArpgDataBootstrap.loadBundledDefinitions(
                Relicwrought.MOD_ID,
                LoggerFactory.getLogger("arpgmod-test")
        );
        var overworldProfile = result.lootProfiles()
                .get(DefinitionKey.parse("overworld_normal_mob", Relicwrought.MOD_ID))
                .orElseThrow();
        assertEquals(LootSourceType.NORMAL_MOB, overworldProfile.sourceType());
        assertEquals(0.08, overworldProfile.dropChance());
        assertTrue(overworldProfile.dimensions().contains("minecraft:overworld"));
    }

    @Test
    void loadedAffixKnowsBestUnlockedTier() {
        DefinitionLoadResult result = ArpgDataBootstrap.loadBundledDefinitions(
                Relicwrought.MOD_ID,
                LoggerFactory.getLogger("arpgmod-test")
        );

        var affix = result.affixes()
                .get(DefinitionKey.parse("local_physical_damage_percent", Relicwrought.MOD_ID))
                .orElseThrow();

        assertEquals(AffixTier.T10, affix.bestUnlockedTier(new ItemLevel(99)).orElseThrow().tier());
        assertEquals(AffixTier.T1, affix.bestUnlockedTier(new ItemLevel(900)).orElseThrow().tier());
        assertEquals("physical_damage", affix.components().getFirst().stat());
    }
}
