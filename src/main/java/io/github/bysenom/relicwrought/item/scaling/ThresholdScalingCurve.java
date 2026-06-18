package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.item.model.ItemLevel;

import java.util.List;

public record ThresholdScalingCurve(List<ThresholdValue> thresholds) implements ScalingCurve {
    public ThresholdScalingCurve {
        thresholds = List.copyOf(thresholds);
        if (thresholds.isEmpty()) {
            throw new IllegalArgumentException("Threshold curve requires at least one threshold");
        }

        int previousLevel = -1;
        for (ThresholdValue threshold : thresholds) {
            int level = threshold.minimumItemLevel().value();
            if (level <= previousLevel) {
                throw new IllegalArgumentException("Thresholds must be strictly ascending by item level");
            }
            previousLevel = level;
        }
    }

    @Override
    public double valueAt(ItemLevel itemLevel) {
        int result = thresholds.getFirst().value();
        for (ThresholdValue threshold : thresholds) {
            if (itemLevel.value() < threshold.minimumItemLevel().value()) {
                return result;
            }
            result = threshold.value();
        }
        return result;
    }
}
