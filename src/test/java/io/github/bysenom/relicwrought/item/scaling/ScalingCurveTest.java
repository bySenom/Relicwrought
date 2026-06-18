package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.item.model.ItemLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ScalingCurveTest {
    @Test
    void linearCurveInterpolatesBoundariesAndMidpoint() {
        LinearScalingCurve curve = new LinearScalingCurve(10.0D, 110.0D);

        assertEquals(10.0D, curve.valueAt(ItemLevel.of(1)), 0.00001D);
        assertEquals(110.0D, curve.valueAt(ItemLevel.of(950)), 0.00001D);
        assertEquals(60.0D, curve.valueAt(ItemLevel.of(475)), 0.06D);
    }

    @Test
    void powerCurveIsMonotonicAndFinite() {
        PowerScalingCurve curve = new PowerScalingCurve(2.0D, 100000.0D, 2.2D);

        double previous = curve.valueAt(ItemLevel.of(1));
        for (int level = 2; level <= 950; level += 50) {
            double value = curve.valueAt(ItemLevel.of(level));
            assertTrue(value >= previous);
            assertTrue(Double.isFinite(value));
            previous = value;
        }

        assertEquals(2.0D, curve.valueAt(ItemLevel.of(1)), 0.00001D);
        assertEquals(100000.0D, curve.valueAt(ItemLevel.of(950)), 0.00001D);
    }

    @Test
    void piecewiseCurveHitsPointsAndInterpolatesLinearly() {
        PiecewiseScalingCurve curve = new PiecewiseScalingCurve(List.of(
                new CurvePoint(ItemLevel.of(1), 5.0D),
                new CurvePoint(ItemLevel.of(50), 40.0D),
                new CurvePoint(ItemLevel.of(150), 200.0D),
                new CurvePoint(ItemLevel.of(950), 250000.0D)
        ));

        assertEquals(5.0D, curve.valueAt(ItemLevel.of(1)), 0.00001D);
        assertEquals(40.0D, curve.valueAt(ItemLevel.of(50)), 0.00001D);
        assertEquals(120.0D, curve.valueAt(ItemLevel.of(100)), 0.00001D);
        assertEquals(250000.0D, curve.valueAt(ItemLevel.of(950)), 0.00001D);
    }

    @Test
    void piecewiseCurveRejectsUnsortedOrDuplicatePoints() {
        assertThrows(IllegalArgumentException.class, () -> new PiecewiseScalingCurve(List.of(
                new CurvePoint(ItemLevel.of(100), 10.0D),
                new CurvePoint(ItemLevel.of(50), 20.0D)
        )));

        assertThrows(IllegalArgumentException.class, () -> new PiecewiseScalingCurve(List.of(
                new CurvePoint(ItemLevel.of(50), 10.0D),
                new CurvePoint(ItemLevel.of(50), 20.0D)
        )));
    }

    @Test
    void thresholdCurveSelectsDiscreteValues() {
        ThresholdScalingCurve curve = new ThresholdScalingCurve(List.of(
                new ThresholdValue(ItemLevel.of(1), 1),
                new ThresholdValue(ItemLevel.of(100), 2),
                new ThresholdValue(ItemLevel.of(250), 3),
                new ThresholdValue(ItemLevel.of(900), 7)
        ));

        assertEquals(1.0D, curve.valueAt(ItemLevel.of(99)));
        assertEquals(2.0D, curve.valueAt(ItemLevel.of(100)));
        assertEquals(3.0D, curve.valueAt(ItemLevel.of(449)));
        assertEquals(7.0D, curve.valueAt(ItemLevel.of(950)));
    }

    @Test
    void roundingStrategiesAreCentralized() {
        assertEquals(10.0D, RoundingStrategy.FLOOR.apply(10.9D));
        assertEquals(11.0D, RoundingStrategy.CEIL.apply(10.1D));
        assertEquals(11.0D, RoundingStrategy.NEAREST.apply(10.6D));
        assertEquals(10.1D, RoundingStrategy.ONE_DECIMAL.apply(10.14D));
        assertEquals(10.15D, RoundingStrategy.TWO_DECIMALS.apply(10.145D));
    }
}
