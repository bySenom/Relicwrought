package io.github.bysenom.relicwrought.combat;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.combat.mitigation.ArmorMitigationResolver;
import io.github.bysenom.relicwrought.combat.mitigation.ResistanceResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MitigationResolversTest {
    @Test
    void testArmorMitigation() {
        ArpgModConfig config = ArpgModConfig.defaults(); // max = 0.85, constant = 100
        assertEquals(0.0, ArmorMitigationResolver.calculatePhysicalReduction(0, 1, config));
        assertEquals(0.5, ArmorMitigationResolver.calculatePhysicalReduction(100, 1, config)); // 100 / (100 + 100*1)
        assertEquals(0.85, ArmorMitigationResolver.calculatePhysicalReduction(100000, 1, config)); // clamped to 0.85
    }

    @Test
    void testResistanceMitigation() {
        ArpgModConfig config = ArpgModConfig.defaults(); // max = 0.75, min = -1.0
        assertEquals(0.5, ResistanceResolver.clampResistance(0.5, config));
        assertEquals(0.75, ResistanceResolver.clampResistance(1.5, config));
        assertEquals(-1.0, ResistanceResolver.clampResistance(-2.0, config));
    }
}
