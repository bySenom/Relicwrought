package io.github.bysenom.relicwrought.item.generation;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.SplittableRandom;

public final class RaritySelector {
    private final DataRegistry<RarityDefinition> rarities;

    public RaritySelector(DataRegistry<RarityDefinition> rarities) {
        this.rarities = rarities;
    }

    public Optional<RarityDefinition> selectExplicit(DefinitionKey rarityId) {
        return rarities.get(rarityId);
    }

    public RarityDefinition selectWeighted(ItemLevel itemLevel, SplittableRandom random) {
        List<RarityDefinition> candidates = rarities.values().stream()
                .filter(RarityDefinition::isGeneratable)
                .filter(r -> r.isUnlockedAt(itemLevel.value()))
                .sorted(Comparator.comparing(r -> r.id().toString()))
                .toList();

        if (candidates.isEmpty()) {
            throw new IllegalStateException("No generatable rarity unlocked at item level " + itemLevel.value());
        }

        return selectWeighted(candidates, random);
    }

    public void validateRarityForLevel(RarityDefinition rarity, ItemLevel itemLevel) {
        if (!rarity.isUnlockedAt(itemLevel.value())) {
            throw new IllegalArgumentException("Rarity " + rarity.id() + " is not unlocked at item level " + itemLevel.value());
        }
    }

    private static RarityDefinition selectWeighted(List<RarityDefinition> candidates, SplittableRandom random) {
        if (!(candidates instanceof RandomAccess)) {
            candidates = new ArrayList<>(candidates);
        }
        long totalWeight = 0;
        for (RarityDefinition r : candidates) {
            totalWeight = Math.addExact(totalWeight, r.weight());
        }
        if (totalWeight <= 0) {
            throw new IllegalStateException("No generatable rarity candidates with positive weight");
        }
        long roll = random.nextLong(totalWeight);
        long cursor = 0;
        for (RarityDefinition r : candidates) {
            cursor += r.weight();
            if (roll < cursor) {
                return r;
            }
        }
        return candidates.getLast();
    }
}
