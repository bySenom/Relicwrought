package io.github.bysenom.relicwrought.ui;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EnemyNameplateModelTest {
    @Test
    void hostileZombieCreatesVisibleNameplateWithOneHealthBarFlag() {
        EnemyNameplateModel model = EnemyNameplateModel.fromSnapshot(
                snapshot("Zombie", 12.0, 20.0, true),
                true, true, true
        );

        assertTrue(model.visible());
        assertEquals("Lv. ? Zombie", model.title());
        assertEquals("12 / 20", model.healthText());
        assertEquals(0.6, model.healthFill(), 0.001);
        assertTrue(model.healthBarVisible());
        assertTrue(model.healthNumbersVisible());
    }

    @Test
    void nonHostileSnapshotIsHidden() {
        EnemyNameplateModel model = EnemyNameplateModel.fromSnapshot(
                snapshot("Armor Stand", 20.0, 20.0, false),
                true, true, true
        );

        assertFalse(model.visible());
    }

    @Test
    void deadOrInvalidHealthIsHidden() {
        assertFalse(EnemyNameplateModel.fromSnapshot(snapshot("Zombie", 0.0, 20.0, true), true, true, true).visible());
        assertFalse(EnemyNameplateModel.fromSnapshot(snapshot("Zombie", 12.0, Double.NaN, true), true, true, true).visible());
        assertFalse(EnemyNameplateModel.fromSnapshot(snapshot("Zombie", 12.0, Double.POSITIVE_INFINITY, true), true, true, true).visible());
    }

    private static EnemyUiSnapshot snapshot(String name, double current, double max, boolean hostile) {
        return new EnemyUiSnapshot(
                1, UUID.randomUUID(), name, EnemyClassification.NORMAL, 0,
                current, max, hostile, false, 0
        );
    }
}
