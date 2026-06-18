package io.github.bysenom.relicwrought.item.model;

import java.util.Locale;

public enum AffixOperation {
    ADDITIVE,
    ADD_FLAT,
    ADDITIVE_PERCENT,
    MULTIPLICATIVE_PERCENT;

    public static AffixOperation parse(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "ADDITIVE" -> ADDITIVE;
            case "ADD", "FLAT", "ADD_FLAT" -> ADD_FLAT;
            case "ADDITIVE_PERCENT", "ADD_PERCENT", "PERCENT" -> ADDITIVE_PERCENT;
            case "MULTIPLICATIVE_PERCENT", "MORE_PERCENT" -> MULTIPLICATIVE_PERCENT;
            default -> throw new IllegalArgumentException("Unknown affix operation: " + value);
        };
    }
}
