package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.item.model.ItemLevel;

public interface ScalingCurve {
    double valueAt(ItemLevel itemLevel);
}
