package io.github.bysenom.relicwrought.combat.damage;

import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStats;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public record DamageCalculationRequest(
        int attackerLevel,
        CharacterCombatStats attackerStats,
        CharacterCombatStats targetStats,
        DamageBundle weaponDamageBundle,
        Set<DamageTag> damageTags,
        double attackStrengthScale,
        long seed,
        boolean isBossTarget,
        boolean isEliteTarget
) {
    public DamageCalculationRequest {
        if (attackerStats == null) throw new IllegalArgumentException("attackerStats must not be null");
        if (targetStats == null) throw new IllegalArgumentException("targetStats must not be null");
        damageTags = damageTags == null || damageTags.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(EnumSet.copyOf(damageTags));
        if (attackStrengthScale < 0.0 || attackStrengthScale > 1.0) throw new IllegalArgumentException("attackStrengthScale must be between 0 and 1");
    }
}
