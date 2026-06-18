package io.github.bysenom.relicwrought.combat.damage;

import io.github.bysenom.relicwrought.combat.CombatErrorCode;

import java.util.Collections;
import java.util.List;

public record DamageCalculationResult(
        boolean success,
        DamageBundle rawDamage,
        DamageBundle finalDamage,
        double totalDamage,
        boolean isCritical,
        double criticalMultiplier,
        double armorMitigationPercent,
        double resistanceMitigationPercent,
        double flatReduction,
        double percentReduction,
        long seed,
        List<String> warnings,
        CombatErrorCode errorCode
) {
    public DamageCalculationResult {
        if (rawDamage == null) rawDamage = DamageBundle.empty();
        if (finalDamage == null) finalDamage = DamageBundle.empty();
        warnings = warnings == null ? Collections.emptyList() : List.copyOf(warnings);
        if (errorCode == null) errorCode = CombatErrorCode.NONE;
    }

    public static DamageCalculationResult failure(CombatErrorCode code, String warning) {
        return new DamageCalculationResult(
                false, DamageBundle.empty(), DamageBundle.empty(), 0.0,
                false, 1.0, 0.0, 0.0, 0.0, 0.0, 0L,
                List.of(warning), code
        );
    }
}
