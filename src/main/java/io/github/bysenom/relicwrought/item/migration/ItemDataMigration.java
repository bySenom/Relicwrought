package io.github.bysenom.relicwrought.item.migration;

import io.github.bysenom.relicwrought.item.model.ArpgItemData;

public interface ItemDataMigration {
    int sourceVersion();

    int targetVersion();

    ArpgItemData migrate(ArpgItemData itemData);
}
