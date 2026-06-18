package io.github.bysenom.relicwrought.item.model;

import io.github.bysenom.relicwrought.item.scaling.RoundingStrategy;

import java.util.List;

public record AffixTierDefinition(
        AffixTier tier,
        int minimumItemLevel,
        int weight,
        RoundingStrategy rounding,
        List<AffixValueRange> values
) {
    public AffixTierDefinition(AffixTier tier, int minimumItemLevel, double minValue, double maxValue) {
        this(tier, minimumItemLevel, 100, RoundingStrategy.NONE, List.of(new AffixValueRange(minValue, maxValue)));
    }

    public AffixTierDefinition {
        if (minimumItemLevel < ItemLevel.MIN || minimumItemLevel > ItemLevel.MAX) {
            throw new IllegalArgumentException("Affix tier minimum item level is out of range: " + minimumItemLevel);
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Affix tier weight must be positive");
        }
        values = List.copyOf(values);
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Affix tier must define at least one value range");
        }
    }

    public boolean isUnlockedAt(ItemLevel itemLevel) {
        return itemLevel.value() >= minimumItemLevel;
    }

    public double valueForRoll(double normalizedRoll) {
        return valueForRoll(0, normalizedRoll);
    }

    public double valueForRoll(int componentIndex, double normalizedRoll) {
        return rounding.apply(values.get(componentIndex).valueForRoll(normalizedRoll));
    }

    public double minValue() {
        return values.getFirst().minValue();
    }

    public double maxValue() {
        return values.getFirst().maxValue();
    }
}
