package io.github.bysenom.relicwrought.progression;

import io.github.bysenom.relicwrought.ArpgModConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public final class MobExperienceResolver {
    private static final double HEALTH_FACTOR = 2.0;
    private static final double ARMOR_FACTOR = 5.0;
    private static final int OVERWORLD_BONUS = 0;
    private static final int NETHER_BONUS = 50;
    private static final int END_BONUS = 150;
    private static final int BOSS_MULTIPLIER = 10;

    private final ArpgModConfig config;

    public MobExperienceResolver(ArpgModConfig config) {
        this.config = config;
    }

    public long resolveXp(LivingEntity entity, Player killer) {
        if (entity == null) return 0;
        if (!config.requirePlayerKillForXp() && killer == null) return 0;
        if (config.requirePlayerKillForXp() && killer == null) return 0;
        if (killer != null && killer == entity) return 0;
        if (entity instanceof Player) return 0;

        double maxHealth = entity.getMaxHealth();
        double armor = entity.getArmorValue();
        String dimension = entity.level().dimension().toString();

        double baseXp = maxHealth * HEALTH_FACTOR + armor * ARMOR_FACTOR;

        int dimensionBonus;
        if (dimension.contains("the_end")) {
            dimensionBonus = END_BONUS;
        } else if (dimension.contains("nether")) {
            dimensionBonus = NETHER_BONUS;
        } else {
            dimensionBonus = OVERWORLD_BONUS;
        }
        baseXp += dimensionBonus;

        if (!(entity instanceof Enemy)) {
            return 0;
        }

        String entityId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        boolean isBoss = entityId.contains("wither") || entityId.contains("ender_dragon") || entityId.contains("boss");
        if (isBoss) {
            baseXp *= BOSS_MULTIPLIER;
        }

        double multiplier = config.xpMultiplier();
        baseXp *= multiplier;

        if (baseXp < 0) baseXp = 0;
        if (baseXp > 1_000_000) baseXp = 1_000_000;

        long result = (long) Math.floor(baseXp);
        return Math.max(0, result);
    }
}
