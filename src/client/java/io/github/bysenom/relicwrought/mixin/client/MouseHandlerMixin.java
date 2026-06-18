package io.github.bysenom.relicwrought.mixin.client;

import io.github.bysenom.relicwrought.client.hud.AbilityHotbarState;
import io.github.bysenom.relicwrought.client.hud.HotbarMode;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Redirect(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;swapPaint(D)V"))
    private void redirectSwapPaint(net.minecraft.world.entity.player.Inventory instance, double direction) {
        if (AbilityHotbarState.getCurrentMode() == HotbarMode.ABILITY) {
            // Block Vanilla item swap in ability mode
            // We could route mouse wheel to ability selection if desired, but for now just block it
            return;
        }
        // instance.swapPaint(direction);
    }
}
