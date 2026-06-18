package io.github.bysenom.relicwrought.client.hud;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.combat.animation.AnimationProfile;
import io.github.bysenom.relicwrought.combat.cooldown.WeaponAttackState;
import io.github.bysenom.relicwrought.item.ArpgItemSystems;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WeaponCooldownHudModel {
    public final boolean visible;
    public final double progress;
    public final boolean ready;
    public final double attackSpeed;
    public final int cooldownTicks;
    public final AnimationProfile weaponCategory;

    public WeaponCooldownHudModel(Minecraft mc, WeaponAttackState state, ArpgModConfig config) {
        Player player = mc.player;
        if (player == null || !config.showWeaponCooldown()) {
            this.visible = false;
            this.progress = 0;
            this.ready = false;
            this.attackSpeed = 0;
            this.cooldownTicks = 0;
            this.weaponCategory = AnimationProfile.MEDIUM;
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        boolean hasArpgData = ArpgItemSystems.itemStackService().hasArpgData(mainHand);

        if (!hasArpgData) {
            this.visible = false;
            this.progress = 0;
            this.ready = false;
            this.attackSpeed = 0;
            this.cooldownTicks = 0;
            this.weaponCategory = AnimationProfile.MEDIUM;
            return;
        }

        ArpgItemData weaponData = ArpgItemSystems.itemStackService().read(mainHand).data().orElse(null);
        if (weaponData == null) {
            this.visible = false;
            this.progress = 0;
            this.ready = false;
            this.attackSpeed = 0;
            this.cooldownTicks = 0;
            this.weaponCategory = AnimationProfile.MEDIUM;
            return;
        }

        // Determine animation profile from weapon data/category.
        AnimationProfile profile = AnimationProfile.MEDIUM;
        ItemBaseDefinition itemBase = ArpgItemSystems.bootstrapResult().itemBases().get(weaponData.itemBaseId()).orElse(null);
        if (itemBase != null && itemBase.category() != null) {
            if (itemBase.category().name().contains("DAGGER")) {
                profile = AnimationProfile.LIGHT;
            } else if (itemBase.category().name().contains("TWO_HANDED")) {
                profile = AnimationProfile.HEAVY;
            }
        }

        long currentTick = mc.level.getGameTime();
        
        this.visible = true;
        this.progress = state.getProgress(currentTick);
        this.ready = state.isReady(currentTick);
        this.attackSpeed = state.getCurrentAttackSpeed();
        this.cooldownTicks = state.getCooldownDurationTicks();
        this.weaponCategory = profile;
    }
}
