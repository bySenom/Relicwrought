package io.github.bysenom.relicwrought.mixin.client;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into Hud.extractHotbarAndDecorations (confirmed via reflection on MC 26.2).
 * This is the correct hook point for rendering custom HUD overlays.
 * 
 * extractRenderState does NOT exist on Hud – it exists on Gui.
 * extractHotbarAndDecorations is called once per frame during HUD extraction.
 */
@Mixin(Hud.class)
public abstract class HudMixin {

    @Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
    private void relicwrought$afterHotbarExtraction(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!Relicwrought.config().enableRelicwroughtHud()) return;

        float partialTick = deltaTracker.getGameTimeDeltaTicks();
        
        // Player HP bar and resource bar
        io.github.bysenom.relicwrought.client.hud.PlayerHudRenderer.render(guiGraphics, partialTick);
        
        // Weapon cooldown bar
        io.github.bysenom.relicwrought.client.hud.WeaponCooldownRenderer.render(guiGraphics, partialTick);
        
        // Enemy nameplate (crosshair target)
        io.github.bysenom.relicwrought.client.enemy.EnemyNameplateRenderer.render(guiGraphics, partialTick);
        
        // Floating damage numbers
        io.github.bysenom.relicwrought.client.combattext.FloatingDamageNumberRenderer.render(guiGraphics, partialTick);
    }
}
