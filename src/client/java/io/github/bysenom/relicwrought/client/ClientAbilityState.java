package io.github.bysenom.relicwrought.client;

import io.github.bysenom.relicwrought.network.AbilitySlotSyncEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientAbilityState {
    private static List<String> slotAbilityIds = new ArrayList<>();
    private static final Map<String, Integer> remainingTicks = new HashMap<>();

    private ClientAbilityState() {
    }

    public static void updateLoadout(List<AbilitySlotSyncEntry> slots) {
        List<String> newIds = new ArrayList<>(9);
        if (slots != null) {
            for (AbilitySlotSyncEntry entry : slots) {
                if (entry != null && entry.abilityId() != null && entry.abilityId().isPresent()) {
                    newIds.add(entry.abilityId().get().toString());
                } else {
                    newIds.add(null);
                }
            }
        }
        while (newIds.size() < 9) {
            newIds.add(null);
        }
        if (newIds.size() > 9) {
            newIds = new ArrayList<>(newIds.subList(0, 9));
        }
        slotAbilityIds = newIds;
    }

    public static void updateCooldowns(Map<String, Integer> cooldowns) {
        remainingTicks.clear();
        if (cooldowns != null) {
            remainingTicks.putAll(cooldowns);
        }
    }

    public static String getAbilityId(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slotAbilityIds.size()) return null;
        return slotAbilityIds.get(slotIndex);
    }

    public static List<String> getSlotAbilityIds() {
        return List.copyOf(slotAbilityIds);
    }

    public static int getCooldownRemaining(String abilityId) {
        return remainingTicks.getOrDefault(abilityId, 0);
    }

    public static boolean isOnCooldown(String abilityId) {
        return remainingTicks.getOrDefault(abilityId, 0) > 0;
    }

    public static void tick() {
        remainingTicks.replaceAll((id, ticks) -> ticks > 0 ? ticks - 1 : 0);
        remainingTicks.values().removeIf(v -> v <= 0);
    }

    public static boolean hasAbilities() {
        for (String id : slotAbilityIds) {
            if (id != null) return true;
        }
        return false;
    }
}
