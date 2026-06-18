package io.github.bysenom.relicwrought.item.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class ArpgNumberFormatter {
    private static final DecimalFormat TECHNICAL = new DecimalFormat("#.####", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DecimalFormat INTEGER = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DecimalFormat ONE_DECIMAL_FIXED = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.US));

    private ArpgNumberFormatter() {
    }

    public static String formatInt(int value) {
        return INTEGER.format(value);
    }

    public static String formatDouble(double value, int decimals) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        return switch (decimals) {
            case 0 -> INTEGER.format(Math.round(value));
            case 1 -> ONE_DECIMAL.format(value);
            case 2 -> TWO_DECIMALS.format(value);
            case 4 -> TECHNICAL.format(value);
            default -> TECHNICAL.format(value);
        };
    }

    public static String formatPercent(double value, int decimals) {
        return formatDouble(value, decimals);
    }

    public static String formatSigned(double value, int decimals) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        String sign = value >= 0 ? "+" : "-";
        String formatted = decimals == 1 ? ONE_DECIMAL_FIXED.format(Math.abs(value)) : formatDouble(Math.abs(value), decimals);
        return sign + formatted;
    }

    public static String formatSignedPercent(double value, int decimals) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0 %";
        }
        String sign = value >= 0 ? "+" : "-";
        String formatted = decimals == 1 ? ONE_DECIMAL_FIXED.format(Math.abs(value)) : formatDouble(Math.abs(value), decimals);
        return sign + formatted + " %";
    }

    public static String formatRange(double min, double max, int decimals) {
        return formatDouble(min, decimals) + "\u2013" + formatDouble(max, decimals);
    }

    public static String formatCompact(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        if (value < 1_000) {
            return INTEGER.format(Math.round(value));
        }
        if (value < 1_000_000) {
            return ONE_DECIMAL.format(value / 1_000.0) + "K";
        }
        if (value < 1_000_000_000) {
            return ONE_DECIMAL.format(value / 1_000_000.0) + "M";
        }
        if (value < 1_000_000_000_000L) {
            return ONE_DECIMAL.format(value / 1_000_000_000.0) + "B";
        }
        return ONE_DECIMAL.format(value / 1_000_000_000_000.0) + "T";
    }

    public static String formatTechnical(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Infinity" : "-Infinity";
        }
        return TECHNICAL.format(value);
    }

    public static String formatTechnicalLong(long value) {
        return String.valueOf(value);
    }

    public static String formatDurability(long current, long max) {
        return INTEGER.format(current) + " / " + INTEGER.format(max);
    }
}
