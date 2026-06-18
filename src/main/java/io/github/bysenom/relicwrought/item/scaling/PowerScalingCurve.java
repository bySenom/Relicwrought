package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.item.model.ItemLevel;

public record PowerScalingCurve(double minimum, double maximum, double exponent) implements ScalingCurve {
    public PowerScalingCurve {
        NumberSafety.requireFiniteNonNegative(minimum, "power minimum");
        NumberSafety.requireFiniteNonNegative(maximum, "power maximum");
        NumberSafety.requireFiniteNonNegative(exponent, "power exponent");
        if (maximum < minimum) {
            throw new IllegalArgumentException("Power curve maximum must be >= minimum");
        }
        if (exponent <= 0.0D) {
            throw new IllegalArgumentException("Power curve exponent must be positive");
        }
    }

    @Override
    public double valueAt(ItemLevel itemLevel) {
        double t = Math.pow(ScalingMath.normalized(itemLevel), exponent);
        double value = minimum + ((maximum - minimum) * t);
        return NumberSafety.requireFiniteNonNegative(value, "power curve value");
    }
}
