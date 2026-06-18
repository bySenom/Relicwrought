package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.item.model.ItemLevel;

public record LinearScalingCurve(double minimum, double maximum) implements ScalingCurve {
    public LinearScalingCurve {
        NumberSafety.requireFiniteNonNegative(minimum, "linear minimum");
        NumberSafety.requireFiniteNonNegative(maximum, "linear maximum");
        if (maximum < minimum) {
            throw new IllegalArgumentException("Linear curve maximum must be >= minimum");
        }
    }

    @Override
    public double valueAt(ItemLevel itemLevel) {
        double value = minimum + ((maximum - minimum) * ScalingMath.normalized(itemLevel));
        return NumberSafety.requireFiniteNonNegative(value, "linear curve value");
    }
}
