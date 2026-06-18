package io.github.bysenom.relicwrought.combat;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.combat.damage.DamageBundle;
import io.github.bysenom.relicwrought.combat.damage.DamageCalculationRequest;
import io.github.bysenom.relicwrought.combat.damage.DamageCalculationResult;
import io.github.bysenom.relicwrought.combat.damage.DamagePipeline;
import io.github.bysenom.relicwrought.combat.damage.DamageTag;
import io.github.bysenom.relicwrought.combat.damage.DamageType;
import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStats;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DamagePipelineTest {
    @Test
    void testPipelineCalculation() {
        ArpgModConfig config = ArpgModConfig.defaults();
        DamagePipeline pipeline = new DamagePipeline(config);

        CharacterCombatStats attackerStats = new CharacterCombatStats(
                0, 0, 0, 0, 0,
                0.5, // 50% phys damage
                0, 0, 0, 1.5, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        );

        CharacterCombatStats targetStats = new CharacterCombatStats(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 1.5, 0, 0,
                100, // 50% phys reduction
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        );

        DamageBundle weaponBundle = DamageBundle.single(DamageType.PHYSICAL, 20.0);

        DamageCalculationRequest request = new DamageCalculationRequest(
                1, attackerStats, targetStats, weaponBundle, Set.of(DamageTag.MELEE), 1.0, 12345L, false, false
        );

        DamageCalculationResult result = pipeline.calculate(request);
        assertTrue(result.success());
        // 20 * 1.5 = 30
        // 30 * (1 - 0.5) = 15
        assertEquals(15.0, result.totalDamage(), 0.001);
    }
}
