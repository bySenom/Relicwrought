package io.github.bysenom.relicwrought.item.format;

import io.github.bysenom.relicwrought.item.ItemDataVersions;
import io.github.bysenom.relicwrought.item.model.*;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadResult;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class ArpgItemCommandServiceTest {
    @Test
    void validItemDataReadsAsValid() {
        DefinitionKey key = DefinitionKey.parse("test_sword", "arpgmod");
        ArpgItemData data = new ArpgItemData(
                ItemDataVersions.CURRENT, UUID.randomUUID(), key,
                new ItemLevel(50), 0, Rarity.MAGIC, 0, 12345L, false,
                List.of(), List.of(), List.of()
        );
        ArpgItemReadResult result = ArpgItemReadResult.valid(data);
        assertEquals(ArpgItemReadStatus.VALID, result.status());
        assertTrue(result.data().isPresent());
    }

    @Test
    void invalidDataReadsAsInvalid() {
        ArpgItemReadResult result = ArpgItemReadResult.invalid(List.of("corrupted"));
        assertEquals(ArpgItemReadStatus.INVALID, result.status());
        assertTrue(result.data().isEmpty());
    }

    @Test
    void notArpgReadsAsNotArpg() {
        ArpgItemReadResult result = ArpgItemReadResult.notArpgItem();
        assertEquals(ArpgItemReadStatus.NOT_ARPG_ITEM, result.status());
    }

    @Test
    void migratedItemHasDataWithMigrationStatus() {
        DefinitionKey key = DefinitionKey.parse("test_sword", "arpgmod");
        ArpgItemData data = new ArpgItemData(
                ItemDataVersions.CURRENT, UUID.randomUUID(), key,
                new ItemLevel(50), 0, Rarity.MAGIC, 0, 12345L, false,
                List.of(), List.of(), List.of()
        );
        ArpgItemReadResult result = ArpgItemReadResult.migrated(data, List.of("migrated from v0"));
        assertEquals(ArpgItemReadStatus.MIGRATED, result.status());
        assertTrue(result.data().isPresent());
        assertEquals(1, result.messages().size());
    }

    @Test
    void unsupportedVersionHasNoData() {
        ArpgItemReadResult result = ArpgItemReadResult.unsupportedVersion(99);
        assertEquals(ArpgItemReadStatus.UNSUPPORTED_VERSION, result.status());
        assertTrue(result.data().isEmpty());
    }
}
