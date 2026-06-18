package io.github.bysenom.relicwrought.item.generation;

import io.github.bysenom.relicwrought.item.model.ArpgItemData;

import java.util.SplittableRandom;

public final class QualityGenerator {
    private static final int[] QUALITY_BOUNDARIES = {0, 6, 11, 16, 20};
    private static final int[] QUALITY_WEIGHTS = {500, 300, 200, 100, 30};

    public int generateQuality(SplittableRandom random) {
        long totalWeight = 0;
        for (int w : QUALITY_WEIGHTS) {
            totalWeight += w;
        }
        long roll = random.nextLong(totalWeight);
        long cursor = 0;
        for (int i = 0; i < QUALITY_WEIGHTS.length; i++) {
            cursor += QUALITY_WEIGHTS[i];
            if (roll < cursor) {
                int low = QUALITY_BOUNDARIES[i];
                int high = (i + 1 < QUALITY_BOUNDARIES.length) ? QUALITY_BOUNDARIES[i + 1] - 1 : QUALITY_BOUNDARIES[i];
                if (low == high) {
                    return low;
                }
                return low + random.nextInt(high - low + 1);
            }
        }
        return 0;
    }

    public int resolveQuality(Integer explicitQuality, SplittableRandom random) {
        if (explicitQuality != null) {
            if (explicitQuality < ArpgItemData.MIN_QUALITY || explicitQuality > ArpgItemData.MAX_QUALITY) {
                throw new IllegalArgumentException("Quality must be between " + ArpgItemData.MIN_QUALITY + " and " + ArpgItemData.MAX_QUALITY + ": " + explicitQuality);
            }
            return explicitQuality;
        }
        return generateQuality(random);
    }
}
