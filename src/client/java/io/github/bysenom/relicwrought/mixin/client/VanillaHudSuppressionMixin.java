package io.github.bysenom.relicwrought.mixin.client;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses vanilla hearts and armor rendering when the Relicwrought HUD is active.
 *
 * Targets (confirmed via reflection on MC 26.2 Hud class):
 * - extractHearts: private void Hud.extractHearts(GuiGraphicsExtractor, Player, int, int, int, int, float, int, int, int, boolean)
 * - extractArmor:  private static void Hud.extractArmor(GuiGraphicsExtractor, Player, int, int, int, int)
 */
@Mixin(Hud.class)
public abstract class VanillaHudSuppressionMixin {

    @Inject(method = "extractHearts", at = @At("HEAD"), cancellable = true)
    private void relicwrought$hideVanillaHealth(CallbackInfo ci) {
        if (Relicwrought.config().enableRelicwroughtHud() && Relicwrought.config().hideVanillaHearts()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void relicwrought$hideVanillaHealthBar(CallbackInfo ci) {
        if (Relicwrought.config().enableRelicwroughtHud() && Relicwrought.config().hideVanillaHearts()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractArmor", at = @At("HEAD"), cancellable = true)
    private static void relicwrought$hideVanillaArmor(CallbackInfo ci) {
        if (Relicwrought.config().enableRelicwroughtHud() && Relicwrought.config().hideVanillaArmor()) {
            ci.cancel();
        }
    }
}
