package io.github.bysenom.relicwrought.ability;

import java.util.HashMap;
import java.util.Map;

public final class PlayerAbilityCooldowns {
    private final Map<String, Integer> remainingTicks = new HashMap<>();

    public void startCooldown(String abilityId, int cooldownTicks) {
        if (cooldownTicks > 0) {
            remainingTicks.put(abilityId, cooldownTicks);
        }
    }

    public boolean isOnCooldown(String abilityId) {
        return remainingTicks.getOrDefault(abilityId, 0) > 0;
    }

    public int getRemainingTicks(String abilityId) {
        return remainingTicks.getOrDefault(abilityId, 0);
    }

    public void tick() {
        remainingTicks.replaceAll((id, ticks) -> ticks > 0 ? ticks - 1 : 0);
        remainingTicks.values().removeIf(v -> v <= 0);
    }

    public Map<String, Integer> getActiveCooldowns() {
        Map<String, Integer> active = new HashMap<>();
        for (Map.Entry<String, Integer> entry : remainingTicks.entrySet()) {
            if (entry.getValue() > 0) {
                active.put(entry.getKey(), entry.getValue());
            }
        }
        return active;
    }

    public void resetAll() {
        remainingTicks.clear();
    }
}
