package io.github.bysenom.relicwrought.loot;

import net.minecraft.world.entity.EntityType;

public record LootContextData(
        LootSourceType sourceType,
        String dimension,
        String entityId,
        EntityType<?> entityType,
        double maxHealth,
        double armor,
        double attackDamage,
        boolean playerKill,
        int lootingLevel
) {
    public double computeBaseStrength() {
        return (maxHealth * 0.4) + (armor * 0.3) + (attackDamage * 0.3);
    }
}
