package io.github.bysenom.relicwrought.ui;

import java.util.Locale;

public final class CharacterResourceResolver {
    public static final double DEFAULT_MAXIMUM_RESOURCE = 100.0;

    private CharacterResourceResolver() {
    }

    public static CharacterResourceType resolveType(String classId) {
        if (classId == null || classId.isBlank()) {
            return CharacterResourceType.NONE;
        }
        String normalized = classId.toLowerCase(Locale.ROOT);
        int namespaceSeparator = normalized.indexOf(':');
        if (namespaceSeparator >= 0 && namespaceSeparator + 1 < normalized.length()) {
            normalized = normalized.substring(namespaceSeparator + 1);
        }
        return switch (normalized) {
            case "warrior" -> CharacterResourceType.RAGE;
            case "ranger", "rogue" -> CharacterResourceType.ENERGY;
            case "arcanist" -> CharacterResourceType.MANA;
            default -> CharacterResourceType.NONE;
        };
    }

    public static CharacterResourceState resolveState(String classId, double currentValue) {
        CharacterResourceType type = resolveType(classId);
        if (type == CharacterResourceType.NONE) {
            return CharacterResourceState.empty();
        }
        double current = isFinitePositive(currentValue) ? currentValue : DEFAULT_MAXIMUM_RESOURCE;
        return new CharacterResourceState(type, current, DEFAULT_MAXIMUM_RESOURCE, CharacterResourceState.CURRENT_VERSION);
    }

    private static boolean isFinitePositive(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && value > 0.0;
    }
}
