package io.github.bysenom.relicwrought.item.persistence;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.ItemDataVersions;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.model.Rarity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArpgItemReadResultTest {
    @Test
    void notArpgItemHasNoData() {
        ArpgItemReadResult result = ArpgItemReadResult.notArpgItem();
        assertEquals(ArpgItemReadStatus.NOT_ARPG_ITEM, result.status());
        assertTrue(result.data().isEmpty());
        assertFalse(result.isValid());
    }

    @Test
    void validResultHasData() {
        ArpgItemData data = ArpgItemData.emptyGenerated(
                DefinitionKey.parse("starter_training_sword", Relicwrought.MOD_ID),
                new ItemLevel(1), Rarity.COMMON, 42L
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        assertEquals(ArpgItemReadStatus.VALID, result.status());
        assertTrue(result.isValid());
        assertTrue(result.data().isPresent());
        assertEquals(data, result.data().get());
    }

    @Test
    void invalidResultHasMessages() {
        ArpgItemReadResult result = ArpgItemReadResult.invalid(List.of("error1", "error2"));
        assertEquals(ArpgItemReadStatus.INVALID, result.status());
        assertFalse(result.isValid());
        assertEquals(2, result.messages().size());
    }

    @Test
    void unsupportedVersionResult() {
        ArpgItemReadResult result = ArpgItemReadResult.unsupportedVersion(99);
        assertEquals(ArpgItemReadStatus.UNSUPPORTED_VERSION, result.status());
        assertFalse(result.isValid());
    }

    @Test
    void missingDefinitionResult() {
        ArpgItemReadResult result = ArpgItemReadResult.missingDefinition("missing_base:unknown_item");
        assertEquals(ArpgItemReadStatus.MISSING_DEFINITION, result.status());
        assertFalse(result.isValid());
    }

    @Test
    void migratedResultIsValid() {
        ArpgItemData data = ArpgItemData.emptyGenerated(
                DefinitionKey.parse("starter_training_sword", Relicwrought.MOD_ID),
                new ItemLevel(1), Rarity.COMMON, 42L
        );
        ArpgItemReadResult result = ArpgItemReadResult.migrated(data, List.of("migrated_from_version:0"));
        assertEquals(ArpgItemReadStatus.MIGRATED, result.status());
        assertTrue(result.isValid());
        assertTrue(result.data().isPresent());
    }
}
