package io.github.bysenom.relicwrought.item.format;

import io.github.bysenom.relicwrought.item.model.AffixComponentRoll;
import io.github.bysenom.relicwrought.item.model.AffixOperation;
import io.github.bysenom.relicwrought.item.model.AffixRoll;
import io.github.bysenom.relicwrought.item.model.AffixScope;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;

import java.util.HashMap;
import java.util.Map;

public final class LocalAffixResolver {

    private LocalAffixResolver() {
    }

    public static LocalAffixModifiers resolve(ArpgItemData data) {
        Map<String, Double> flatModifiers = new HashMap<>();
        Map<String, Double> percentModifiers = new HashMap<>();

        for (AffixRoll roll : data.prefixes()) {
            collectLocalModifiers(roll, flatModifiers, percentModifiers);
        }
        for (AffixRoll roll : data.implicitAffixes()) {
            collectLocalModifiers(roll, flatModifiers, percentModifiers);
        }

        return new LocalAffixModifiers(Map.copyOf(flatModifiers), Map.copyOf(percentModifiers));
    }

    private static void collectLocalModifiers(
            AffixRoll roll,
            Map<String, Double> flatModifiers,
            Map<String, Double> percentModifiers
    ) {
        for (AffixComponentRoll comp : roll.componentRolls()) {
            if (comp.scope() != AffixScope.LOCAL) continue;
            switch (comp.operation()) {
                case ADD_FLAT, ADDITIVE ->
                        flatModifiers.merge(comp.stat(), comp.value(), Double::sum);
                case ADDITIVE_PERCENT, MULTIPLICATIVE_PERCENT ->
                        percentModifiers.merge(comp.stat(), comp.value(), Double::sum);
            }
        }
    }

    public record LocalAffixModifiers(Map<String, Double> flatModifiers, Map<String, Double> percentModifiers) {
        public LocalAffixModifiers {
            flatModifiers = Map.copyOf(flatModifiers);
            percentModifiers = Map.copyOf(percentModifiers);
        }
    }
}
