package io.github.bysenom.relicwrought.item.generation;

import io.github.bysenom.relicwrought.item.affix.AffixSlotLimits;

import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;
import java.util.SplittableRandom;
import java.util.function.Predicate;

public final class SlotDistributionSelector {

    public AffixSlotLimits select(RarityDefinition rarity, SplittableRandom random) {
        return select(rarity, random, limits -> true);
    }

    public AffixSlotLimits select(RarityDefinition rarity, SplittableRandom random, Predicate<AffixSlotLimits> filter) {
        List<RarityDefinition.AllowedAffixCount> counts = rarity.allowedAffixCounts();
        if (counts == null || counts.isEmpty()) {
            return new AffixSlotLimits(0, 0);
        }
        List<RarityDefinition.AllowedAffixCount> filtered = counts.stream()
                .filter(c -> filter.test(new AffixSlotLimits(c.prefixes(), c.suffixes())))
                .toList();
        if (filtered.isEmpty()) {
            throw new IllegalStateException("No fulfillable slot distribution for rarity: " + rarity.id());
        }
        RarityDefinition.AllowedAffixCount selected = selectWeighted(filtered, random);
        return new AffixSlotLimits(selected.prefixes(), selected.suffixes());
    }

    private static RarityDefinition.AllowedAffixCount selectWeighted(
            List<RarityDefinition.AllowedAffixCount> candidates,
            SplittableRandom random
    ) {
        if (!(candidates instanceof RandomAccess)) {
            candidates = new ArrayList<>(candidates);
        }
        long totalWeight = 0;
        for (RarityDefinition.AllowedAffixCount c : candidates) {
            totalWeight = Math.addExact(totalWeight, c.weight());
        }
        if (totalWeight <= 0) {
            throw new IllegalStateException("No allowed affix count candidates with positive weight");
        }
        long roll = random.nextLong(totalWeight);
        long cursor = 0;
        for (RarityDefinition.AllowedAffixCount c : candidates) {
            cursor += c.weight();
            if (roll < cursor) {
                return c;
            }
        }
        return candidates.getLast();
    }
}
