package io.github.bysenom.relicwrought.combat.mitigation;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.item.scaling.NumberSafety;

public final class ArmorMitigationResolver {

    private ArmorMitigationResolver() {
    }

    public static double calculatePhysicalReduction(double armor, int attackerLevel, ArpgModConfig config) {
        NumberSafety.requireFiniteNonNegative(armor, "armor");
        if (attackerLevel < 1) attackerLevel = 1;
        
        if (armor <= 0.0) {
            return 0.0;
        }

        double constant = config.armorConstant();
        if (constant <= 0.0) constant = 100.0; // Fail-safe

        double maxReduction = config.maximumPhysicalReduction();
        
        double reduction = armor / (armor + constant * attackerLevel);
        
        return Math.min(reduction, maxReduction);
    }
}
