package io.github.bysenom.relicwrought.client.hud;

import io.github.bysenom.relicwrought.combat.cooldown.WeaponAttackState;

public class ClientWeaponCooldownState {
    private static final WeaponAttackState STATE = new WeaponAttackState();

    public static WeaponAttackState getState() {
        return STATE;
    }
}
