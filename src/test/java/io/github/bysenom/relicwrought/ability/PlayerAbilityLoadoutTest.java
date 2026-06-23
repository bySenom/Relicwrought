package io.github.bysenom.relicwrought.ability;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerAbilityLoadoutTest {

    @Test
    void equals_emptyLoadouts_areEqual() {
        PlayerAbilityLoadout a = new PlayerAbilityLoadout();
        PlayerAbilityLoadout b = new PlayerAbilityLoadout();
        assertEquals(a, b);
    }

    @Test
    void equals_withNullSlots_doesNotThrow() {
        PlayerAbilityLoadout a = new PlayerAbilityLoadout();
        PlayerAbilityLoadout b = new PlayerAbilityLoadout();
        // all slots are null — must not throw NullPointerException
        assertDoesNotThrow(() -> a.equals(b));
    }

    @Test
    void hashCode_withNullSlots_doesNotThrow() {
        PlayerAbilityLoadout a = new PlayerAbilityLoadout();
        assertDoesNotThrow(a::hashCode);
    }

    @Test
    void equals_sameAbilityInSlot_areEqual() {
        PlayerAbilityLoadout a = new PlayerAbilityLoadout();
        PlayerAbilityLoadout b = new PlayerAbilityLoadout();
        a.setSlot(0, "relicwrought:power_strike");
        b.setSlot(0, "relicwrought:power_strike");
        assertEquals(a, b);
    }

    @Test
    void equals_differentAbilityInSameSlot_notEqual() {
        PlayerAbilityLoadout a = new PlayerAbilityLoadout();
        PlayerAbilityLoadout b = new PlayerAbilityLoadout();
        a.setSlot(0, "relicwrought:power_strike");
        b.setSlot(0, "relicwrought:fire_bolt");
        assertNotEquals(a, b);
    }

    @Test
    void equals_partiallyFilledVsEmpty_notEqual() {
        PlayerAbilityLoadout a = new PlayerAbilityLoadout();
        PlayerAbilityLoadout b = new PlayerAbilityLoadout();
        a.setSlot(3, "relicwrought:second_wind");
        assertNotEquals(a, b);
    }

    @Test
    void equals_consistentWithHashCode() {
        PlayerAbilityLoadout a = new PlayerAbilityLoadout();
        PlayerAbilityLoadout b = new PlayerAbilityLoadout();
        a.setSlot(0, "relicwrought:power_strike");
        b.setSlot(0, "relicwrought:power_strike");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void fromList_nullEntriesIgnored() {
        List<String> input = new java.util.ArrayList<>();
        for (int i = 0; i < 9; i++) input.add(null);
        PlayerAbilityLoadout loadout = new PlayerAbilityLoadout(input);
        for (int i = 0; i < 9; i++) {
            assertTrue(loadout.getAbilityId(i).isEmpty(), "Slot " + i + " should be empty");
        }
    }

    @Test
    void clearSlot_makesSlotEmpty() {
        PlayerAbilityLoadout loadout = new PlayerAbilityLoadout();
        loadout.setSlot(0, "relicwrought:power_strike");
        loadout.clearSlot(0);
        assertTrue(loadout.getAbilityId(0).isEmpty());
    }

    @Test
    void setSlot_outOfBounds_throws() {
        PlayerAbilityLoadout loadout = new PlayerAbilityLoadout();
        assertThrows(IllegalArgumentException.class, () -> loadout.setSlot(9, "anything"));
        assertThrows(IllegalArgumentException.class, () -> loadout.setSlot(-1, "anything"));
    }
}
