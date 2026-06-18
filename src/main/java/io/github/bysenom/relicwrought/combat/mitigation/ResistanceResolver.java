package io.github.bysenom.relicwrought.combat.mitigation;

import io.github.bysenom.relicwrought.ArpgModConfig;

public final class ResistanceResolver {

    private ResistanceResolver() {
    }

    public static double clampResistance(double resistance, ArpgModConfig config) {
        double maxResist = config.maximumElementalResistance();
        double minResist = config.minimumElementalResistance();
        
        if (resistance > maxResist) return maxResist;
        if (resistance < minResist) return minResist;
        return resistance;
    }
}
