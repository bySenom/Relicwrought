package io.github.bysenom.relicwrought.item.scaling;

import java.util.Locale;

public enum RoundingStrategy {
    NONE {
        @Override
        public double apply(double value) {
            return value;
        }
    },
    FLOOR {
        @Override
        public double apply(double value) {
            return Math.floor(value);
        }
    },
    CEIL {
        @Override
        public double apply(double value) {
            return Math.ceil(value);
        }
    },
    NEAREST {
        @Override
        public double apply(double value) {
            return Math.round(value);
        }
    },
    ONE_DECIMAL {
        @Override
        public double apply(double value) {
            return Math.round(value * 10.0D) / 10.0D;
        }
    },
    TWO_DECIMALS {
        @Override
        public double apply(double value) {
            return Math.round(value * 100.0D) / 100.0D;
        }
    };

    public abstract double apply(double value);

    public static RoundingStrategy parse(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "NONE" -> NONE;
            case "FLOOR" -> FLOOR;
            case "CEIL", "CEILING" -> CEIL;
            case "NEAREST", "ROUND" -> NEAREST;
            case "ONE_DECIMAL", "ONE_DECIMALS" -> ONE_DECIMAL;
            case "TWO_DECIMAL", "TWO_DECIMALS" -> TWO_DECIMALS;
            default -> throw new IllegalArgumentException("Unknown rounding strategy: " + value);
        };
    }
}
