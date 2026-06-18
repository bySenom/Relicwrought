package io.github.bysenom.relicwrought.item.format;

public final class ArpgStatDisplayResolver {
    private static final String STAT_KEY_PREFIX = "stat.relicwrought.";

    private ArpgStatDisplayResolver() {
    }

    public static String resolveTranslationKey(String statId) {
        return STAT_KEY_PREFIX + statId;
    }
}
