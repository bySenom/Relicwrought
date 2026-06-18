package io.github.bysenom.relicwrought.item.model;

import io.github.bysenom.relicwrought.item.ItemDataVersions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ArpgItemDataTest {
    @Test
    void storesCurrentDataVersionOnGeneratedItems() {
        ArpgItemData itemData = ArpgItemData.emptyGenerated(
                DefinitionKey.parse("starter_training_sword", "arpgmod"),
                new ItemLevel(1),
                Rarity.COMMON,
                1234L
        );

        assertEquals(ItemDataVersions.CURRENT, itemData.dataVersion());
        assertEquals(1, itemData.itemLevel().value());
    }

    @Test
    void rejectsMoreThanThreePrefixesOrSuffixes() {
        DefinitionKey key = DefinitionKey.parse("maximum_durability_percent", "arpgmod");
        AffixRoll roll = new AffixRoll(key, AffixTier.T10, 0.5D, 10.0D, 1);

        assertThrows(IllegalArgumentException.class, () -> new ArpgItemData(
                1,
                UUID.randomUUID(),
                DefinitionKey.parse("starter_pickaxe", "arpgmod"),
                new ItemLevel(1),
                0,
                Rarity.RARE,
                0,
                1L,
                false,
                List.of(),
                List.of(roll, roll, roll, roll),
                List.of()
        ));

        assertThrows(IllegalArgumentException.class, () -> new ArpgItemData(
                1,
                UUID.randomUUID(),
                DefinitionKey.parse("starter_pickaxe", "arpgmod"),
                new ItemLevel(1),
                0,
                Rarity.RARE,
                0,
                1L,
                false,
                List.of(),
                List.of(),
                List.of(roll, roll, roll, roll)
        ));
    }

    @Test
    void rejectsQualityOutsidePreparedRange() {
        assertThrows(IllegalArgumentException.class, () -> new ArpgItemData(
                1,
                UUID.randomUUID(),
                DefinitionKey.parse("starter_pickaxe", "arpgmod"),
                new ItemLevel(1),
                0,
                Rarity.COMMON,
                21,
                1L,
                false,
                List.of(),
                List.of(),
                List.of()
        ));
    }
}
