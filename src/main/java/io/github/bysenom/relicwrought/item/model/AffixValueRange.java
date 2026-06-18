package io.github.bysenom.relicwrought.item.model;

import io.github.bysenom.relicwrought.item.scaling.NumberSafety;

public record AffixValueRange(double minValue, double maxValue) {
    public AffixValueRange {
        NumberSafety.requireFiniteNonNegative(minValue, "Affix tier min value");
        NumberSafety.requireFiniteNonNegative(maxValue, "Affix tier max value");
        if (maxValue < minValue) {
            throw new IllegalArgumentException("Affix tier max value must be >= min value");
        }
    }

    public double valueForRoll(double normalizedRoll) {
        if (normalizedRoll < 0.0D || normalizedRoll > 1.0D || Double.isNaN(normalizedRoll)) {
            throw new IllegalArgumentException("Normalized roll must be between 0.0 and 1.0: " + normalizedRoll);
        }
        return minValue + ((maxValue - minValue) * normalizedRoll);
    }
}
