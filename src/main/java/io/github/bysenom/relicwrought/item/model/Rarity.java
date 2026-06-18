package io.github.bysenom.relicwrought.item.model;

public enum Rarity {
    COMMON(0, 0, 0, 0),
    MAGIC(1, 2, 1, 1),
    RARE(3, 5, 3, 3),
    LEGENDARY(4, 6, 3, 3),
    UNIQUE(0, 0, 0, 0);

    private final int minAffixes;
    private final int maxAffixes;
    private final int maxPrefixes;
    private final int maxSuffixes;

    Rarity(int minAffixes, int maxAffixes, int maxPrefixes, int maxSuffixes) {
        this.minAffixes = minAffixes;
        this.maxAffixes = maxAffixes;
        this.maxPrefixes = maxPrefixes;
        this.maxSuffixes = maxSuffixes;
    }

    public int minAffixes() {
        return minAffixes;
    }

    public int maxAffixes() {
        return maxAffixes;
    }

    public int maxPrefixes() {
        return maxPrefixes;
    }

    public int maxSuffixes() {
        return maxSuffixes;
    }
}
