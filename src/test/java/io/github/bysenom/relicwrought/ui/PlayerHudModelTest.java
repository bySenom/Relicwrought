package io.github.bysenom.relicwrought.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerHudModelTest {
    @Test
    void visibleHudUsesSyncedHealthWhenValid() {
        PlayerHudModel model = PlayerHudModel.resolve(
                true, true, true, true,
                45.0, 90.0,
                10.0, 20.0,
                120.0,
                CharacterResourceState.empty()
        );

        assertTrue(model.visible());
        assertEquals(45.0, model.currentHealth(), 0.001);
        assertEquals(90.0, model.maximumHealth(), 0.001);
        assertEquals(0.5, model.healthFill(), 0.001);
        assertTrue(model.vanillaHeartsHidden());
        assertTrue(model.vanillaArmorHidden());
    }

    @Test
    void missingArpgLifeFallsBackToScaledVanillaHealth() {
        PlayerHudModel model = PlayerHudModel.resolve(
                true, true, true, false,
                0.0, 0.0,
                5.0, 20.0,
                200.0,
                CharacterResourceState.empty()
        );

        assertEquals(50.0, model.currentHealth(), 0.001);
        assertEquals(200.0, model.maximumHealth(), 0.001);
        assertEquals(0.25, model.healthFill(), 0.001);
    }

    @Test
    void missingResolvedLifeFallsBackToVanillaHealth() {
        PlayerHudModel model = PlayerHudModel.resolve(
                true, true, true, false,
                Double.NaN, Double.NaN,
                7.0, 20.0,
                Double.NaN,
                CharacterResourceState.empty()
        );

        assertEquals(7.0, model.currentHealth(), 0.001);
        assertEquals(20.0, model.maximumHealth(), 0.001);
        assertEquals(0.35, model.healthFill(), 0.001);
    }

    @Test
    void resourceNoneIsSafeAndHidden() {
        PlayerHudModel model = PlayerHudModel.resolve(
                true, true, true, true,
                20.0, 20.0,
                20.0, 20.0,
                20.0,
                CharacterResourceState.empty()
        );

        assertFalse(model.resourceVisible());
        assertEquals(0.0, model.resourceFill(), 0.001);
    }

    @Test
    void disabledHudDoesNotClaimVanillaSuppression() {
        PlayerHudModel model = PlayerHudModel.resolve(
                false, true, true, true,
                20.0, 20.0,
                20.0, 20.0,
                20.0,
                CharacterResourceState.empty()
        );

        assertFalse(model.visible());
        assertFalse(model.vanillaHeartsHidden());
        assertFalse(model.vanillaArmorHidden());
    }
}
