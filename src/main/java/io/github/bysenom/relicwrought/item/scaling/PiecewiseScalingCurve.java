package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.item.model.ItemLevel;

import java.util.List;

public record PiecewiseScalingCurve(List<CurvePoint> points) implements ScalingCurve {
    public PiecewiseScalingCurve {
        points = List.copyOf(points);
        if (points.size() < 2) {
            throw new IllegalArgumentException("Piecewise curve requires at least two points");
        }

        int previousLevel = -1;
        for (CurvePoint point : points) {
            int level = point.itemLevel().value();
            if (level <= previousLevel) {
                throw new IllegalArgumentException("Piecewise curve points must be strictly ascending by item level");
            }
            previousLevel = level;
        }
    }

    @Override
    public double valueAt(ItemLevel itemLevel) {
        int level = itemLevel.value();
        CurvePoint first = points.getFirst();
        if (level <= first.itemLevel().value()) {
            return first.value();
        }

        for (int index = 1; index < points.size(); index++) {
            CurvePoint left = points.get(index - 1);
            CurvePoint right = points.get(index);
            if (level <= right.itemLevel().value()) {
                double span = right.itemLevel().value() - left.itemLevel().value();
                double t = (level - left.itemLevel().value()) / span;
                double value = left.value() + ((right.value() - left.value()) * t);
                return NumberSafety.requireFiniteNonNegative(value, "piecewise curve value");
            }
        }

        return points.getLast().value();
    }
}
