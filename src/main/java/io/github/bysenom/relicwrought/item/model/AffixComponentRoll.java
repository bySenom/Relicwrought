package io.github.bysenom.relicwrought.item.model;

import io.github.bysenom.relicwrought.item.scaling.NumberSafety;

public record AffixComponentRoll(
        String stat,
        AffixScope scope,
        AffixOperation operation,
        double normalizedRoll,
        double value
) {
    public AffixComponentRoll {
        if (stat == null || stat.isBlank()) {
            throw new IllegalArgumentException("Affix component stat must not be blank");
        }
        if (normalizedRoll < 0.0D || normalizedRoll > 1.0D || Double.isNaN(normalizedRoll)) {
            throw new IllegalArgumentException("Normalized roll must be between 0.0 and 1.0: " + normalizedRoll);
        }
        NumberSafety.requireFiniteNonNegative(value, "Affix component value");
    }
}
