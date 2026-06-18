package io.github.bysenom.relicwrought.item.model;

public record ItemLevel(int value) {
    public static final int MIN = 1;
    public static final int MAX = 950;

    public static ItemLevel of(int value) {
        return new ItemLevel(value);
    }

    public static ItemLevel clamp(int value) {
        return new ItemLevel(Math.max(MIN, Math.min(MAX, value)));
    }

    public ItemLevel {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException("Item level must be between " + MIN + " and " + MAX + ": " + value);
        }
    }
}
