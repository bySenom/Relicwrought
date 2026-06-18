package io.github.bysenom.relicwrought.combat;

import io.github.bysenom.relicwrought.combat.damage.DamageBundle;
import io.github.bysenom.relicwrought.combat.damage.DamageType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DamageBundleTest {
    @Test
    void testSingleDamage() {
        DamageBundle bundle = DamageBundle.single(DamageType.PHYSICAL, 15.0);
        assertEquals(15.0, bundle.getTotalDamage());
        assertEquals(15.0, bundle.getDamage(DamageType.PHYSICAL));
        assertEquals(0.0, bundle.getDamage(DamageType.FIRE));
    }

    @Test
    void testMultipleDamages() {
        DamageBundle bundle = new DamageBundle(Map.of(
                DamageType.PHYSICAL, 10.0,
                DamageType.FIRE, 5.0
        ));
        assertEquals(15.0, bundle.getTotalDamage());
        assertEquals(10.0, bundle.getDamage(DamageType.PHYSICAL));
        assertEquals(5.0, bundle.getDamage(DamageType.FIRE));
    }

    @Test
    void testInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> DamageBundle.single(DamageType.PHYSICAL, -1.0));
        assertThrows(IllegalArgumentException.class, () -> DamageBundle.single(DamageType.PHYSICAL, Double.NaN));
    }
}
