package io.github.bysenom.relicwrought.combat.cooldown;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WeaponAttackStateTest {

    @Test
    void testInitialState() {
        WeaponAttackState state = new WeaponAttackState();
        assertTrue(state.isReady(0));
        assertEquals(1.0, state.getProgress(0));
    }

    @Test
    void testAttackRecording() {
        WeaponAttackState state = new WeaponAttackState();
        state.recordAttack(100, 20, 1.0);

        assertFalse(state.isReady(105));
        assertEquals(0.25, state.getProgress(105), 0.01);

        assertTrue(state.isReady(120));
        assertEquals(1.0, state.getProgress(125));
    }

    @Test
    void testWeaponSwap() {
        WeaponAttackState state = new WeaponAttackState();
        UUID weaponA = UUID.randomUUID();
        UUID weaponB = UUID.randomUUID();

        assertTrue(state.checkWeaponSwap(100, weaponA)); // initial swap
        assertFalse(state.checkWeaponSwap(105, weaponA)); // same weapon
        assertTrue(state.checkWeaponSwap(110, weaponB)); // new weapon
    }
}
