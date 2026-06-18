package io.github.bysenom.relicwrought.combat.stats;

import io.github.bysenom.relicwrought.ArpgModConfig;

import java.util.SplittableRandom;

public final class CriticalStrikeResolver {

    private CriticalStrikeResolver() {
    }

    public static boolean isCritical(double chance, long seed, ArpgModConfig config) {
        double maxChance = config.maximumCriticalChance();
        double effectiveChance = Math.min(Math.max(chance, 0.0), maxChance);
        
        if (effectiveChance <= 0.0) return false;
        if (effectiveChance >= 1.0) return true;

        SplittableRandom random = new SplittableRandom(seed);
        return random.nextDouble() < effectiveChance;
    }
}
