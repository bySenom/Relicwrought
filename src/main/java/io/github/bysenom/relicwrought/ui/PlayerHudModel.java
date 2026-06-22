package io.github.bysenom.relicwrought.ui;

public record PlayerHudModel(
        boolean visible,
        double currentHealth,
        double maximumHealth,
        double healthFill,
        boolean vanillaHeartsHidden,
        boolean vanillaArmorHidden,
        CharacterResourceState resourceState,
        boolean resourceVisible,
        double resourceFill
) {
    public static PlayerHudModel hidden(boolean hideVanillaHearts, boolean hideVanillaArmor) {
        return new PlayerHudModel(
                false, 0.0, 0.0, 0.0, hideVanillaHearts, hideVanillaArmor,
                CharacterResourceState.empty(), false, 0.0
        );
    }

    public static PlayerHudModel resolve(
            boolean hudEnabled,
            boolean hideVanillaHearts,
            boolean hideVanillaArmor,
            boolean hasArpgLife,
            double arpgCurrentLife,
            double arpgMaximumLife,
            double vanillaCurrentHealth,
            double vanillaMaximumHealth,
            double resolvedMaximumLife,
            CharacterResourceState resourceState
    ) {
        if (!hudEnabled) {
            return hidden(false, false);
        }

        HealthValues health = resolveHealth(
                hasArpgLife, arpgCurrentLife, arpgMaximumLife,
                vanillaCurrentHealth, vanillaMaximumHealth, resolvedMaximumLife
        );
        CharacterResourceState resource = resourceState == null ? CharacterResourceState.empty() : resourceState.clamp();
        boolean resourceVisible = resource.type() != CharacterResourceType.NONE && resource.maximumValue() > 0.0;
        double resourceFill = resourceVisible ? clamp01(resource.currentValue() / resource.maximumValue()) : 0.0;

        return new PlayerHudModel(
                true, health.current(), health.maximum(), health.fill(),
                hideVanillaHearts, hideVanillaArmor,
                resource, resourceVisible, resourceFill
        );
    }

    private static HealthValues resolveHealth(
            boolean hasArpgLife,
            double arpgCurrentLife,
            double arpgMaximumLife,
            double vanillaCurrentHealth,
            double vanillaMaximumHealth,
            double resolvedMaximumLife
    ) {
        if (hasArpgLife && isFinitePositive(arpgMaximumLife) && isFinite(arpgCurrentLife)) {
            double maximum = arpgMaximumLife;
            double current = clamp(arpgCurrentLife, 0.0, maximum);
            return new HealthValues(current, maximum, clamp01(current / maximum));
        }
        if (isFinitePositive(resolvedMaximumLife) && isFinitePositive(vanillaMaximumHealth) && isFinite(vanillaCurrentHealth)) {
            double maximum = resolvedMaximumLife;
            double vanillaFill = clamp01(vanillaCurrentHealth / vanillaMaximumHealth);
            return new HealthValues(maximum * vanillaFill, maximum, vanillaFill);
        }
        double maximum = isFinitePositive(vanillaMaximumHealth) ? vanillaMaximumHealth : 1.0;
        double current = isFinite(vanillaCurrentHealth) ? clamp(vanillaCurrentHealth, 0.0, maximum) : 0.0;
        return new HealthValues(current, maximum, clamp01(current / maximum));
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private static boolean isFinitePositive(double value) {
        return isFinite(value) && value > 0.0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp01(double value) {
        if (!isFinite(value)) {
            return 0.0;
        }
        return clamp(value, 0.0, 1.0);
    }

    private record HealthValues(double current, double maximum, double fill) {
    }
}
