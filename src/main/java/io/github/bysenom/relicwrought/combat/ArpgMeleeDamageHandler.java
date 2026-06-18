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
    private final io.github.bysenom.relicwrought.combat.cooldown.WeaponCooldownResolver cooldownResolver;
    private final io.github.bysenom.relicwrought.combat.cooldown.WeaponAttackManager cooldownManager;

    public ArpgMeleeDamageHandler(ArpgModConfig config, ArpgItemStackService itemService, ProgressionManager progressionManager) {
        this.config = config;
        this.itemService = itemService;
        this.progressionManager = progressionManager;
        this.pipeline = new DamagePipeline(config);
        this.equippedResolver = new EquippedItemStatResolver(itemService);
        this.cooldownResolver = new io.github.bysenom.relicwrought.combat.cooldown.WeaponCooldownResolver(config, this::getAttackerStats);
        this.cooldownManager = new io.github.bysenom.relicwrought.combat.cooldown.WeaponAttackManager();
    }
    
    public io.github.bysenom.relicwrought.combat.cooldown.WeaponCooldownResolver getCooldownResolver() { return cooldownResolver; }
    public io.github.bysenom.relicwrought.combat.cooldown.WeaponAttackManager getCooldownManager() { return cooldownManager; }

    public CharacterCombatStats getAttackerStats(net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return CharacterCombatStats.empty();
        }
        io.github.bysenom.relicwrought.progression.CharacterProgression progression = progressionManager != null 
                ? progressionManager.getProgression(serverPlayer) : null;
        CharacterCombatStats baseStats = progression != null 
                ? io.github.bysenom.relicwrought.combat.stats.AttributeCombatResolver.resolve(progressionManager.getTotalAttributes(serverPlayer), config)
                : CharacterCombatStats.empty();
        CharacterCombatStats equippedStats = equippedResolver.collectGlobalStats(serverPlayer);
        return io.github.bysenom.relicwrought.combat.stats.CharacterCombatStatResolver.combine(baseStats, equippedStats);
    }

    private static final ThreadLocal<Boolean> APPLYING_ARPG_DAMAGE = ThreadLocal.withInitial(() -> false);

    public DamageCalculationResult calculateDamage(ServerPlayer attacker, Entity entity) {
        if (!config.enableArpgCombat()) {
            return null;
        }

        if (!(entity instanceof LivingEntity target)) {
            return null;
        }

        if (target instanceof Player && !config.enableArpgPvpDamage()) {
            return null;
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (!itemService.hasArpgData(weapon)) {
            return null; // fallback to vanilla
        }

        ArpgItemData weaponData = itemService.read(weapon).data().orElse(null);
        if (weaponData == null) {
            return null;
        }

        // 1. Resolve Attacker Stats
        CharacterProgression progression = progressionManager.getProgression(attacker);
        int level = progression != null ? progression.level().value() : 1;
        
        CharacterCombatStats attackerStats = getAttackerStats(attacker);

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
        if (itemBase == null) return null;
        
        ItemStatScaler scaler = new ItemStatScaler(ArpgItemSystems.bootstrapResult().scalingProfiles());
        ScalingContext ctx = ScalingContext.of(weaponData.itemLevel(), ItemQuality.of(weaponData.quality()));
        WeaponBaseStats wStats = scaler.scaleWeaponBaseStats(itemBase, ctx);
        
        LocalAffixResolver.LocalAffixModifiers localMods = LocalAffixResolver.resolve(weaponData);
        double localPercent = localMods.percentModifiers().getOrDefault("physical_damage", 0.0) / 100.0;
        
        double min = wStats.minimumDamage() * (1.0 + localPercent);
        double max = wStats.maximumDamage() * (1.0 + localPercent);
        double physRoll = min + (attacker.level().getRandom().nextDouble() * (max - min));
        
        DamageBundle weaponBundle = DamageBundle.single(DamageType.PHYSICAL, physRoll);
        
        boolean isBoss = target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon || target instanceof net.minecraft.world.entity.boss.wither.WitherBoss;
        boolean isElite = false; // Add elite logic if present
        
        DamageCalculationRequest request = new DamageCalculationRequest(
                level,
                attackerStats,
                targetStats,
                weaponBundle,
                EnumSet.of(DamageTag.MELEE, DamageTag.ATTACK, target instanceof Player ? DamageTag.PLAYER_SOURCE : DamageTag.MOB_SOURCE),
                attacker.getAttackStrengthScale(0.5f), // get current cooldown
                attacker.level().getRandom().nextLong(),
                isBoss,
                isElite
        );

        return pipeline.calculate(request);
    }
    
    public void register() {
        // Event logic removed in favor of Mixin inside PlayerAttackMixin
    }
    
    public ArpgModConfig getConfig() {
        return config;
    }
}
