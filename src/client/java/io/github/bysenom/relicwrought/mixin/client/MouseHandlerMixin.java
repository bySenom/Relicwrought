package io.github.bysenom.relicwrought.mixin.client;

import io.github.bysenom.relicwrought.client.hud.AbilityHotbarState;
import io.github.bysenom.relicwrought.client.hud.HotbarMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void relicwrought$onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (window == client.getWindow().handle()) {
            boolean isScreenOpen = !client.canInterruptScreen();
            boolean isPlayerPresent = client.player != null;
            HotbarMode currentMode = AbilityHotbarState.getCurrentMode();

            if (io.github.bysenom.relicwrought.client.hud.AbilityInputRouter.shouldBlockHotbarScroll(currentMode, isScreenOpen, isPlayerPresent)) {
                if (io.github.bysenom.relicwrought.Relicwrought.config().enableCombatDebugLogging()) {
                    io.github.bysenom.relicwrought.Relicwrought.LOGGER.info("[HUD] Blocked item hotbar scroll in ABILITY mode");
                }
                ci.cancel();
            }
        }
    }
}
