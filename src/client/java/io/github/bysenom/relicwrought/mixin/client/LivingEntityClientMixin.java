package io.github.bysenom.relicwrought.mixin.client;

import io.github.bysenom.relicwrought.client.hud.ClientWeaponCooldownState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityClientMixin {

    @Inject(method = "getCurrentSwingDuration", at = @At("HEAD"), cancellable = true)
    private void adjustArpgSwingDuration(CallbackInfoReturnable<Integer> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof Player player)) return;

        if (player.level().isClientSide()) {
            if (io.github.bysenom.relicwrought.item.ArpgItemSystems.itemStackService().hasArpgData(player.getMainHandItem())) {
                int duration = ClientWeaponCooldownState.getState().getCooldownDurationTicks();
                if (duration > 0) {
                    cir.setReturnValue(duration);
                }
            }
        }
    }
}
