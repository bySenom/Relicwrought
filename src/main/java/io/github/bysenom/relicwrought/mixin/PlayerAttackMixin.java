package io.github.bysenom.relicwrought.mixin;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.combat.damage.DamageCalculationResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {

    private static final ThreadLocal<Entity> CURRENT_TARGET = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> APPLYING_ARPG_DAMAGE = ThreadLocal.withInitial(() -> false);

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttackStart(Entity target, CallbackInfo ci) {
        CURRENT_TARGET.set(target);
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void onAttackEnd(Entity target, CallbackInfo ci) {
        CURRENT_TARGET.remove();
    }

    @Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
    private void hideVanillaAttackIndicator(float adjustTicks, CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        io.github.bysenom.relicwrought.combat.ArpgMeleeDamageHandler handler = Relicwrought.getMeleeDamageHandler();
        if (handler != null && handler.getConfig().enableWeaponCooldownGating()) {
            if (player.level().isClientSide()) {
                if (handler.getConfig().hideVanillaAttackIndicatorForArpgWeapons()) {
                    if (io.github.bysenom.relicwrought.item.ArpgItemSystems.itemStackService().hasArpgData(player.getMainHandItem())) {
                        cir.setReturnValue(1.0f); // Forces vanilla indicator to hide
                    }
                }
            } else {
                if (io.github.bysenom.relicwrought.item.ArpgItemSystems.itemStackService().hasArpgData(player.getMainHandItem())) {
                    cir.setReturnValue(1.0f); // Ensures server damage calculations don't use vanilla cooldown
                }
            }
        }
    }

    private static final java.util.concurrent.atomic.AtomicBoolean MISSING_HANDLER_WARNING_LOGGED = new java.util.concurrent.atomic.AtomicBoolean();

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean redirectAttackHurt(Entity hurtTarget, DamageSource source, float vanillaDamage) {
        Player player = (Player) (Object) this;
        Entity mainTarget = CURRENT_TARGET.get();

        if (!(player instanceof ServerPlayer serverPlayer) || player.isSpectator()) {
            return hurtTarget.hurtOrSimulate(source, vanillaDamage);
        }

        if (hurtTarget.isSpectator()) {
            return hurtTarget.hurtOrSimulate(source, vanillaDamage);
        }

        if (APPLYING_ARPG_DAMAGE.get()) {
            return hurtTarget.hurtOrSimulate(source, vanillaDamage);
        }

        io.github.bysenom.relicwrought.combat.ArpgMeleeDamageHandler handler = Relicwrought.getMeleeDamageHandler();
        
        if (handler == null) {
            if (MISSING_HANDLER_WARNING_LOGGED.compareAndSet(false, true)) {
                Relicwrought.LOGGER.error("Melee damage handler is unavailable; falling back to vanilla combat");
            }
            return hurtTarget.hurtOrSimulate(source, vanillaDamage);
        }

        if (!handler.getConfig().enableArpgCombat()) {
            return hurtTarget.hurtOrSimulate(source, vanillaDamage);
        }

        io.github.bysenom.relicwrought.item.model.ArpgItemData weaponData = null;
        if (io.github.bysenom.relicwrought.item.ArpgItemSystems.itemStackService().hasArpgData(serverPlayer.getMainHandItem())) {
            weaponData = io.github.bysenom.relicwrought.item.ArpgItemSystems.itemStackService().read(serverPlayer.getMainHandItem()).data().orElse(null);
        }
        boolean isArpgWeapon = weaponData != null;

        if (isArpgWeapon && handler.getConfig().enableWeaponCooldownGating()) {
            long currentTick = serverPlayer.level().getGameTime();
            io.github.bysenom.relicwrought.combat.cooldown.WeaponAttackState state = handler.getCooldownManager().getState(serverPlayer);
            if (!state.isReady(currentTick)) {
                return false;
            }
        }

        // Only calculate if we are hitting the main target to avoid redundant calculations
        DamageCalculationResult result = null;
        if (hurtTarget == mainTarget) {
            result = handler.calculateDamage(serverPlayer, mainTarget);
        } else {
            // For sweeping targets, we calculate against them individually
            result = handler.calculateDamage(serverPlayer, hurtTarget);
        }

        if (result != null) {
            if (hurtTarget != mainTarget) {
                // Sweeping is disabled for ARPG weapons
                return false; 
            }
            
            if (!result.success()) {
                return false;
            }

            float arpgDamage = (float) result.totalDamage();
            
            APPLYING_ARPG_DAMAGE.set(true);
            try {
                boolean success = hurtTarget.hurtOrSimulate(source, arpgDamage);
                
                if (success && result.isCritical() && serverPlayer.level() != null && serverPlayer.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.CRIT,
                            hurtTarget.getX(), hurtTarget.getY(0.5), hurtTarget.getZ(),
                            10, 0.1, 0.1, 0.1, 0.2
                    );
                }
                
                if (success && handler.getConfig().enableCombatDebugLogging()) {
                    Relicwrought.LOGGER.info(
                        "ARPG Melee (Mixin): Attacker={}, Target={}, RawDmg={}, Crit={}, FinalDmg={}",
                        serverPlayer.getScoreboardName(), hurtTarget.getName().getString(),
                        result.rawDamage().getTotalDamage(), result.isCritical(), result.totalDamage()
                    );
                }
                
                if (success && isArpgWeapon && handler.getConfig().enableWeaponCooldownGating()) {
                    long currentTick = serverPlayer.level().getGameTime();
                    double aps = handler.getCooldownResolver().resolveAttackSpeed(serverPlayer, weaponData);
                    int cooldownDuration = handler.getCooldownResolver().resolveCooldownTicks(serverPlayer, weaponData);
                    
                    io.github.bysenom.relicwrought.combat.cooldown.WeaponAttackState state = handler.getCooldownManager().getState(serverPlayer);
                    state.recordAttack(currentTick, cooldownDuration, aps);
                    
                    io.github.bysenom.relicwrought.network.WeaponCooldownNetworking.sendSync(serverPlayer, state, true);
                }

                if (success) {
                    // Dispatch Floating Damage Number
                    io.github.bysenom.relicwrought.combat.damage.CombatTextEvent textEvent = new io.github.bysenom.relicwrought.combat.damage.CombatTextEvent(
                            hurtTarget.getId(),
                            hurtTarget.getUUID(),
                            serverPlayer.getUUID(),
                            result.totalDamage(),
                            result.isCritical(),
                            "physical",
                            serverPlayer.level().getRandom().nextLong(),
                            serverPlayer.level().getGameTime()
                    );
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                            serverPlayer,
                            new io.github.bysenom.relicwrought.network.FloatingDamageNumberPayload(textEvent)
                    );

                    // Dispatch Enemy UI Sync
                    if (hurtTarget instanceof LivingEntity living) {
                        io.github.bysenom.relicwrought.ui.EnemyClassification classification = io.github.bysenom.relicwrought.ui.EnemyClassification.NORMAL;
                        if (living instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon || living instanceof net.minecraft.world.entity.boss.wither.WitherBoss) {
                            classification = io.github.bysenom.relicwrought.ui.EnemyClassification.BOSS;
                        }

                        io.github.bysenom.relicwrought.ui.EnemyUiSnapshot snapshot = new io.github.bysenom.relicwrought.ui.EnemyUiSnapshot(
                                living.getId(),
                                living.getUUID(),
                                living.getName().getString(),
                                classification,
                                1, // level placeholder
                                living.getHealth(),
                                living.getMaxHealth(),
                                living instanceof net.minecraft.world.entity.Mob, // hostile
                                classification == io.github.bysenom.relicwrought.ui.EnemyClassification.BOSS,
                                0
                        );
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                                serverPlayer,
                                new io.github.bysenom.relicwrought.network.EnemyUiSyncPayload(snapshot)
                        );
                    }
                }
                
                return success;
            } finally {
                APPLYING_ARPG_DAMAGE.set(false);
            }
        }

        // Fallback to Vanilla damage
        return hurtTarget.hurtOrSimulate(source, vanillaDamage);
    }
}
