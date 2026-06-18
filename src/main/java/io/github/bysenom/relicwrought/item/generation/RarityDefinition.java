package io.github.bysenom.relicwrought.item.generation;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.KeyedDefinition;

import java.util.List;

public record RarityDefinition(
        DefinitionKey id,
        String displayName,
        int weight,
        int minimumItemLevel,
        int maximumItemLevel,
        List<AllowedAffixCount> allowedAffixCounts,
        int displayOrder,
        String color,
        int dataVersion
) implements KeyedDefinition {
    public RarityDefinition {
        if (weight < 0) {
            throw new IllegalArgumentException("Rarity weight must not be negative: " + weight);
        }
        if (minimumItemLevel < 1) {
            throw new IllegalArgumentException("Minimum item level must be at least 1: " + minimumItemLevel);
        }
        if (maximumItemLevel != 0 && maximumItemLevel < minimumItemLevel) {
            throw new IllegalArgumentException("Maximum item level must be >= minimum item level");
        }
        allowedAffixCounts = List.copyOf(allowedAffixCounts);
        if (!allowedAffixCounts.isEmpty()) {
            for (AllowedAffixCount aac : allowedAffixCounts) {
                if (aac.prefixes() < 0 || aac.prefixes() > 3) {
                    throw new IllegalArgumentException("Prefix count must be 0-3: " + aac.prefixes());
                }
                if (aac.suffixes() < 0 || aac.suffixes() > 3) {
                    throw new IllegalArgumentException("Suffix count must be 0-3: " + aac.suffixes());
                }
                if (aac.weight() <= 0) {
                    throw new IllegalArgumentException("Allowed affix count weight must be positive: " + aac.weight());
                }
            }
        }
        if (dataVersion <= 0) {
            throw new IllegalArgumentException("Rarity data version must be positive");
        }
    }

    public boolean isUnlockedAt(int itemLevel) {
        if (itemLevel < minimumItemLevel) {
            return false;
        }
        return maximumItemLevel == 0 || itemLevel <= maximumItemLevel;
    }

    public boolean isGeneratable() {
        return weight > 0;
    }

    public record AllowedAffixCount(int prefixes, int suffixes, int weight) {
        public AllowedAffixCount {
            if (prefixes < 0 || prefixes > 3) {
                throw new IllegalArgumentException("Prefix count must be 0-3: " + prefixes);
            }
            if (suffixes < 0 || suffixes > 3) {
                throw new IllegalArgumentException("Suffix count must be 0-3: " + suffixes);
            }
            if (weight <= 0) {
                throw new IllegalArgumentException("Weight must be positive: " + weight);
            }
        }
    }
}
