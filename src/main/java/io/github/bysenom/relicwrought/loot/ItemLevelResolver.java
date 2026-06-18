package io.github.bysenom.relicwrought.loot;

import io.github.bysenom.relicwrought.item.model.ItemLevel;

import java.util.SplittableRandom;

public final class ItemLevelResolver {

    private ItemLevelResolver() {
    }

    public static ItemLevel resolve(
            LootProfileDefinition profile,
            LootContextData context,
            EntityLootOverride override,
            SplittableRandom random
    ) {
        LootItemLevelConfig config = profile.itemLevel();
        int baseLevel;

        switch (config.type()) {
            case FIXED -> baseLevel = config.minimum();

            case RANDOM -> {
                int range = config.maximum() - config.minimum() + 1;
                baseLevel = config.minimum() + (range > 0 ? random.nextInt(range) : 0);
            }

            case SOURCE_SCALED -> baseLevel = scaleFromContext(context, config);
            default -> baseLevel = config.minimum();
        }

        int bonus = (override != null) ? override.itemLevelBonus() : 0;
        baseLevel += bonus;

        int variance = config.randomVariance();
        if (variance > 0) {
            int offset = random.nextInt(variance * 2 + 1) - variance;
            baseLevel += offset;
        }

        baseLevel = Math.max(config.minimum(), Math.min(config.maximum(), baseLevel));
        baseLevel = Math.max(1, Math.min(950, baseLevel));

        return ItemLevel.of(baseLevel);
    }

    private static int scaleFromContext(LootContextData context, LootItemLevelConfig config) {
        double strength = context.computeBaseStrength();

        int dimensionMin = dimensionMinimum(context.dimension());
        int dimensionMax = dimensionMaximum(context.dimension());

        int effectiveMin = Math.max(config.minimum(), dimensionMin);
        int effectiveMax = Math.min(config.maximum(), dimensionMax);

        if (effectiveMax <= effectiveMin) return effectiveMin;

        double t;
        if (strength <= 0) {
            t = 0.0;
        } else {
            t = Math.min(1.0, strength / 200.0);
        }
        return effectiveMin + (int) Math.round(t * (effectiveMax - effectiveMin));
    }

    static int dimensionMinimum(String dimension) {
        return switch (dimension) {
            case "minecraft:overworld" -> 1;
            case "minecraft:nether" -> 250;
            case "minecraft:the_end" -> 500;
            default -> 1;
        };
    }

    static int dimensionMaximum(String dimension) {
        return switch (dimension) {
            case "minecraft:overworld" -> 500;
            case "minecraft:nether" -> 650;
            case "minecraft:the_end" -> 750;
            default -> 500;
        };
    }
}
