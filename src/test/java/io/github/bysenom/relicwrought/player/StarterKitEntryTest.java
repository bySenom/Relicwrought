package io.github.bysenom.relicwrought.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class StarterKitEntryTest {
    @Test
    void createsValidEntry() {
        StarterKitEntry entry = new StarterKitEntry("relicwrought:starter_pickaxe", 1, 0, 1, false, "");
        assertEquals("relicwrought:starter_pickaxe", entry.itemBaseId());
        assertEquals(1, entry.itemLevel());
        assertEquals(0, entry.quality());
        assertEquals(1, entry.count());
        assertFalse(entry.autoEquip());
    }

    @Test
    void createsAutoEquipEntry() {
        StarterKitEntry entry = new StarterKitEntry("relicwrought:starter_training_sword", 1, 0, 1, true, "mainhand");
        assertTrue(entry.autoEquip());
        assertEquals("mainhand", entry.slot());
    }

    @Test
    void rejectsBlankItemBaseId() {
        assertThrows(IllegalArgumentException.class, () -> new StarterKitEntry("", 1, 0, 1, false, ""));
        assertThrows(IllegalArgumentException.class, () -> new StarterKitEntry("  ", 1, 0, 1, false, ""));
        assertThrows(IllegalArgumentException.class, () -> new StarterKitEntry(null, 1, 0, 1, false, ""));
    }

    @Test
    void rejectsItemLevelBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new StarterKitEntry("relicwrought:test", 0, 0, 1, false, ""));
        assertThrows(IllegalArgumentException.class, () -> new StarterKitEntry("relicwrought:test", -1, 0, 1, false, ""));
    }

    @Test
    void rejectsItemLevelAbove950() {
        assertThrows(IllegalArgumentException.class, () -> new StarterKitEntry("relicwrought:test", 951, 0, 1, false, ""));
        assertThrows(IllegalArgumentException.class, () -> new StarterKitEntry("relicwrought:test", 9999, 0, 1, false, ""));
    }

    @Test
    void acceptsBoundaryItemLevels() {
        assertDoesNotThrow(() -> new StarterKitEntry("relicwrought:test", 1, 0, 1, false, ""));
        assertDoesNotThrow(() -> new StarterKitEntry("relicwrought:test", 950, 0, 1, false, ""));
    }

    @Test
    void rejectsQualityBelowZero() {
        assertThrows(IllegalArgumentException.class, () -> new StarterKitEntry("relicwrought:test", 1, -1, 1, false, ""));
    }

    @Test
    void rejectsQualityAboveTwenty() {
        assertThrows(IllegalArgumentException.class, () -> new StarterKitEntry("relicwrought:test", 1, 21, 1, false, ""));
        assertThrows(IllegalArgumentException.class, () -> new StarterKitEntry("relicwrought:test", 1, 999, 1, false, ""));
    }

    @Test
    void acceptsBoundaryQualities() {
        assertDoesNotThrow(() -> new StarterKitEntry("relicwrought:test", 1, 0, 1, false, ""));
        assertDoesNotThrow(() -> new StarterKitEntry("relicwrought:test", 1, 20, 1, false, ""));
    }

    @Test
    void rejectsCountBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new StarterKitEntry("relicwrought:test", 1, 0, 0, false, ""));
        assertThrows(IllegalArgumentException.class, () -> new StarterKitEntry("relicwrought:test", 1, 0, -1, false, ""));
    }

    @Test
    void nullSlotDefaultsToEmpty() {
        StarterKitEntry entry = new StarterKitEntry("relicwrought:test", 1, 0, 1, false, null);
        assertEquals("", entry.slot());
    }
}
