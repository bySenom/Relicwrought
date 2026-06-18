package io.github.bysenom.relicwrought.mixin.client;

import io.github.bysenom.relicwrought.client.hud.ClientWeaponCooldownState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = (Minecraft) (Object) this;
        Player player = mc.player;
        if (player == null) return;
        
        io.github.bysenom.relicwrought.combat.cooldown.WeaponAttackState state = ClientWeaponCooldownState.getState();
        if (!state.isReady(mc.level.getGameTime())) {
            cir.setReturnValue(false); // Reject the attack input early
        } else {
            // Predictive cooldown reset. The server will sync the exact tick later.
            state.recordAttack(mc.level.getGameTime(), state.getCooldownDurationTicks(), state.getCurrentAttackSpeed());
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        Player player = mc.player;
        if (player == null || mc.level == null) return;
        
        io.github.bysenom.relicwrought.combat.cooldown.WeaponAttackState state = ClientWeaponCooldownState.getState();
        net.minecraft.world.item.ItemStack mainHand = player.getMainHandItem();
        
        io.github.bysenom.relicwrought.item.model.ArpgItemData weaponData = null;
        if (io.github.bysenom.relicwrought.item.ArpgItemSystems.itemStackService().hasArpgData(mainHand)) {
            weaponData = io.github.bysenom.relicwrought.item.ArpgItemSystems.itemStackService().read(mainHand).data().orElse(null);
        }
        
        java.util.UUID weaponUuid = weaponData != null ? weaponData.itemId() : null;
        long currentTick = mc.level.getGameTime();
        
        if (state.checkWeaponSwap(currentTick, weaponUuid)) {
            // Reset client side prediction on swap. The server will also reset and sync.
            state.resetCooldown(currentTick);
        }
    }
}
