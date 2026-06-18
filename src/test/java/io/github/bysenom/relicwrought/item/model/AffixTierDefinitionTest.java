package io.github.bysenom.relicwrought.item.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AffixTierDefinitionTest {
    @Test
    void usesNormalizedRollToCalculateActualValue() {
        AffixTierDefinition tier = new AffixTierDefinition(AffixTier.T3, 700, 45.0D, 55.0D);

        assertEquals(45.0D, tier.valueForRoll(0.0D));
        assertEquals(52.2D, tier.valueForRoll(0.72D), 0.0001D);
        assertEquals(55.0D, tier.valueForRoll(1.0D));
    }

    @Test
    void rejectsInvalidNormalizedRolls() {
        AffixTierDefinition tier = new AffixTierDefinition(AffixTier.T10, 1, 1.0D, 2.0D);

        assertThrows(IllegalArgumentException.class, () -> tier.valueForRoll(-0.01D));
        assertThrows(IllegalArgumentException.class, () -> tier.valueForRoll(1.01D));
    }

    @Test
    void unlocksAtConfiguredItemLevel() {
        AffixTierDefinition tier = new AffixTierDefinition(AffixTier.T2, 800, 10.0D, 20.0D);

        assertFalse(tier.isUnlockedAt(new ItemLevel(799)));
        assertTrue(tier.isUnlockedAt(new ItemLevel(800)));
    }
}
