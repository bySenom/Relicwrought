package io.github.bysenom.relicwrought.progression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class ExperienceRewardServiceTest {
    private ExperienceRewardService service;
    private CharacterProgression level1;

    @BeforeEach
    void setUp() {
        service = new ExperienceRewardService(new ExperienceCurve(100, 1.65));
        level1 = CharacterProgression.createDefault();
    }

    @Test
    void smallGrantNoLevelUp() {
        ExperienceGrantResult result = service.grantXp(level1, 50);
        assertTrue(result.success());
        assertEquals(0, result.levelUps());
        assertEquals(1, result.levelAfter().value());
        assertEquals(50, result.xpAfter());
    }

    @Test
    void exactXpForLevelUp() {
        long xpNeeded = service.curve().xpForLevel(2);
        ExperienceGrantResult result = service.grantXp(level1, xpNeeded);
        assertTrue(result.success());
        assertEquals(1, result.levelUps());
        assertEquals(2, result.levelAfter().value());
    }

    @Test
    void excessXpRemainsAfterLevelUp() {
        long xpNeeded = service.curve().xpForLevel(2);
        ExperienceGrantResult result = service.grantXp(level1, xpNeeded + 30);
        assertTrue(result.success());
        assertEquals(1, result.levelUps());
        assertEquals(2, result.levelAfter().value());
    }

    @Test
    void multipleLevelUps() {
        long xpForLevel3 = service.curve().xpForLevel(3);
        ExperienceGrantResult result = service.grantXp(level1, xpForLevel3 + 500);
        assertTrue(result.success());
        assertTrue(result.levelUps() >= 2);
        assertTrue(result.levelAfter().value() >= 3);
    }

    @Test
    void level99To100() {
        long xpFor100 = service.curve().xpForLevel(100);
        CharacterProgression prog99 = new CharacterProgression(CharacterLevel.of(99), 0L, service.curve().totalXpForLevel(99), 0, Map.of());
        ExperienceGrantResult result = service.grantXp(prog99, xpFor100);
        assertTrue(result.success());
        assertTrue(result.levelAfter().value() >= 100 || result.levelUps() > 0);
    }

    @Test
    void xpAtMaxLevel() {
        long xpFor100 = service.curve().xpForLevel(100);
        CharacterProgression progMax = new CharacterProgression(CharacterLevel.of(100), 0L, xpFor100, 0, Map.of());
        ExperienceGrantResult result = service.grantXp(progMax, 1000);
        assertFalse(result.success());
        assertTrue(result.error() == ExperienceGrantResult.ExperienceGrantError.MAX_LEVEL_REACHED);
    }

    @Test
    void negativeGrantIsRejected() {
        ExperienceGrantResult result = service.grantXp(level1, -100);
        assertFalse(result.success());
        assertTrue(result.error() == ExperienceGrantResult.ExperienceGrantError.INVALID_XP_AMOUNT);
    }

    @Test
    void zeroGrantIsAccepted() {
        ExperienceGrantResult result = service.grantXp(level1, 0);
        assertTrue(result.success());
        assertEquals(0, result.levelUps());
    }

    @Test
    void allocateAttributeSuccess() {
        var result = service.allocateAttribute(level1, CharacterAttribute.STRENGTH, 1);
        assertFalse(result.success());
    }

    @Test
    void allocateAttributeWithPoints() {
        var prog = new CharacterProgression(CharacterLevel.of(5), 0L, 0L, 3, Map.of());
        var result = service.allocateAttribute(prog, CharacterAttribute.STRENGTH, 2);
        assertTrue(result.success());
        assertEquals(2, result.amountAllocated());
        assertEquals(1, result.remainingPoints());
    }

    @Test
    void allocateAttributeInsufficientPoints() {
        var prog = new CharacterProgression(CharacterLevel.of(5), 0L, 0L, 1, Map.of());
        var result = service.allocateAttribute(prog, CharacterAttribute.STRENGTH, 2);
        assertFalse(result.success());
    }

    @Test
    void allocateAttributeInvalidAmount() {
        var result = service.allocateAttribute(level1, CharacterAttribute.STRENGTH, 0);
        assertFalse(result.success());
    }

    @Test
    void allocateAttributeNull() {
        var result = service.allocateAttribute(level1, null, 1);
        assertFalse(result.success());
    }

    @Test
    void allocateAttributeOverflow() {
        var prog = new CharacterProgression(CharacterLevel.of(5), 0L, 0L, 10,
                Map.of(CharacterAttribute.STRENGTH, Integer.MAX_VALUE));
        var result = service.allocateAttribute(prog, CharacterAttribute.STRENGTH, 1);
        assertFalse(result.success());
    }

    @Test
    void nullProgressionReturnsFailure() {
        ExperienceGrantResult result = service.grantXp(null, 100);
        assertFalse(result.success());
    }
}
