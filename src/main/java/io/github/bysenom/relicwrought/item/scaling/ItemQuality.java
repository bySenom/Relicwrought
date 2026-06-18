package io.github.bysenom.relicwrought.item.scaling;

public record ItemQuality(int percent) {
    public static final int MIN = 0;
    public static final int MAX = 20;

    public static ItemQuality of(int percent) {
        return new ItemQuality(percent);
    }

    public ItemQuality {
        if (percent < MIN || percent > MAX) {
            throw new IllegalArgumentException("Item quality must be between " + MIN + " and " + MAX + ": " + percent);
        }
    }

    public double multiplier() {
        return 1.0D + (percent / 100.0D);
    }
}
