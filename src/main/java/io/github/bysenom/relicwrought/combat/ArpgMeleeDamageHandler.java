package io.github.bysenom.relicwrought.combat;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.combat.damage.*;
import io.github.bysenom.relicwrought.combat.stats.*;
import io.github.bysenom.relicwrought.combat.stats.*;
import io.github.bysenom.relicwrought.item.ArpgItemSystems;
import io.github.bysenom.relicwrought.item.format.LocalAffixResolver;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import io.github.bysenom.relicwrought.item.scaling.ItemQuality;
import io.github.bysenom.relicwrought.item.scaling.ItemStatScaler;
import io.github.bysenom.relicwrought.item.scaling.ScalingContext;
import io.github.bysenom.relicwrought.item.scaling.WeaponBaseStats;
import io.github.bysenom.relicwrought.progression.CharacterProgression;
import io.github.bysenom.relicwrought.progression.ProgressionManager;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

public final class ArpgMeleeDamageHandler {
    private final ArpgModConfig config;
    private final ArpgItemStackService itemService;
    private final ProgressionManager progressionManager;
    private final DamagePipeline pipeline;
    private final EquippedItemStatResolver equippedResolver;

    public ArpgMeleeDamageHandler(ArpgModConfig config, ArpgItemStackService itemService, ProgressionManager progressionManager) {
        this.config = config;
        this.itemService = itemService;
        this.progressionManager = progressionManager;
        this.pipeline = new DamagePipeline(config);
        this.equippedResolver = new EquippedItemStatResolver(itemService);
    }

    private static final ThreadLocal<Boolean> APPLYING_ARPG_DAMAGE = ThreadLocal.withInitial(() -> false);

    public void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (APPLYING_ARPG_DAMAGE.get()) {
                return InteractionResult.PASS;
            }

            if (!config.enableArpgCombat()) {
                return InteractionResult.PASS;
            }

            if (!(entity instanceof LivingEntity target)) {
                return InteractionResult.PASS;
            }

            if (target instanceof Player targetPlayer) {
                if (!config.enableArpgPvpDamage()) {
                    return InteractionResult.PASS;
                }
            }

            ItemStack weapon = player.getMainHandItem();
            if (!itemService.hasArpgData(weapon)) {
                return InteractionResult.PASS; // fallback to vanilla
            }

            ArpgItemData weaponData = itemService.read(weapon).data().orElse(null);
            if (weaponData == null) {
                return InteractionResult.PASS;
            }

            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            // 1. Resolve Attacker Stats
            CharacterProgression progression = progressionManager.getProgression(serverPlayer);
            int level = progression != null ? progression.level().value() : 1;
            
            CharacterCombatStats baseStats = progression != null 
                    ? AttributeCombatResolver.resolve(progression.allocatedAttributes(), config)
                    : CharacterCombatStats.empty();
                    
            CharacterCombatStats equippedStats = equippedResolver.collectGlobalStats(serverPlayer);
            CharacterCombatStats attackerStats = CharacterCombatStatResolver.combine(baseStats, equippedStats);

            // 2. Resolve Target Stats
            CharacterCombatStats targetStats;
            if (target instanceof ServerPlayer targetPlayer) {
                CharacterProgression targetProg = progressionManager.getProgression(targetPlayer);
                CharacterCombatStats targetBase = targetProg != null 
                        ? AttributeCombatResolver.resolve(targetProg.allocatedAttributes(), config)
                        : CharacterCombatStats.empty();
                CharacterCombatStats targetEquip = equippedResolver.collectGlobalStats(targetPlayer);
                targetStats = CharacterCombatStatResolver.combine(targetBase, targetEquip);
            } else if (target instanceof Mob mob) {
                targetStats = MobCombatStatResolver.resolve(mob, config);
            } else {
                targetStats = CharacterCombatStats.empty();
            }

            ItemBaseDefinition itemBase = ArpgItemSystems.bootstrapResult().itemBases().get(weaponData.itemBaseId()).orElse(null);
            if (itemBase == null) return InteractionResult.PASS;
            
            ItemStatScaler scaler = new ItemStatScaler(ArpgItemSystems.bootstrapResult().scalingProfiles());
            ScalingContext ctx = ScalingContext.of(weaponData.itemLevel(), ItemQuality.of(weaponData.quality()));
            WeaponBaseStats wStats = scaler.scaleWeaponBaseStats(itemBase, ctx);
            
            LocalAffixResolver.LocalAffixModifiers localMods = LocalAffixResolver.resolve(weaponData);
            double localPercent = localMods.percentModifiers().getOrDefault("physical_damage", 0.0) / 100.0;
            
            double min = wStats.minimumDamage() * (1.0 + localPercent);
            double max = wStats.maximumDamage() * (1.0 + localPercent);
            double physRoll = min + (world.getRandom().nextDouble() * (max - min));
            
            DamageBundle weaponBundle = DamageBundle.single(DamageType.PHYSICAL, physRoll);
            
            // 4. Calculate
            boolean isBoss = target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon || target instanceof net.minecraft.world.entity.boss.wither.WitherBoss;
            boolean isElite = false; // Add elite logic if present
            
            DamageCalculationRequest request = new DamageCalculationRequest(
                    level,
                    attackerStats,
                    targetStats,
                    weaponBundle,
                    EnumSet.of(DamageTag.MELEE, DamageTag.ATTACK, target instanceof Player ? DamageTag.PLAYER_SOURCE : DamageTag.MOB_SOURCE),
                    player.getAttackStrengthScale(0.5f), // get current cooldown
                    world.getRandom().nextLong(),
                    isBoss,
                    isElite
            );

            DamageCalculationResult result = pipeline.calculate(request);

            if (result.success()) {
                float damageAmount = (float) result.totalDamage();
                
                APPLYING_ARPG_DAMAGE.set(true);
                try {
                    DamageSource source = world.damageSources().playerAttack(player);
                    float healthBefore = target.getHealth();
                    target.hurt(source, damageAmount);
                    boolean hurtSuccess = target.getHealth() < healthBefore || !target.isAlive();
                    
                    if (hurtSuccess) {
                        // Restore Vanilla Knockback
                        int knockbackLevel = serverPlayer.isSprinting() ? 1 : 0;
                        if (knockbackLevel > 0) {
                            target.knockback(
                                    (double)((float)knockbackLevel * 0.5F), 
                                    (double)net.minecraft.util.Mth.sin(serverPlayer.getYRot() * ((float)Math.PI / 180F)), 
                                    (double)(-net.minecraft.util.Mth.cos(serverPlayer.getYRot() * ((float)Math.PI / 180F))),
                                    source,
                                    damageAmount
                            );
                        }

                        // Restore Fire Aspect is skipped for now due to API changes.
                        
                        // Restore Durability Loss
                        weapon.hurtAndBreak(1, serverPlayer, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                        
                        // Restore Exhaustion & Stats
                        serverPlayer.causeFoodExhaustion(0.1F);
                        serverPlayer.awardStat(net.minecraft.stats.Stats.DAMAGE_DEALT, Math.round(damageAmount * 10.0F));
                        
                        // Critical Particles
                        if (result.isCritical() && world instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, target.getX(), target.getY(0.5), target.getZ(), 10, 0.1, 0.1, 0.1, 0.2);
                        }

                        if (config.enableCombatDebugLogging()) {
                            io.github.bysenom.relicwrought.Relicwrought.LOGGER.info(
                                "ARPG Melee: Attacker={}, Target={}, RawDmg={}, Crit={}, CooldownScale={}, ArmorRed={}, FinalDmg={}",
                                serverPlayer.getScoreboardName(), target.getName().getString(),
                                result.rawDamage().getTotalDamage(), result.isCritical(), request.attackStrengthScale(),
                                result.armorMitigationPercent(), result.totalDamage()
                            );
                        }
                    }
                } finally {
                    APPLYING_ARPG_DAMAGE.set(false);
                }
                
                // Reset vanilla cooldown
                player.resetAttackStrengthTicker();
                
                // We handled it, cancel vanilla damage
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });
    }
}
