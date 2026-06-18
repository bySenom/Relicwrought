package io.github.bysenom.relicwrought.item.persistence;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.ItemDataVersions;
import io.github.bysenom.relicwrought.item.migration.ItemDataMigration;
import io.github.bysenom.relicwrought.item.migration.ItemDataMigrator;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ArpgItemStackService {
    private final ItemDataMigrator migrator;

    public ArpgItemStackService(List<ItemDataMigration> migrations) {
        this.migrator = new ItemDataMigrator(migrations);
    }

    public boolean hasArpgData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.has(ArpgItemComponents.ARPG_ITEM_DATA);
    }

    public ArpgItemReadResult read(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ArpgItemReadResult.notArpgItem();
        }

        ArpgItemComponent component = stack.get(ArpgItemComponents.ARPG_ITEM_DATA);
        if (component == null) {
            return ArpgItemReadResult.notArpgItem();
        }

        ArpgItemData rawData = component.data();
        if (rawData == null) {
            return ArpgItemReadResult.invalid(List.of("null_data_in_component"));
        }

        List<String> messages = new ArrayList<>();

        if (rawData.dataVersion() > ItemDataVersions.CURRENT) {
            return ArpgItemReadResult.unsupportedVersion(rawData.dataVersion());
        }

        ArpgItemData migratedData;
        try {
            migratedData = migrator.migrateToCurrent(rawData);
        } catch (Exception e) {
            return ArpgItemReadResult.invalid(List.of("migration_failed:" + e.getMessage()));
        }

        if (migratedData.dataVersion() != ItemDataVersions.CURRENT) {
            return ArpgItemReadResult.invalid(List.of("migration_did_not_reach_current:" + migratedData.dataVersion()));
        }

        List<String> validationErrors = ArpgItemPersistenceValidator.validate(migratedData);
        if (!validationErrors.isEmpty()) {
            return ArpgItemReadResult.invalid(validationErrors);
        }

        if (migratedData != rawData) {
            messages.add("migrated_from_version:" + rawData.dataVersion());
        }

        return new ArpgItemReadResult(
                migratedData != rawData ? ArpgItemReadStatus.MIGRATED : ArpgItemReadStatus.VALID,
                migratedData,
                messages
        );
    }

    public ArpgItemWriteResult write(ItemStack stack, ArpgItemData data) {
        if (stack == null || stack.isEmpty()) {
            return ArpgItemWriteResult.failure(List.of("stack_empty"));
        }

        List<String> errors = ArpgItemPersistenceValidator.validate(data);
        if (!errors.isEmpty()) {
            return ArpgItemWriteResult.failure(errors);
        }

        try {
            stack.set(ArpgItemComponents.ARPG_ITEM_DATA, new ArpgItemComponent(data));
            return ArpgItemWriteResult.success();
        } catch (Exception e) {
            Relicwrought.LOGGER.warn("Failed to write ARPG item data: {}", e.getMessage());
            return ArpgItemWriteResult.failure(List.of("write_failed:" + e.getMessage()));
        }
    }

    public boolean remove(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!stack.has(ArpgItemComponents.ARPG_ITEM_DATA)) {
            return false;
        }
        stack.remove(ArpgItemComponents.ARPG_ITEM_DATA);
        return true;
    }

    public ArpgItemData require(ItemStack stack) {
        return read(stack).data()
                .orElseThrow(() -> new IllegalStateException("ItemStack does not contain valid ARPG item data"));
    }

    public Optional<ArpgItemData> readOptional(ItemStack stack) {
        return read(stack).data();
    }
}
