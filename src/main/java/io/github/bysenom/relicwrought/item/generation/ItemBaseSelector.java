package io.github.bysenom.relicwrought.item.generation;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ItemCategory;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SplittableRandom;

public final class ItemBaseSelector {
    private final DataRegistry<ItemBaseDefinition> itemBases;

    public ItemBaseSelector(DataRegistry<ItemBaseDefinition> itemBases) {
        this.itemBases = itemBases;
    }

    public Optional<ItemBaseDefinition> selectExplicit(DefinitionKey baseId) {
        return itemBases.get(baseId);
    }

    public ItemBaseDefinition selectFromPool(
            Set<ItemCategory> allowedCategories,
            Set<String> requiredTags,
            Set<String> excludedTags,
            Set<DefinitionKey> allowedBaseIds,
            SplittableRandom random
    ) {
        List<ItemBaseDefinition> candidates = itemBases.values().stream()
                .filter(base -> matchesCategories(base, allowedCategories))
                .filter(base -> matchesTags(base, requiredTags))
                .filter(base -> matchesExcludedTags(base, excludedTags))
                .filter(base -> matchesAllowedIds(base, allowedBaseIds))
                .sorted(Comparator.comparing(b -> b.id().toString()))
                .toList();

        if (candidates.isEmpty()) {
            throw new IllegalStateException("No eligible item base found for the given filters");
        }

        return selectWeighted(candidates, random);
    }

    private boolean matchesCategories(ItemBaseDefinition base, Set<ItemCategory> allowedCategories) {
        if (allowedCategories == null || allowedCategories.isEmpty()) {
            return true;
        }
        return allowedCategories.contains(base.category());
    }

    private boolean matchesTags(ItemBaseDefinition base, Set<String> requiredTags) {
        if (requiredTags == null || requiredTags.isEmpty()) {
            return true;
        }
        return base.affixTags().containsAll(requiredTags);
    }

    private boolean matchesExcludedTags(ItemBaseDefinition base, Set<String> excludedTags) {
        if (excludedTags == null || excludedTags.isEmpty()) {
            return true;
        }
        return excludedTags.stream().noneMatch(base.affixTags()::contains);
    }

    private boolean matchesAllowedIds(ItemBaseDefinition base, Set<DefinitionKey> allowedBaseIds) {
        if (allowedBaseIds == null || allowedBaseIds.isEmpty()) {
            return true;
        }
        return allowedBaseIds.contains(base.id());
    }

    private static ItemBaseDefinition selectWeighted(List<ItemBaseDefinition> candidates, SplittableRandom random) {
        if (!(candidates instanceof RandomAccess)) {
            candidates = new ArrayList<>(candidates);
        }
        long totalWeight = candidates.size();
        if (totalWeight <= 0) {
            throw new IllegalStateException("No eligible item base candidates");
        }
        long roll = random.nextLong(totalWeight);
        return candidates.get((int) roll);
    }
}
