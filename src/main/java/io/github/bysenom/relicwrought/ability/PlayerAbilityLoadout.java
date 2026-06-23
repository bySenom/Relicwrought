package io.github.bysenom.relicwrought.ability;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PlayerAbilityLoadout {
    public static final int SLOT_COUNT = 9;

    private final String[] abilityIds = new String[SLOT_COUNT];

    public PlayerAbilityLoadout() {
    }

    public PlayerAbilityLoadout(List<String> fromList) {
        for (int i = 0; i < SLOT_COUNT && i < fromList.size(); i++) {
            String val = fromList.get(i);
            if (val != null && !val.isBlank()) {
                abilityIds[i] = val;
            }
        }
    }

    public Optional<String> getAbilityId(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return Optional.empty();
        return Optional.ofNullable(abilityIds[slotIndex]);
    }

    public void setSlot(int slotIndex, String abilityId) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT)
            throw new IllegalArgumentException("Slot index out of bounds: " + slotIndex);
        abilityIds[slotIndex] = abilityId;
    }

    public void clearSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT)
            throw new IllegalArgumentException("Slot index out of bounds: " + slotIndex);
        abilityIds[slotIndex] = null;
    }

    public List<String> toList() {
        List<String> result = new ArrayList<>(SLOT_COUNT);
        for (String id : abilityIds) {
            result.add(id);
        }
        return result;
    }

    public static PlayerAbilityLoadout defaultsForClass(String classId) {
        PlayerAbilityLoadout loadout = new PlayerAbilityLoadout();
        if (classId == null) return loadout;
        // Strip namespace prefix if present (e.g. "relicwrought:warrior" -> "warrior")
        String shortId = classId.contains(":") ? classId.substring(classId.indexOf(':') + 1) : classId;
        if ("warrior".equals(shortId)) {
            loadout.abilityIds[0] = "relicwrought:power_strike";
            loadout.abilityIds[8] = "relicwrought:second_wind";
        } else if ("arcanist".equals(shortId)) {
            loadout.abilityIds[0] = "relicwrought:fire_bolt";
            loadout.abilityIds[8] = "relicwrought:second_wind";
        } else if ("rogue".equals(shortId)) {
            loadout.abilityIds[0] = "relicwrought:quick_jab";
            loadout.abilityIds[8] = "relicwrought:second_wind";
        } else if ("ranger".equals(shortId)) {
            loadout.abilityIds[8] = "relicwrought:second_wind";
        }
        return loadout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerAbilityLoadout that)) return false;
        return List.of(abilityIds).equals(List.of(that.abilityIds));
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object[]) abilityIds);
    }
}
