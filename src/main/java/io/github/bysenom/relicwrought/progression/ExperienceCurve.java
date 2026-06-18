package io.github.bysenom.relicwrought.progression;

public final class ExperienceCurve {
    private final double baseXp;
    private final double exponent;

    public ExperienceCurve(double baseXp, double exponent) {
        if (baseXp <= 0) throw new IllegalArgumentException("baseXp must be positive: " + baseXp);
        if (exponent <= 0) throw new IllegalArgumentException("exponent must be positive: " + exponent);
        this.baseXp = baseXp;
        this.exponent = exponent;
    }

    public long xpForLevel(int level) {
        if (level < CharacterLevel.MIN) return 0;
        if (level > CharacterLevel.MAX) return Long.MAX_VALUE;
        if (level == CharacterLevel.MIN) return 0;
        double result = baseXp * Math.pow(level, exponent);
        if (result > Long.MAX_VALUE) return Long.MAX_VALUE;
        if (result < 0) return Long.MAX_VALUE;
        return (long) Math.ceil(result);
    }

    public long xpFromPreviousToCurrent(int level) {
        return xpForLevel(level) - xpForLevel(level - 1);
    }

    public long totalXpForLevel(int level) {
        return xpForLevel(level);
    }

    public int maxLevelForXp(long totalXp) {
        if (totalXp <= 0) return CharacterLevel.MIN;
        for (int level = CharacterLevel.MIN; level <= CharacterLevel.MAX; level++) {
            if (totalXpForLevel(level + 1 > CharacterLevel.MAX ? CharacterLevel.MAX : level + 1) > totalXp) {
                return level;
            }
        }
        return CharacterLevel.MAX;
    }

    public long xpToNextLevel(int currentLevel, long currentXp) {
        long required = xpForLevel(currentLevel + 1);
        long have = xpForLevel(currentLevel) + currentXp;
        return required - have;
    }

    public double baseXp() { return baseXp; }
    public double exponent() { return exponent; }
}
