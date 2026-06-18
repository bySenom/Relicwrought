package io.github.bysenom.relicwrought.item.generation;

import io.github.bysenom.relicwrought.item.affix.AffixSlotLimits;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

class SlotDistributionSelectorTest {
    final SlotDistributionSelector selector = new SlotDistributionSelector();
    static final DefinitionKey COMMON = DefinitionKey.parse("common", "arpgmod");
    static final DefinitionKey MAGIC = DefinitionKey.parse("magic", "arpgmod");

    @Test
    void commonReturnsZeroZero() {
        RarityDefinition common = new RarityDefinition(
                COMMON, "c", 650, 1, 0, List.of(), 0, "#FFF", 1
        );
        AffixSlotLimits limits = selector.select(common, new SplittableRandom(42));
        assertEquals(0, limits.prefixes());
        assertEquals(0, limits.suffixes());
    }

    @Test
    void magicNeverExceedsOnePerSlot() {
        RarityDefinition magic = new RarityDefinition(
                MAGIC, "m", 300, 1, 0,
                List.of(
                        new RarityDefinition.AllowedAffixCount(1, 0, 100),
                        new RarityDefinition.AllowedAffixCount(0, 1, 100),
                        new RarityDefinition.AllowedAffixCount(1, 1, 60)
                ), 1, "#55F", 1
        );
        for (int seed = 0; seed < 500; seed++) {
            AffixSlotLimits limits = selector.select(magic, new SplittableRandom(seed));
            assertTrue(limits.prefixes() <= 1);
            assertTrue(limits.suffixes() <= 1);
            assertTrue(limits.prefixes() + limits.suffixes() >= 1);
            assertTrue(limits.prefixes() + limits.suffixes() <= 2);
        }
    }

    @Test
    void magicCanReachBothSlots() {
        RarityDefinition magic = new RarityDefinition(
                MAGIC, "m", 300, 1, 0,
                List.of(
                        new RarityDefinition.AllowedAffixCount(1, 0, 100),
                        new RarityDefinition.AllowedAffixCount(0, 1, 100),
                        new RarityDefinition.AllowedAffixCount(1, 1, 60)
                ), 1, "#55F", 1
        );
        boolean foundBoth = false;
        for (int seed = 0; seed < 1000; seed++) {
            AffixSlotLimits limits = selector.select(magic, new SplittableRandom(seed));
            if (limits.prefixes() == 1 && limits.suffixes() == 1) {
                foundBoth = true;
                break;
            }
        }
        assertTrue(foundBoth, "Both slots should be reachable");
    }

    @Test
    void deterministic() {
        RarityDefinition magic = new RarityDefinition(
                MAGIC, "m", 300, 1, 0,
                List.of(
                        new RarityDefinition.AllowedAffixCount(1, 0, 100),
                        new RarityDefinition.AllowedAffixCount(0, 1, 100)
                ), 1, "#55F", 1
        );
        AffixSlotLimits r1 = selector.select(magic, new SplittableRandom(77));
        AffixSlotLimits r2 = selector.select(magic, new SplittableRandom(77));
        assertEquals(r1.prefixes(), r2.prefixes());
        assertEquals(r1.suffixes(), r2.suffixes());
    }
}
