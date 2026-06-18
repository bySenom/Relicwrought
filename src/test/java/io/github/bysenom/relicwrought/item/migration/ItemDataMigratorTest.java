package io.github.bysenom.relicwrought.item.migration;

import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.model.Rarity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

final class ItemDataMigratorTest {
    @Test
    void returnsCurrentVersionWithoutMigration() {
        ArpgItemData itemData = ArpgItemData.emptyGenerated(
                DefinitionKey.parse("starter_training_sword", "arpgmod"),
                new ItemLevel(1),
                Rarity.COMMON,
                42L
        );

        assertSame(itemData, new ItemDataMigrator(List.of()).migrateToCurrent(itemData));
    }
}
