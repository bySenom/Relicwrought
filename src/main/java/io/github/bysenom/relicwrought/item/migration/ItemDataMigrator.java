package io.github.bysenom.relicwrought.item.migration;

import io.github.bysenom.relicwrought.item.ItemDataVersions;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;

import java.util.Comparator;
import java.util.List;

public final class ItemDataMigrator {
    private final List<ItemDataMigration> migrations;

    public ItemDataMigrator(List<ItemDataMigration> migrations) {
        this.migrations = migrations.stream()
                .sorted(Comparator.comparingInt(ItemDataMigration::sourceVersion))
                .toList();
    }

    public ArpgItemData migrateToCurrent(ArpgItemData itemData) {
        ArpgItemData current = itemData;
        while (current.dataVersion() < ItemDataVersions.CURRENT) {
            int version = current.dataVersion();
            ItemDataMigration migration = migrations.stream()
                    .filter(candidate -> candidate.sourceVersion() == version)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No item data migration registered for version " + version));
            current = migration.migrate(current);
        }
        return current;
    }
}
