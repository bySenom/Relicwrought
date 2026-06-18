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

        // Only calculate if we are hitting the main target to avoid redundant calculations
        DamageCalculationResult result = null;
        if (hurtTarget == mainTarget) {
            result = Relicwrought.getMeleeDamageHandler().calculateDamage(serverPlayer, mainTarget);
        } else {
            // For sweeping targets, we calculate against them individually
            result = Relicwrought.getMeleeDamageHandler().calculateDamage(serverPlayer, hurtTarget);
        }

        if (result != null) {
            if (hurtTarget != mainTarget) {
                // Sweeping is disabled for ARPG weapons
                return false; 
            }
            
            if (result.success()) {
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
                    
                    if (success && Relicwrought.getMeleeDamageHandler().getConfig().enableCombatDebugLogging()) {
                        Relicwrought.LOGGER.info(
                            "ARPG Melee (Mixin): Attacker={}, Target={}, RawDmg={}, Crit={}, FinalDmg={}",
                            serverPlayer.getScoreboardName(), hurtTarget.getName().getString(),
                            result.rawDamage().getTotalDamage(), result.isCritical(), result.totalDamage()
                        );
                    }
                    return success;
                } finally {
                    APPLYING_ARPG_DAMAGE.set(false);
                }
            }
        }

        // Fallback to Vanilla damage
        return hurtTarget.hurtOrSimulate(source, vanillaDamage);
    }
}
