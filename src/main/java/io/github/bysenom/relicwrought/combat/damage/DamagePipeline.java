package io.github.bysenom.relicwrought.combat.damage;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.combat.CombatErrorCode;
import io.github.bysenom.relicwrought.combat.mitigation.ArmorMitigationResolver;
import io.github.bysenom.relicwrought.combat.mitigation.ResistanceResolver;
import io.github.bysenom.relicwrought.combat.stats.CriticalStrikeResolver;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.scaling.NumberSafety;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class DamagePipeline {
    private final ArpgModConfig config;

    public DamagePipeline(ArpgModConfig config) {
        this.config = config;
    }

    public DamageCalculationResult calculate(DamageCalculationRequest request) {
        if (!config.enableArpgCombat()) {
            return DamageCalculationResult.failure(CombatErrorCode.COMBAT_DISABLED, "ARPG Combat is disabled.");
        }

        List<String> warnings = new ArrayList<>();
        
        // 1. Base Weapon Damage
        // For physical, scaledDamageMin/Max already contains local percent and flat modifiers
        // For elemental, we need to extract from local flat affixes on the weapon
        Map<DamageType, Double> rawDamageMap = new EnumMap<>(DamageType.class);
        
        // Populate raw damage from request weapon bundle
        for (Map.Entry<DamageType, Double> entry : request.weaponDamageBundle().components().entrySet()) {
            rawDamageMap.put(entry.getKey(), entry.getValue());
        }

        // 2. Global Flat additions
        rawDamageMap.merge(DamageType.PHYSICAL, request.attackerStats().flatPhysicalDamage(), Double::sum);
        rawDamageMap.merge(DamageType.FIRE, request.attackerStats().fireDamage(), Double::sum);
        rawDamageMap.merge(DamageType.COLD, request.attackerStats().coldDamage(), Double::sum);
        rawDamageMap.merge(DamageType.LIGHTNING, request.attackerStats().lightningDamage(), Double::sum);
        rawDamageMap.merge(DamageType.POISON, request.attackerStats().poisonDamage(), Double::sum);

        DamageBundle rawBundle = new DamageBundle(rawDamageMap);

        // 3. Additive Multipliers
        double physAdditive = 1.0 + request.attackerStats().physicalDamagePercent();
        double elemAdditive = 1.0 + request.attackerStats().elementalDamagePercent();

        Map<DamageType, Double> afterAdditiveMap = new EnumMap<>(DamageType.class);
        for (Map.Entry<DamageType, Double> entry : rawDamageMap.entrySet()) {
            double base = entry.getValue();
            if (base <= 0) continue;
            
            double multiplier = entry.getKey().isElemental() ? elemAdditive : physAdditive;
            afterAdditiveMap.put(entry.getKey(), Math.max(0, base * multiplier));
        }

        // 4. Critical Strike
        double baseCritChance = request.attackerStats().criticalStrikeChance(); // Already contains config.baseCriticalChance() if injected via AttributeCombatResolver, but wait, AttributeCombatResolver starts at 0. Let's add base.
        double totalCritChance = config.baseCriticalChance() + baseCritChance;
        
        boolean isCrit = CriticalStrikeResolver.isCritical(totalCritChance, request.seed(), config);
        double critMultiplier = isCrit ? Math.max(1.0, request.attackerStats().criticalStrikeMultiplier()) : 1.0;

        // 5. Conditional (Elite/Boss)
        double conditionalMultiplier = 1.0;
        if (request.isBossTarget()) conditionalMultiplier += request.attackerStats().bossDamageBonus();
        if (request.isEliteTarget()) conditionalMultiplier += request.attackerStats().eliteDamageBonus();

        // Combine Multipliers
        double globalMultiplier = critMultiplier * conditionalMultiplier;

        Map<DamageType, Double> afterMultiplicativeMap = new EnumMap<>(DamageType.class);
        for (Map.Entry<DamageType, Double> entry : afterAdditiveMap.entrySet()) {
            afterMultiplicativeMap.put(entry.getKey(), entry.getValue() * globalMultiplier);
        }

        // 6. Attack Cooldown
        double attackScale = Math.max(0.0, Math.min(1.0, request.attackStrengthScale()));
        if (config.useVanillaAttackCooldown()) {
            for (Map.Entry<DamageType, Double> entry : afterMultiplicativeMap.entrySet()) {
                afterMultiplicativeMap.put(entry.getKey(), entry.getValue() * attackScale);
            }
        }

        // 7. Defenses (Mitigation)
        double armorRed = ArmorMitigationResolver.calculatePhysicalReduction(request.targetStats().armor(), request.attackerLevel(), config);
        
        double fireRes = ResistanceResolver.clampResistance(request.targetStats().fireResistance(), config);
        double coldRes = ResistanceResolver.clampResistance(request.targetStats().coldResistance(), config);
        double lightningRes = ResistanceResolver.clampResistance(request.targetStats().lightningResistance(), config);
        double poisonRes = ResistanceResolver.clampResistance(request.targetStats().poisonResistance(), config);
        // Fallback for arcane/holy etc. could be 0, but for now we only support main 4
        
        Map<DamageType, Double> finalDamageMap = new EnumMap<>(DamageType.class);
        for (Map.Entry<DamageType, Double> entry : afterMultiplicativeMap.entrySet()) {
            DamageType type = entry.getKey();
            double dmg = entry.getValue();
            
            double mitigated = dmg;
            if (type == DamageType.PHYSICAL) {
                mitigated = dmg * (1.0 - armorRed);
            } else if (type == DamageType.FIRE) {
                mitigated = dmg * (1.0 - fireRes);
            } else if (type == DamageType.COLD) {
                mitigated = dmg * (1.0 - coldRes);
            } else if (type == DamageType.LIGHTNING) {
                mitigated = dmg * (1.0 - lightningRes);
            } else if (type == DamageType.POISON) {
                mitigated = dmg * (1.0 - poisonRes);
            }
            // For other elementals, resistance is 0 implicitly
            
            finalDamageMap.put(type, mitigated);
        }

        // 8. Flat & Percent Total Reduction
        double flatReduction = request.targetStats().flatDamageReduction();
        double percentReduction = Math.min(1.0, Math.max(0.0, request.targetStats().percentDamageReduction()));

        Map<DamageType, Double> trulyFinalMap = new EnumMap<>(DamageType.class);
        for (Map.Entry<DamageType, Double> entry : finalDamageMap.entrySet()) {
            double dmg = entry.getValue();
            
            // Apportion flat reduction roughly by percentage of total damage, or apply greedily?
            // Greedily is easier. We will just subtract flat reduction sequentially for now.
            if (flatReduction > 0) {
                if (dmg >= flatReduction) {
                    dmg -= flatReduction;
                    flatReduction = 0;
                } else {
                    flatReduction -= dmg;
                    dmg = 0;
                }
            }
            
            dmg = dmg * (1.0 - percentReduction);
            
            try {
                dmg = NumberSafety.requireFiniteNonNegative(dmg, "final mitigated damage");
            } catch (Exception e) {
                warnings.add(e.getMessage());
                dmg = NumberSafety.MAX_SCALED_VALUE; // Cap it
            }
            
            trulyFinalMap.put(entry.getKey(), dmg);
        }

        DamageBundle finalBundle = new DamageBundle(trulyFinalMap);
        
        return new DamageCalculationResult(
                true,
                rawBundle,
                finalBundle,
                finalBundle.getTotalDamage(),
                isCrit,
                critMultiplier,
                armorRed,
                0, // resistance mitigation percent varies by type, hard to summarize in one number
                request.targetStats().flatDamageReduction(),
                percentReduction,
                request.seed(),
                warnings,
                CombatErrorCode.NONE
        );
    }
}
