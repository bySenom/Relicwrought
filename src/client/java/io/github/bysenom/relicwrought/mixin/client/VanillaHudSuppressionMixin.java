package io.github.bysenom.relicwrought.mixin.client;

import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.bysenom.relicwrought.ArpgModConfig;

/**
 * Fallback Mixin for suppressing vanilla health and armor bars.
 * Fabric API 0.152.1+26.2 does not provide HudElementRegistry in the current workspace state.
 * This injects directly into the actually decompiled Minecraft 26.2 (1.21.4+) methods.
 */
@Mixin(Hud.class)
public abstract class VanillaHudSuppressionMixin {

    @Inject(
        method = "extractHearts(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void relicwrought$hideVanillaHealth(CallbackInfo ci) {
        if (io.github.bysenom.relicwrought.Relicwrought.config().hideVanillaHearts()) {
            ci.cancel();
        }
    }

    @Inject(
        method = "extractArmor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIII)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void relicwrought$hideVanillaArmor(CallbackInfo ci) {
        if (io.github.bysenom.relicwrought.Relicwrought.config().hideVanillaArmor()) {
            ci.cancel();
        }
    }
}
