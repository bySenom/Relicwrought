package io.github.bysenom.relicwrought.mixin.client;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
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
    private void relicwrought$hideVanillaHealth(
            GuiGraphicsExtractor guiGraphics,
            Player player,
            int x,
            int y,
            int height,
            int regenerationHeartIndex,
            float maxHealth,
            int currentHealth,
            int displayHealth,
            int absorptionAmount,
            boolean blinking,
            CallbackInfo ci
    ) {
        if (Relicwrought.config().enableRelicwroughtHud() && Relicwrought.config().hideVanillaHearts()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractArmor", at = @At("HEAD"), cancellable = true)
    private static void relicwrought$hideVanillaArmor(
            GuiGraphicsExtractor guiGraphics,
            Player player,
            int armorValue,
            int x,
            int y,
            int height,
            CallbackInfo ci
    ) {
        if (Relicwrought.config().enableRelicwroughtHud() && Relicwrought.config().hideVanillaArmor()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractItemHotbar", at = @At("HEAD"), cancellable = true)
    private void relicwrought$hideVanillaItemHotbar(
            GuiGraphicsExtractor guiGraphics,
            net.minecraft.client.DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {
        if (Relicwrought.config().enableRelicwroughtHud() && io.github.bysenom.relicwrought.client.hud.AbilityHotbarState.getCurrentMode() == io.github.bysenom.relicwrought.client.hud.HotbarMode.ABILITY) {
            ci.cancel();
        }
    }
}
