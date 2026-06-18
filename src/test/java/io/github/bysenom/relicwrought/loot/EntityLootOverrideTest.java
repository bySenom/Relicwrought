package io.github.bysenom.relicwrought.loot;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class EntityLootOverrideTest {
    private static final DefinitionKey PROFILE = DefinitionKey.parse("boss_profile", Relicwrought.MOD_ID);

    @Test
    void clampsDropChanceBonusBelowNegativeOne() {
        var override = new EntityLootOverride(null, 0, -2.0, 0);
        assertEquals(-1.0, override.dropChanceBonus());
    }

    @Test
    void clampsDropChanceBonusAboveOne() {
        var override = new EntityLootOverride(null, 0, 2.0, 0);
        assertEquals(1.0, override.dropChanceBonus());
    }

    @Test
    void clampsNegativeAdditionalDropCountToZero() {
        var override = new EntityLootOverride(null, 0, 0.0, -5);
        assertEquals(0, override.additionalDropCount());
    }

    @Test
    void clampsLargeAdditionalDropCountToTen() {
        var override = new EntityLootOverride(null, 0, 0.0, 20);
        assertEquals(10, override.additionalDropCount());
    }

    @Test
    void preservesValidOverride() {
        var override = new EntityLootOverride(PROFILE, 50, -0.1, 2);
        assertEquals(PROFILE, override.profileId());
        assertEquals(50, override.itemLevelBonus());
        assertEquals(-0.1, override.dropChanceBonus());
        assertEquals(2, override.additionalDropCount());
    }
}
