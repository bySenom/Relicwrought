package io.github.bysenom.relicwrought.combat.damage;

import io.github.bysenom.relicwrought.item.scaling.NumberSafety;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record DamageBundle(Map<DamageType, Double> components) {
    public DamageBundle {
        if (components == null) throw new IllegalArgumentException("components must not be null");
        Map<DamageType, Double> copy = new EnumMap<>(DamageType.class);
        for (Map.Entry<DamageType, Double> entry : components.entrySet()) {
            if (entry.getKey() == null) throw new IllegalArgumentException("DamageType must not be null");
            double val = entry.getValue();
            NumberSafety.requireFiniteNonNegative(val, "Damage bundle component value");
            if (val > 0.0) {
                copy.put(entry.getKey(), val);
            }
        }
        components = Collections.unmodifiableMap(copy);
    }

    public static DamageBundle empty() {
        return new DamageBundle(Collections.emptyMap());
    }

    public static DamageBundle single(DamageType type, double value) {
        return new DamageBundle(Map.of(type, value));
    }

    public double getTotalDamage() {
        double sum = 0.0;
        for (double val : components.values()) {
            sum += val;
        }
        return NumberSafety.requireFiniteNonNegative(sum, "Damage bundle total sum");
    }

    public double getDamage(DamageType type) {
        return components.getOrDefault(type, 0.0);
    }

    public boolean isEmpty() {
        return components.isEmpty();
    }
}
