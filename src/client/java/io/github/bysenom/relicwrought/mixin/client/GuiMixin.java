package io.github.bysenom.relicwrought.mixin.client;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void hideVanillaHearts(GuiGraphicsExtractor guiGraphics, net.minecraft.world.entity.player.Player player, int x, int y, int height, int offsetHeartIndex, float maxHealth, int currentHealth, int displayHealth, int absorptionAmount, boolean renderHighlight, CallbackInfo ci) {
        if (Relicwrought.config().enableRelicwroughtHud() && Relicwrought.config().hideVanillaHearts()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private void hideVanillaArmor(GuiGraphicsExtractor guiGraphics, net.minecraft.world.entity.player.Player player, int y, int heartRows, int height, CallbackInfo ci) {
        if (Relicwrought.config().enableRelicwroughtHud() && Relicwrought.config().hideVanillaArmor()) {
            ci.cancel();
        }
    }
}
