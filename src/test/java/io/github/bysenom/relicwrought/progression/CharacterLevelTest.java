package io.github.bysenom.relicwrought.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class CharacterLevelTest {
    @Test
    void createsLevel1() {
        CharacterLevel level = CharacterLevel.of(1);
        assertEquals(1, level.value());
    }

    @Test
    void createsLevel100() {
        CharacterLevel level = CharacterLevel.of(100);
        assertEquals(100, level.value());
    }

    @Test
    void rejectsLevelBelowMin() {
        assertThrows(IllegalArgumentException.class, () -> CharacterLevel.of(0));
    }

    @Test
    void rejectsLevelAboveMax() {
        assertThrows(IllegalArgumentException.class, () -> CharacterLevel.of(101));
    }

    @Test
    void clampReturnsValidLevel() {
        assertEquals(1, CharacterLevel.clamp(0).value());
        assertEquals(100, CharacterLevel.clamp(150).value());
        assertEquals(50, CharacterLevel.clamp(50).value());
    }

    @Test
    void nextReturnsIncrementedLevel() {
        assertEquals(2, CharacterLevel.of(1).next().value());
    }

    @Test
    void nextDoesNotExceedMax() {
        assertEquals(100, CharacterLevel.of(100).next().value());
    }

    @Test
    void isMaxReturnsTrueFor100() {
        assertTrue(CharacterLevel.of(100).isMax());
    }

    @Test
    void isMaxReturnsFalseFor1() {
        assertFalse(CharacterLevel.of(1).isMax());
    }
}
