package io.github.bysenom.relicwrought.item.persistence;

import io.github.bysenom.relicwrought.item.model.ArpgItemData;

import java.util.List;
import java.util.Optional;

public record ArpgItemReadResult(
        ArpgItemReadStatus status,
        ArpgItemData itemData,
        List<String> messages
) {
    public ArpgItemReadResult {
        messages = List.copyOf(messages);
    }

    public static ArpgItemReadResult notArpgItem() {
        return new ArpgItemReadResult(ArpgItemReadStatus.NOT_ARPG_ITEM, null, List.of());
    }

    public static ArpgItemReadResult valid(ArpgItemData data) {
        return new ArpgItemReadResult(ArpgItemReadStatus.VALID, data, List.of());
    }

    public static ArpgItemReadResult migrated(ArpgItemData data, List<String> messages) {
        return new ArpgItemReadResult(ArpgItemReadStatus.MIGRATED, data, messages);
    }

    public static ArpgItemReadResult invalid(List<String> messages) {
        return new ArpgItemReadResult(ArpgItemReadStatus.INVALID, null, messages);
    }

    public static ArpgItemReadResult unsupportedVersion(int version) {
        return new ArpgItemReadResult(ArpgItemReadStatus.UNSUPPORTED_VERSION, null, List.of("unsupported_data_version:" + version));
    }

    public static ArpgItemReadResult missingDefinition(String detail) {
        return new ArpgItemReadResult(ArpgItemReadStatus.MISSING_DEFINITION, null, List.of(detail));
    }

    public Optional<ArpgItemData> data() {
        return Optional.ofNullable(itemData);
    }

    public boolean isValid() {
        return itemData != null && (status == ArpgItemReadStatus.VALID || status == ArpgItemReadStatus.MIGRATED);
    }
}
