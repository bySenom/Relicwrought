package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.item.model.ItemLevel;

public record ThresholdValue(ItemLevel minimumItemLevel, int value) {
    public ThresholdValue {
        if (value < 0) {
            throw new IllegalArgumentException("Threshold value must not be negative");
        }
    }
}
