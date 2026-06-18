package io.github.bysenom.relicwrought.progression;

public record CharacterLevel(int value) {
    public static final int MIN = 1;
    public static final int MAX = 100;

    public static CharacterLevel of(int value) {
        return new CharacterLevel(value);
    }

    public static CharacterLevel clamp(int value) {
        return new CharacterLevel(Math.max(MIN, Math.min(MAX, value)));
    }

    public CharacterLevel {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException("Character level must be between " + MIN + " and " + MAX + ": " + value);
        }
    }

    public CharacterLevel next() {
        if (value >= MAX) return this;
        return new CharacterLevel(value + 1);
    }

    public boolean isMax() {
        return value >= MAX;
    }
}
