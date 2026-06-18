package io.github.bysenom.relicwrought.loot;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.ArpgModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ArpgMobDropHandler {
    private final ArpgDropGenerator dropGenerator;
    private final LootProfileResolver profileResolver;
    private final ArpgModConfig config;

    public ArpgMobDropHandler(
            ArpgDropGenerator dropGenerator,
            LootProfileResolver profileResolver,
            ArpgModConfig config
    ) {
        this.dropGenerator = dropGenerator;
        this.profileResolver = profileResolver;
        this.config = config;
    }

    public void onLivingDeath(LivingEntity entity) {
        if (entity.level().isClientSide()) return;
        if (!config.enableArpgMobDrops()) return;
        if (!(entity instanceof Enemy)) return;

        Player playerKiller = findPlayerKiller(entity);
        if (config.requirePlayerKill() && playerKiller == null) return;

        LootContextData context = buildContext(entity, playerKiller);

        try {
            LootProfileDefinition profile = profileResolver.resolveForContext(context);
            long seed = buildSeed(entity, profile);
            LootDropResult result = dropGenerator.generateDrops(profile, context, seed);

            if (result.didDrop() && !result.generatedItems().isEmpty()) {
                spawnDrops(result.generatedItems(), entity);
                if (config.enableDebugLootLogging()) {
                    String entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                    Relicwrought.LOGGER.info("Dropped {} ARPG items from {} (profile: {})",
                            result.successfulCount(), entityTypeId, profile.id());
                }
            }
        } catch (Exception e) {
            if (config.enableDebugLootLogging()) {
                Relicwrought.LOGGER.warn("ARPG drop handling failed for {}: {}", entity, e.getMessage());
            }
        }
    }

    private Player findPlayerKiller(LivingEntity entity) {
        if (entity.getLastAttacker() instanceof Player player) return player;
        if (entity.getKillCredit() instanceof Player player) return player;
        return null;
    }

    private LootContextData buildContext(LivingEntity entity, Player killer) {
        String dimension = entity.level().dimension().toString();
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        double maxHealth = entity.getMaxHealth();
        double armor = entity.getArmorValue();
        double attackDamage = 0.0;
        int lootingLevel = 0;

        if (killer != null) {
            lootingLevel = getLootingLevel(killer.getMainHandItem());
        }

        return new LootContextData(
                LootSourceType.NORMAL_MOB, dimension, entityId,
                entity.getType(), maxHealth, armor, attackDamage,
                killer != null, lootingLevel
        );
    }

    private int getLootingLevel(ItemStack stack) {
        return 0;
    }

    private long buildSeed(LivingEntity entity, LootProfileDefinition profile) {
        long entitySeed = entity.getUUID().getMostSignificantBits() ^ entity.getUUID().getLeastSignificantBits();
        long profileSeed = profile.id().toString().hashCode();
        long worldSeed = 0L;
        if (entity.level() instanceof ServerLevel serverLevel) {
            worldSeed = serverLevel.getSeed();
        }
        return (worldSeed * 31 + entitySeed) * 31 + profileSeed;
    }

    private void spawnDrops(List<ItemStack> items, LivingEntity entity) {
        ServerLevel level = (ServerLevel) entity.level();
        Vec3 pos = entity.position();
        var random = level.getRandom();
        for (ItemStack stack : items) {
            var entityItem = new net.minecraft.world.entity.item.ItemEntity(level, pos.x, pos.y, pos.z, stack);
            entityItem.setDeltaMovement(
                    (random.nextDouble() - 0.5) * 0.1,
                    random.nextDouble() * 0.1 + 0.1,
                    (random.nextDouble() - 0.5) * 0.1
            );
            level.addFreshEntity(entityItem);
        }
    }
}
