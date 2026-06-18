package io.github.bysenom.relicwrought.client;

import io.github.bysenom.relicwrought.network.WeaponCooldownSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RelicwroughtClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(WeaponCooldownSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                io.github.bysenom.relicwrought.combat.cooldown.WeaponAttackState clientState = 
                        io.github.bysenom.relicwrought.client.hud.ClientWeaponCooldownState.getState();
                
                clientState.update(context.client().level.getGameTime(), payload.cooldownDurationTicks(), payload.attackSpeed());
                if (payload.triggerCooldownReset()) {
                    clientState.recordAttack(context.client().level.getGameTime(), payload.cooldownDurationTicks(), payload.attackSpeed());
                }
            });
        });

    }
}
