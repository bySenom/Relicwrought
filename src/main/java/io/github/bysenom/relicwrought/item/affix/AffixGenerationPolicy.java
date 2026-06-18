package io.github.bysenom.relicwrought.item.affix;

public record AffixGenerationPolicy(AffixGenerationMode mode, int tierWindow) {
    public static final int DEFAULT_TIER_WINDOW = 2;
    public static final AffixGenerationPolicy STRICT_DEFAULT = new AffixGenerationPolicy(
            AffixGenerationMode.STRICT,
            DEFAULT_TIER_WINDOW
    );
    public static final AffixGenerationPolicy BEST_EFFORT_DEFAULT = new AffixGenerationPolicy(
            AffixGenerationMode.BEST_EFFORT,
            DEFAULT_TIER_WINDOW
    );

    public AffixGenerationPolicy {
        if (tierWindow < 0 || tierWindow > 9) {
            throw new IllegalArgumentException("Tier window must be between 0 and 9: " + tierWindow);
        }
    }
}
