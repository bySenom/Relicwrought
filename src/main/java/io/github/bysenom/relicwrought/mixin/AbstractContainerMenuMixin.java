package io.github.bysenom.relicwrought.mixin;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void onSlotClick(int slotId, int button, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (!Relicwrought.config().disablePlayerInventoryCrafting()) return;
        if (!(((AbstractContainerMenu) (Object) this) instanceof InventoryMenu)) return;
        if (slotId >= 0 && slotId <= 4) {
            ci.cancel();
        }
    }
}
