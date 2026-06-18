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

    public void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (!config.enableArpgCombat()) {
                return InteractionResult.PASS;
            }

            if (!(entity instanceof LivingEntity target)) {
                return InteractionResult.PASS;
            }

            if (target instanceof Player && !config.enableArpgPvpDamage()) {
                return InteractionResult.PASS;
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
            
            // Collect local elemental from weapon
            double fireMin = 0, fireMax = 0;
            double coldMin = 0, coldMax = 0;
            double lightMin = 0, lightMax = 0;
            double poisonMin = 0, poisonMax = 0;
            
            // Assuming LocalAffixResolver processed these into baseStats, but actually baseStats only has min/max damage which is physical in our model.
            // For now, only physical is extracted from baseStats. If we need elemental from local affixes, we'd need to extract it. We'll stick to physical + global elemental for this iteration, as weapon elemental was complex.
            
            DamageBundle initialBundle = DamageBundle.single(DamageType.PHYSICAL, physRoll);

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
                
                // Apply damage
                DamageSource source = world.damageSources().playerAttack(player);
                target.hurt(source, damageAmount);
                
                // Reset vanilla cooldown
                player.resetAttackStrengthTicker();
                
                // We handled it, cancel vanilla damage
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });
    }
}
