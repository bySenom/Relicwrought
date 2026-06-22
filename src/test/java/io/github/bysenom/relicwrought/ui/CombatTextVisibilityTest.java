package io.github.bysenom.relicwrought.ui;

import io.github.bysenom.relicwrought.combat.damage.CombatTextEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CombatTextVisibilityTest {
    @Test
    void positiveDamageIsVisibleWhenConfigAllowsIt() {
        UUID player = UUID.randomUUID();
        CombatTextEvent event = event(12.0, false, player);

        assertTrue(CombatTextVisibility.isVisible(event, true, true, true, player));
        assertEquals("12", CombatTextVisibility.textFor(event));
    }

    @Test
    void criticalDamageIsVisuallyDistinct() {
        CombatTextEvent event = event(34.0, true, UUID.randomUUID());

        assertEquals("34!", CombatTextVisibility.textFor(event));
    }

    @Test
    void invalidDamageValuesAreRejected() {
        UUID player = UUID.randomUUID();

        assertFalse(CombatTextVisibility.isVisible(event(-1.0, false, player), true, true, true, player));
        assertFalse(CombatTextVisibility.isVisible(event(Double.NaN, false, player), true, true, true, player));
        assertFalse(CombatTextVisibility.isVisible(event(Double.POSITIVE_INFINITY, false, player), true, true, true, player));
    }

    @Test
    void configCanHideDamageNumbers() {
        UUID player = UUID.randomUUID();
        CombatTextEvent event = event(12.0, false, player);

        assertFalse(CombatTextVisibility.isVisible(event, false, true, true, player));
        assertFalse(CombatTextVisibility.isVisible(event, true, false, true, player));
        assertFalse(CombatTextVisibility.isVisible(event, true, true, false, player));
    }

    private static CombatTextEvent event(double damage, boolean critical, UUID source) {
        return new CombatTextEvent(1, UUID.randomUUID(), source, damage, critical, "physical", 1L, 1L);
    }
}
