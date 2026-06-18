package io.github.bysenom.relicwrought.mixin.client;

import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderState(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        io.github.bysenom.relicwrought.client.hud.WeaponCooldownRenderer.render(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
        io.github.bysenom.relicwrought.client.hud.DualHotbarRenderer.render(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
        io.github.bysenom.relicwrought.client.hud.PlayerHudRenderer.render(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
    }
}
