package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.item.model.ItemLevel;

final class ScalingMath {
    private ScalingMath() {
    }

    static double normalized(ItemLevel itemLevel) {
        return (itemLevel.value() - ItemLevel.MIN) / (double) (ItemLevel.MAX - ItemLevel.MIN);
    }
}
