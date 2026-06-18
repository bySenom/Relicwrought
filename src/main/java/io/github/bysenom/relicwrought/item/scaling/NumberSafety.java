package io.github.bysenom.relicwrought.item.scaling;

public final class NumberSafety {
    public static final double MAX_SCALED_VALUE = 1_000_000_000_000.0D;

    private NumberSafety() {
    }

    public static double requireFiniteNonNegative(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be finite: " + value);
        }
        if (value < 0.0D) {
            throw new IllegalArgumentException(name + " must not be negative: " + value);
        }
        if (value > MAX_SCALED_VALUE) {
            throw new IllegalArgumentException(name + " exceeds safety limit " + MAX_SCALED_VALUE + ": " + value);
        }
        return value;
    }

    public static long toSafeLong(double value, String name) {
        double checked = requireFiniteNonNegative(value, name);
        if (checked > Long.MAX_VALUE) {
            throw new IllegalArgumentException(name + " exceeds long range: " + checked);
        }
        return Math.max(0L, Math.round(checked));
    }
}
