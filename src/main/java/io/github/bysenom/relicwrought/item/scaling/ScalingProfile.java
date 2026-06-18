package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.model.KeyedDefinition;

public record ScalingProfile(
        DefinitionKey id,
        ScalingStat stat,
        ScalingCurve curve,
        RoundingStrategy rounding,
        double minimumClamp,
        double maximumClamp,
        int dataVersion
) implements KeyedDefinition {
    public ScalingProfile {
        NumberSafety.requireFiniteNonNegative(minimumClamp, "profile minimum clamp");
        NumberSafety.requireFiniteNonNegative(maximumClamp, "profile maximum clamp");
        if (maximumClamp < minimumClamp) {
            throw new IllegalArgumentException("Scaling profile maximum clamp must be >= minimum clamp");
        }
        if (dataVersion <= 0) {
            throw new IllegalArgumentException("Scaling profile data version must be positive");
        }
    }

    public double valueAt(ItemLevel itemLevel) {
        double value = curve.valueAt(itemLevel);
        value = Math.max(minimumClamp, Math.min(maximumClamp, value));
        value = rounding.apply(value);
        return NumberSafety.requireFiniteNonNegative(value, "scaling profile value " + id);
    }

    public long longValueAt(ItemLevel itemLevel) {
        return NumberSafety.toSafeLong(valueAt(itemLevel), "scaling profile long value " + id);
    }
}
