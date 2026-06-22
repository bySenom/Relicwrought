package io.github.bysenom.relicwrought.mixin.client;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.client.screen.RpgEquipmentScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class InventoryKeyMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void onHandleKeybinds(CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        if (mc.player == null || mc.level == null) return;
        if (!mc.canInterruptScreen()) return;

        if (mc.options.keyInventory.consumeClick()) {
            LocalPlayer player = mc.player;
            if (player.isCreative()) {
                mc.setScreenAndShow(new CreativeModeInventoryScreen(player, player.level().enabledFeatures(), player.isCreative()));
            } else if (!player.isSpectator() && Relicwrought.config().replaceVanillaInventoryScreen()) {
                RpgEquipmentScreen.open();
            }
        }
    }
}
