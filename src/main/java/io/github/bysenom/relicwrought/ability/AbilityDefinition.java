package io.github.bysenom.relicwrought.ability;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.KeyedDefinition;

import java.util.Set;

public record AbilityDefinition(
        DefinitionKey id,
        String translationKey,
        String descriptionTranslationKey,
        String iconPath,
        Set<String> allowedClasses,
        AbilityResourceType resourceType,
        double resourceCost,
        int cooldownTicks,
        AbilityTargetingType targetingType,
        AbilityEffectType effectType,
        double basePower,
        double scaling,
        int range,
        int radius,
        int dataVersion
) implements KeyedDefinition {
    public AbilityDefinition {
        if (cooldownTicks < 0) cooldownTicks = 0;
        if (resourceCost < 0) resourceCost = 0;
        if (basePower < 0) basePower = 0;
        if (range < 1) range = 1;
        if (radius < 0) radius = 0;
    }
}
