package io.github.bysenom.relicwrought.item.generation;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RarityDefinitionTest {
    static final DefinitionKey COMMON = DefinitionKey.parse("common", "arpgmod");
    static final DefinitionKey MAGIC = DefinitionKey.parse("magic", "arpgmod");
    static final DefinitionKey RARE = DefinitionKey.parse("rare", "arpgmod");

    @Test
    void commonHasNoAffixCounts() {
        RarityDefinition common = new RarityDefinition(
                COMMON, "rarity.arpgmod.common", 650, 1, 0,
                List.of(), 0, "#FFFFFF", 1
        );
        assertEquals(0, common.allowedAffixCounts().size());
        assertTrue(common.isGeneratable());
    }

    @Test
    void magicHasCorrectCounts() {
        RarityDefinition magic = new RarityDefinition(
                MAGIC, "rarity.arpgmod.magic", 300, 1, 0,
                List.of(
                        new RarityDefinition.AllowedAffixCount(1, 0, 100),
                        new RarityDefinition.AllowedAffixCount(0, 1, 100),
                        new RarityDefinition.AllowedAffixCount(1, 1, 60)
                ), 1, "#5555FF", 1
        );
        assertEquals(3, magic.allowedAffixCounts().size());
        assertTrue(magic.isGeneratable());
        assertTrue(magic.isUnlockedAt(5));
    }

    @Test
    void rareHasLevelLock() {
        RarityDefinition rare = new RarityDefinition(
                RARE, "rarity.arpgmod.rare", 50, 10, 0,
                List.of(
                        new RarityDefinition.AllowedAffixCount(2, 1, 100)
                ), 2, "#FFFF55", 1
        );
        assertFalse(rare.isUnlockedAt(5));
        assertTrue(rare.isUnlockedAt(10));
        assertTrue(rare.isUnlockedAt(50));
    }

    @Test
    void weightZeroIsNotGeneratable() {
        RarityDefinition legendary = new RarityDefinition(
                DefinitionKey.parse("legendary", "arpgmod"),
                "rarity.arpgmod.legendary", 0, 50, 0,
                List.of(), 3, "#FFAA00", 1
        );
        assertFalse(legendary.isGeneratable());
    }

    @Test
    void rejectsNegativeWeight() {
        assertThrows(IllegalArgumentException.class, () ->
                new RarityDefinition(COMMON, "t", -1, 1, 0, List.of(), 0, "#FFF", 1)
        );
    }

    @Test
    void rejectsInvalidPrefixCount() {
        assertThrows(IllegalArgumentException.class, () ->
                new RarityDefinition.AllowedAffixCount(4, 0, 100)
        );
    }

    @Test
    void rejectsNonPositiveWeight() {
        assertThrows(IllegalArgumentException.class, () ->
                new RarityDefinition.AllowedAffixCount(1, 0, 0)
        );
    }

    @Test
    void allowedAffixCountMustNotBeNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                new RarityDefinition.AllowedAffixCount(-1, 0, 100)
        );
    }
}
