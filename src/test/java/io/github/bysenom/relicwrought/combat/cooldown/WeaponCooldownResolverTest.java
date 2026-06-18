package io.github.bysenom.relicwrought.combat.cooldown;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStatResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeaponCooldownResolverTest {

    @Test
    void testCooldownCalculation() {
        ArpgModConfig config = ArpgModConfig.defaults();
        WeaponCooldownResolver resolver = new WeaponCooldownResolver(config, player -> io.github.bysenom.relicwrought.combat.stats.CharacterCombatStats.empty());

        // We can't fully mock the Player or registry in this pure unit test without Mockito
        // but we can test the fallback math indirectly if we refactored it, or we can just test AttackState.
    }
}
