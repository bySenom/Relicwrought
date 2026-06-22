package io.github.bysenom.relicwrought.client.hud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbilityHotbarTest {

    @BeforeEach
    void setUp() {
        // Reset state before each test
        AbilityHotbarState.setMode(HotbarMode.ITEM);
    }

    @AfterEach
    void tearDown() {
        AbilityHotbarState.setMode(HotbarMode.ITEM);
    }

    @Test
    void testHotbarModeToggle() {
        assertEquals(HotbarMode.ITEM, AbilityHotbarState.getCurrentMode(), "Default mode should be ITEM");
        
        AbilityHotbarState.toggleMode();
        assertEquals(HotbarMode.ABILITY, AbilityHotbarState.getCurrentMode(), "Mode should toggle to ABILITY");
        
        AbilityHotbarState.toggleMode();
        assertEquals(HotbarMode.ITEM, AbilityHotbarState.getCurrentMode(), "Mode should toggle back to ITEM");
    }

    @Test
    void testInputRouterInItemMode() {
        AbilityHotbarState.setMode(HotbarMode.ITEM);
        
        // In ITEM mode, the router should return false, allowing vanilla to handle the input and change the item slot
        boolean handled = AbilityInputRouter.handleHotbarKey(0);
        assertFalse(handled, "Input router should not handle hotbar keys in ITEM mode");
    }

    @Test
    void testShouldBlockHotbarScroll() {
        // ITEM Mode
        assertFalse(AbilityInputRouter.shouldBlockHotbarScroll(HotbarMode.ITEM, false, true), "Should NOT block scroll in ITEM mode");
        
        // ABILITY Mode
        assertTrue(AbilityInputRouter.shouldBlockHotbarScroll(HotbarMode.ABILITY, false, true), "Should block scroll in ABILITY mode without screen");
        
        // ABILITY Mode with Screen open
        assertFalse(AbilityInputRouter.shouldBlockHotbarScroll(HotbarMode.ABILITY, true, true), "Should NOT block scroll in ABILITY mode if screen is open");
        
        // ABILITY Mode but Player not present
        assertFalse(AbilityInputRouter.shouldBlockHotbarScroll(HotbarMode.ABILITY, false, false), "Should NOT block scroll in ABILITY mode if player is null");
    }

    // Note: To test AbilityInputRouter in ABILITY mode fully, we would need to mock ClientPlayNetworking.
    // In our current implementation, handleHotbarKey calls ClientPlayNetworking.send(), which fails in a unit test
    // environment without a real Minecraft client. So we just verify the state logic here.
}
