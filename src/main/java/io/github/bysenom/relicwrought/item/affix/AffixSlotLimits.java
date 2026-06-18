package io.github.bysenom.relicwrought.item.affix;

public record AffixSlotLimits(int prefixes, int suffixes) {
    public static final int MAX_PREFIXES = 3;
    public static final int MAX_SUFFIXES = 3;

    public AffixSlotLimits {
        if (prefixes < 0 || prefixes > MAX_PREFIXES) {
            throw new IllegalArgumentException("Prefix slots must be between 0 and 3: " + prefixes);
        }
        if (suffixes < 0 || suffixes > MAX_SUFFIXES) {
            throw new IllegalArgumentException("Suffix slots must be between 0 and 3: " + suffixes);
        }
    }
}
