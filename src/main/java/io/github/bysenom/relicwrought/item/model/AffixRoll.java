package io.github.bysenom.relicwrought.item.model;

import io.github.bysenom.relicwrought.item.scaling.NumberSafety;

import java.util.List;

public record AffixRoll(
        DefinitionKey affixId,
        AffixTier tier,
        double normalizedRoll,
        double value,
        List<AffixComponentRoll> componentRolls,
        int dataVersion
) {
    public AffixRoll(DefinitionKey affixId, AffixTier tier, double normalizedRoll, double value, int dataVersion) {
        this(affixId, tier, normalizedRoll, value, List.of(), dataVersion);
    }

    public AffixRoll {
        if (normalizedRoll < 0.0D || normalizedRoll > 1.0D || Double.isNaN(normalizedRoll)) {
            throw new IllegalArgumentException("Normalized roll must be between 0.0 and 1.0: " + normalizedRoll);
        }
        NumberSafety.requireFiniteNonNegative(value, "Affix value");
        componentRolls = List.copyOf(componentRolls);
        if (dataVersion <= 0) {
            throw new IllegalArgumentException("Affix roll data version must be positive");
        }
    }
}
