package io.github.bysenom.relicwrought.combat.damage;

import io.github.bysenom.relicwrought.item.scaling.NumberSafety;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public record DamageComponent(
        DamageType damageType,
        double baseValue,
        double finalValue,
        String source,
        Set<DamageTag> tags,
        boolean canCrit,
        boolean mitigatedByArmor,
        boolean mitigatedByResistance,
        int dataVersion
) {
    public DamageComponent {
        if (damageType == null) throw new IllegalArgumentException("damageType must not be null");
        NumberSafety.requireFiniteNonNegative(baseValue, "baseValue");
        NumberSafety.requireFiniteNonNegative(finalValue, "finalValue");
        if (source == null || source.isBlank()) throw new IllegalArgumentException("source must not be blank");
        tags = tags == null || tags.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(EnumSet.copyOf(tags));
        if (dataVersion <= 0) throw new IllegalArgumentException("dataVersion must be positive");
    }
}
