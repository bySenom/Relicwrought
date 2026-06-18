package io.github.bysenom.relicwrought.player;

import java.util.Objects;

public record StarterKitEntry(
        String itemBaseId,
        int itemLevel,
        int quality,
        int count,
        boolean autoEquip,
        String slot
) {
    public StarterKitEntry {
        if (itemBaseId == null || itemBaseId.isBlank()) {
            throw new IllegalArgumentException("Item base ID must not be blank");
        }
        if (itemLevel < 1 || itemLevel > 950) {
            throw new IllegalArgumentException("Item level must be between 1 and 950: " + itemLevel);
        }
        if (quality < 0 || quality > 20) {
            throw new IllegalArgumentException("Quality must be between 0 and 20: " + quality);
        }
        if (count < 1) {
            throw new IllegalArgumentException("Count must be at least 1: " + count);
        }
        slot = Objects.requireNonNullElse(slot, "");
    }
}
