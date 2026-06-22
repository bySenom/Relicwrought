package io.github.bysenom.relicwrought.ui;

import io.github.bysenom.relicwrought.combat.damage.CombatTextEvent;

import java.util.UUID;

public final class CombatTextVisibility {
    private CombatTextVisibility() {
    }

    public static boolean isVisible(
            CombatTextEvent event,
            boolean combatTextEnabled,
            boolean floatingNumbersEnabled,
            boolean showOwnDamage,
            UUID localPlayerUuid
    ) {
        if (!combatTextEnabled || !floatingNumbersEnabled || event == null) {
            return false;
        }
        double damage = event.totalDamage();
        if (Double.isNaN(damage) || Double.isInfinite(damage) || damage <= 0.0) {
            return false;
        }
        return showOwnDamage || localPlayerUuid == null || !localPlayerUuid.equals(event.sourcePlayerUuid());
    }

    public static String textFor(CombatTextEvent event) {
        if (event == null) {
            return "";
        }
        String text = String.valueOf((int) Math.max(0.0, event.totalDamage()));
        if (event.critical()) {
            text += "!";
        }
        return text;
    }
}
