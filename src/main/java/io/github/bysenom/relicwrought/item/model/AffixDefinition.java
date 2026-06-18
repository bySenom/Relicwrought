package io.github.bysenom.relicwrought.item.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public record AffixDefinition(
        DefinitionKey id,
        String translationKey,
        AffixType type,
        Set<DefinitionKey> groups,
        Set<ItemCategory> validItemCategories,
        List<AffixComponentDefinition> components,
        int weight,
        List<AffixTierDefinition> tiers,
        Set<DefinitionKey> conflictGroups,
        Set<String> tags,
        Set<String> requiredTagsAny,
        Set<String> requiredTagsAll,
        Set<String> excludedTags,
        String calculation,
        int dataVersion
) implements KeyedDefinition {
    public AffixDefinition {
        if (translationKey == null || translationKey.isBlank()) {
            throw new IllegalArgumentException("Affix translation key must not be blank");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Affix weight must be positive");
        }
        groups = Set.copyOf(groups);
        if (groups.isEmpty()) {
            throw new IllegalArgumentException("Affix must define at least one group");
        }
        validItemCategories = Set.copyOf(validItemCategories);
        if (validItemCategories.isEmpty()) {
            throw new IllegalArgumentException("Affix must define at least one valid item category");
        }
        components = List.copyOf(components);
        if (components.isEmpty()) {
            throw new IllegalArgumentException("Affix must define at least one component");
        }
        tiers = List.copyOf(tiers);
        if (tiers.isEmpty()) {
            throw new IllegalArgumentException("Affix must define at least one tier");
        }
        for (AffixTierDefinition tier : tiers) {
            if (tier.values().size() != components.size()) {
                throw new IllegalArgumentException("Affix tier value count must match component count");
            }
        }
        conflictGroups = Set.copyOf(conflictGroups);
        tags = Set.copyOf(tags);
        requiredTagsAny = Set.copyOf(requiredTagsAny);
        requiredTagsAll = Set.copyOf(requiredTagsAll);
        excludedTags = Set.copyOf(excludedTags);
        calculation = calculation == null || calculation.isBlank() ? "additive" : calculation;
        if (dataVersion <= 0) {
            throw new IllegalArgumentException("Affix data version must be positive");
        }
    }

    public Optional<AffixTierDefinition> bestUnlockedTier(ItemLevel itemLevel) {
        return tiers.stream()
                .filter(tier -> tier.isUnlockedAt(itemLevel))
                .min((left, right) -> Integer.compare(left.tier().displayTier(), right.tier().displayTier()));
    }

    public boolean appliesTo(ItemCategory category) {
        return validItemCategories.contains(category);
    }

    public DefinitionKey group() {
        return groups.iterator().next();
    }

    public AffixScope scope() {
        return components.getFirst().scope();
    }
}
