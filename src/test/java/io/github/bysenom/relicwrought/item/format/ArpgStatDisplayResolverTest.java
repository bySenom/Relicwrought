package io.github.bysenom.relicwrought.item.format;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ArpgStatDisplayResolverTest {

    @Test
    void resolvesMaximumDurability() {
        assertEquals("stat.relicwrought.maximum_durability",
                ArpgStatDisplayResolver.resolveTranslationKey("maximum_durability"));
    }

    @Test
    void resolvesFireResistance() {
        assertEquals("stat.relicwrought.fire_resistance",
                ArpgStatDisplayResolver.resolveTranslationKey("fire_resistance"));
    }

    @Test
    void resolvesCustomStat() {
        assertEquals("stat.relicwrought.test_stat",
                ArpgStatDisplayResolver.resolveTranslationKey("test_stat"));
    }

    @Test
    void prefixIsAlwaysStatArpgmod() {
        String result = ArpgStatDisplayResolver.resolveTranslationKey("anything");
        assertTrue(result.startsWith("stat.relicwrought."));
    }

    @Test
    void emptyStringKey() {
        assertEquals("stat.relicwrought.",
                ArpgStatDisplayResolver.resolveTranslationKey(""));
    }
}
