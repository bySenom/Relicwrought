package io.github.bysenom.relicwrought.item.model;

public enum AffixTier {
    T10(10, 1),
    T9(9, 100),
    T8(8, 200),
    T7(7, 300),
    T6(6, 400),
    T5(5, 500),
    T4(4, 600),
    T3(3, 700),
    T2(2, 800),
    T1(1, 900);

    private final int displayTier;
    private final int minimumItemLevel;

    AffixTier(int displayTier, int minimumItemLevel) {
        this.displayTier = displayTier;
        this.minimumItemLevel = minimumItemLevel;
    }

    public int displayTier() {
        return displayTier;
    }

    public int minimumItemLevel() {
        return minimumItemLevel;
    }

    public boolean isUnlockedAt(ItemLevel itemLevel) {
        return itemLevel.value() >= minimumItemLevel;
    }
}
