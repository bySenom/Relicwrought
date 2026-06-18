package io.github.bysenom.relicwrought.loot;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LootDropResultTest {
    private static final DefinitionKey PROFILE = DefinitionKey.parse("test_profile", Relicwrought.MOD_ID);
    private static final long SEED = 12345L;

    @Test
    void noDropFactory() {
        var result = LootDropResult.noDrop(PROFILE, LootSourceType.NORMAL_MOB, 0.08, SEED);
        assertFalse(result.didDrop());
        assertEquals(0, result.requestedCount());
        assertEquals(0, result.successfulCount());
        assertEquals(0, result.failedCount());
        assertTrue(result.itemLevels().isEmpty());
        assertTrue(result.generatedItems().isEmpty());
        assertEquals(LootErrorCode.NONE, result.errorCode());
    }

    @Test
    void failureFactory() {
        var result = LootDropResult.failure(PROFILE, LootSourceType.BOSS, SEED,
                LootErrorCode.NO_ELIGIBLE_ITEM_BASE, List.of("no items available"));
        assertFalse(result.didDrop());
        assertEquals(LootErrorCode.NO_ELIGIBLE_ITEM_BASE, result.errorCode());
        assertEquals("no items available", result.warnings().getFirst());
    }

    @Test
    void successfulDrop() {
        var result = new LootDropResult(
                PROFILE, LootSourceType.NORMAL_MOB, 0.15, true, 3, 2, 1,
                List.of(50, 120), SEED, List.of(), LootErrorCode.NONE, List.of()
        );
        assertTrue(result.didDrop());
        assertEquals(3, result.requestedCount());
        assertEquals(2, result.successfulCount());
        assertEquals(1, result.failedCount());
        assertEquals(2, result.itemLevels().size());
    }

    @Test
    void nullSafety() {
        var result = new LootDropResult(
                PROFILE, LootSourceType.NORMAL_MOB, 0.5, false, 0, 0, 0,
                null, SEED, null, LootErrorCode.NONE, null
        );
        assertTrue(result.itemLevels().isEmpty());
        assertTrue(result.generatedItems().isEmpty());
        assertTrue(result.warnings().isEmpty());
    }
}
