package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.item.model.ItemLevel;

public record CurvePoint(ItemLevel itemLevel, double value) {
    public CurvePoint {
        NumberSafety.requireFiniteNonNegative(value, "curve point value");
    }
}
