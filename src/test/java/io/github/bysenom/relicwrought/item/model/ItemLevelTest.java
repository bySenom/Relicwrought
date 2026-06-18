package io.github.bysenom.relicwrought.item.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ItemLevelTest {
    @Test
    void acceptsValidBoundaryLevels() {
        assertDoesNotThrow(() -> ItemLevel.of(1));
        assertDoesNotThrow(() -> ItemLevel.of(950));
    }

    @Test
    void rejectsLevelsOutsideAllowedRange() {
        assertThrows(IllegalArgumentException.class, () -> new ItemLevel(0));
        assertThrows(IllegalArgumentException.class, () -> new ItemLevel(951));
    }

    @Test
    void clampsOnlyWhenExplicitlyRequested() {
        org.junit.jupiter.api.Assertions.assertEquals(1, ItemLevel.clamp(-100).value());
        org.junit.jupiter.api.Assertions.assertEquals(950, ItemLevel.clamp(1200).value());
        org.junit.jupiter.api.Assertions.assertEquals(500, ItemLevel.clamp(500).value());
    }
}
