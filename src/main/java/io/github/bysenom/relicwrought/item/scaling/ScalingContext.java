package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.item.model.ItemLevel;

public record ScalingContext(ItemLevel itemLevel, ItemQuality quality) {
    public static ScalingContext of(ItemLevel itemLevel, ItemQuality quality) {
        return new ScalingContext(itemLevel, quality);
    }
}
