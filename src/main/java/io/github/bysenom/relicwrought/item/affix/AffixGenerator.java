package io.github.bysenom.relicwrought.item.affix;

import io.github.bysenom.relicwrought.item.model.AffixComponentRoll;
import io.github.bysenom.relicwrought.item.model.AffixDefinition;
import io.github.bysenom.relicwrought.item.model.AffixGroupDefinition;
import io.github.bysenom.relicwrought.item.model.AffixRoll;
import io.github.bysenom.relicwrought.item.model.AffixTierDefinition;
import io.github.bysenom.relicwrought.item.model.AffixType;
import io.github.bysenom.relicwrought.item.model.AffixValueRange;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;
import io.github.bysenom.relicwrought.item.scaling.NumberSafety;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SplittableRandom;

public final class AffixGenerator {
    private final DataRegistry<AffixDefinition> affixes;
    private final DataRegistry<AffixGroupDefinition> affixGroups;

    public AffixGenerator(DataRegistry<AffixDefinition> affixes, DataRegistry<AffixGroupDefinition> affixGroups) {
        this.affixes = affixes;
        this.affixGroups = affixGroups;
    }

    public AffixGenerationResult generate(AffixGenerationRequest request) {
        SplittableRandom random = new SplittableRandom(request.seed());
        List<AffixRoll> prefixes = new ArrayList<>();
        List<AffixRoll> suffixes = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        List<AffixDefinition> selectedDefinitions = resolveExistingDefinitions(request.existingAffixes(), messages);
        boolean complete = true;

        complete &= fillSlots(request, AffixType.PREFIX, request.slotLimits().prefixes(), selectedDefinitions, prefixes, messages, random);
        complete &= fillSlots(request, AffixType.SUFFIX, request.slotLimits().suffixes(), selectedDefinitions, suffixes, messages, random);

        return new AffixGenerationResult(prefixes, suffixes, messages, complete && messages.isEmpty());
    }

    private boolean fillSlots(
            AffixGenerationRequest request,
            AffixType type,
            int slots,
            List<AffixDefinition> selectedDefinitions,
            List<AffixRoll> output,
            List<String> messages,
            SplittableRandom random
    ) {
        boolean complete = true;
        for (int slot = 0; slot < slots; slot++) {
            List<AffixDefinition> candidates = candidates(request, type, selectedDefinitions);
            if (candidates.isEmpty()) {
                String message = "no_affix_candidate:" + type.name().toLowerCase() + ":" + slot;
                messages.add(message);
                complete = false;
                if (request.policy().mode() == AffixGenerationMode.STRICT) {
                    return false;
                }
                continue;
            }

            AffixDefinition affix = selectWeighted(candidates, AffixDefinition::weight, random);
            Optional<AffixTierDefinition> tier = selectTier(affix, request, random);
            if (tier.isEmpty()) {
                messages.add("no_affix_tier:" + affix.id());
                complete = false;
                if (request.policy().mode() == AffixGenerationMode.STRICT) {
                    return false;
                }
                continue;
            }

            selectedDefinitions.add(affix);
            output.add(rollAffix(affix, tier.orElseThrow(), random));
        }
        return complete;
    }

    private List<AffixDefinition> candidates(
            AffixGenerationRequest request,
            AffixType type,
            List<AffixDefinition> selectedDefinitions
    ) {
        return affixes.values().stream()
                .filter(affix -> affix.type() == type)
                .filter(affix -> affix.appliesTo(request.itemBase().category()))
                .filter(affix -> tagsAllow(affix, request.itemBase().affixTags()))
                .filter(affix -> hasSelectableTier(affix, request))
                .filter(affix -> !alreadySelected(affix, selectedDefinitions))
                .filter(affix -> groupsAllow(affix, selectedDefinitions))
                .sorted(Comparator.comparing(affix -> affix.id().toString()))
                .toList();
    }

    private List<AffixDefinition> resolveExistingDefinitions(List<AffixRoll> existingAffixes, List<String> messages) {
        List<AffixDefinition> definitions = new ArrayList<>();
        for (AffixRoll roll : existingAffixes) {
            affixes.get(roll.affixId()).ifPresentOrElse(definitions::add, () -> messages.add("missing_existing_affix:" + roll.affixId()));
        }
        definitions.sort(Comparator.comparing(affix -> affix.id().toString()));
        return definitions;
    }

    private static boolean tagsAllow(AffixDefinition affix, Set<String> itemTags) {
        if (!itemTags.containsAll(affix.requiredTagsAll())) {
            return false;
        }
        if (!affix.requiredTagsAny().isEmpty() && affix.requiredTagsAny().stream().noneMatch(itemTags::contains)) {
            return false;
        }
        return affix.excludedTags().stream().noneMatch(itemTags::contains);
    }

    private boolean groupsAllow(AffixDefinition candidate, List<AffixDefinition> selectedDefinitions) {
        Map<DefinitionKey, Integer> selectedGroupCounts = new HashMap<>();
        Set<DefinitionKey> selectedGroups = new HashSet<>();
        Set<DefinitionKey> selectedConflictGroups = new HashSet<>();

        for (AffixDefinition selected : selectedDefinitions) {
            for (DefinitionKey group : selected.groups()) {
                selectedGroups.add(group);
                selectedGroupCounts.merge(group, 1, Integer::sum);
                affixGroups.get(group).map(AffixGroupDefinition::conflicts).ifPresent(selectedConflictGroups::addAll);
            }
            selectedConflictGroups.addAll(selected.conflictGroups());
        }

        for (DefinitionKey group : candidate.groups()) {
            int maxPerItem = affixGroups.get(group).map(AffixGroupDefinition::maxPerItem).orElse(1);
            if (selectedGroupCounts.getOrDefault(group, 0) >= maxPerItem) {
                return false;
            }
            if (selectedConflictGroups.contains(group)) {
                return false;
            }
            Set<DefinitionKey> candidateGroupConflicts = affixGroups.get(group)
                    .map(AffixGroupDefinition::conflicts)
                    .orElse(Set.of());
            if (candidateGroupConflicts.stream().anyMatch(selectedGroups::contains)) {
                return false;
            }
        }

        return candidate.conflictGroups().stream().noneMatch(selectedGroups::contains);
    }

    private static boolean alreadySelected(AffixDefinition affix, List<AffixDefinition> selectedDefinitions) {
        return selectedDefinitions.stream().anyMatch(selected -> selected.id().equals(affix.id()));
    }

    private boolean hasSelectableTier(AffixDefinition affix, AffixGenerationRequest request) {
        return selectTierCandidates(affix, request).size() > 0;
    }

    private Optional<AffixTierDefinition> selectTier(
            AffixDefinition affix,
            AffixGenerationRequest request,
            SplittableRandom random
    ) {
        List<AffixTierDefinition> candidates = selectTierCandidates(affix, request);
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(selectWeighted(candidates, AffixTierDefinition::weight, random));
    }

    private List<AffixTierDefinition> selectTierCandidates(AffixDefinition affix, AffixGenerationRequest request) {
        int bestDisplayTier = affix.tiers().stream()
                .filter(tier -> tier.isUnlockedAt(request.itemLevel()))
                .mapToInt(tier -> tier.tier().displayTier())
                .min()
                .orElse(Integer.MAX_VALUE);
        if (bestDisplayTier == Integer.MAX_VALUE) {
            return List.of();
        }
        int worstAllowedDisplayTier = bestDisplayTier + request.policy().tierWindow();
        return affix.tiers().stream()
                .filter(tier -> tier.isUnlockedAt(request.itemLevel()))
                .filter(tier -> tier.tier().displayTier() >= bestDisplayTier)
                .filter(tier -> tier.tier().displayTier() <= worstAllowedDisplayTier)
                .sorted(Comparator.comparingInt(tier -> tier.tier().displayTier()))
                .toList();
    }

    private static AffixRoll rollAffix(AffixDefinition affix, AffixTierDefinition tier, SplittableRandom random) {
        List<AffixComponentRoll> componentRolls = new ArrayList<>();
        double normalizedTotal = 0.0D;
        double firstValue = 0.0D;
        for (int index = 0; index < affix.components().size(); index++) {
            var component = affix.components().get(index);
            AffixValueRange range = tier.values().get(index);
            double normalizedRoll = random.nextDouble();
            double value = NumberSafety.requireFiniteNonNegative(
                    tier.rounding().apply(range.valueForRoll(normalizedRoll)),
                    "Affix component value"
            );
            if (index == 0) {
                firstValue = value;
            }
            normalizedTotal += normalizedRoll;
            componentRolls.add(new AffixComponentRoll(
                    component.stat(),
                    component.scope(),
                    component.operation(),
                    normalizedRoll,
                    value
            ));
        }
        return new AffixRoll(
                affix.id(),
                tier.tier(),
                normalizedTotal / affix.components().size(),
                firstValue,
                componentRolls,
                affix.dataVersion()
        );
    }

    public int countAvailableAffixes(AffixType type, ItemBaseDefinition base, ItemLevel itemLevel) {
        return (int) affixes.values().stream()
                .filter(affix -> affix.type() == type)
                .filter(affix -> affix.appliesTo(base.category()))
                .filter(affix -> tagsAllow(affix, base.affixTags()))
                .filter(affix -> affix.tiers().stream().anyMatch(tier -> tier.isUnlockedAt(itemLevel)))
                .count();
    }

    private static <T> T selectWeighted(List<T> sortedCandidates, WeightGetter<T> weightGetter, SplittableRandom random) {
        if (!(sortedCandidates instanceof RandomAccess)) {
            sortedCandidates = new ArrayList<>(sortedCandidates);
        }
        long totalWeight = 0L;
        for (T candidate : sortedCandidates) {
            int weight = weightGetter.weight(candidate);
            if (weight <= 0) {
                throw new IllegalArgumentException("Weighted candidate must have positive weight");
            }
            totalWeight = Math.addExact(totalWeight, weight);
        }
        if (totalWeight <= 0L) {
            throw new IllegalArgumentException("Weighted candidate list is empty");
        }
        long roll = random.nextLong(totalWeight);
        long cursor = 0L;
        for (T candidate : sortedCandidates) {
            cursor += weightGetter.weight(candidate);
            if (roll < cursor) {
                return candidate;
            }
        }
        return sortedCandidates.getLast();
    }

    @FunctionalInterface
    private interface WeightGetter<T> {
        int weight(T value);
    }
}
