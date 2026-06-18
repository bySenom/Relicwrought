package io.github.bysenom.relicwrought.combat;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.combat.stats.AttributeCombatResolver;
import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStats;
import io.github.bysenom.relicwrought.progression.CharacterAttribute;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttributeCombatResolverTest {
    @Test
    void testStatResolution() {
        ArpgModConfig config = ArpgModConfig.defaults();
        CharacterCombatStats stats = AttributeCombatResolver.resolve(Map.of(
                CharacterAttribute.STRENGTH, 100,
                CharacterAttribute.DEXTERITY, 50,
                CharacterAttribute.INTELLIGENCE, 10,
                CharacterAttribute.VITALITY, 20
        ), config);

        assertEquals(0.2, stats.physicalDamagePercent(), 0.001); // 100 * 0.002
        assertEquals(100.0, stats.armor());
        assertEquals(0.05, stats.attackSpeedPercent(), 0.001); // 50 * 0.001
        assertEquals(0.025, stats.criticalStrikeChance(), 0.001); // 50 * 0.0005
        assertEquals(0.02, stats.elementalDamagePercent(), 0.001); // 10 * 0.002
        assertEquals(0.02, stats.fireResistance(), 0.001); // 10 * 0.002
        assertEquals(100.0, stats.maximumLife()); // 20 * 5
    }
}
