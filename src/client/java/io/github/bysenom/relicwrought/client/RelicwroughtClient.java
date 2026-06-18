package io.github.bysenom.relicwrought.client;

import io.github.bysenom.relicwrought.network.WeaponCooldownSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RelicwroughtClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        io.github.bysenom.relicwrought.client.tooltip.ArpgItemTooltipAppender.register();
        io.github.bysenom.relicwrought.client.screen.ClassSelectionClientState.register();

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

        ClientPlayNetworking.registerGlobalReceiver(io.github.bysenom.relicwrought.network.PlayerProgressionSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                io.github.bysenom.relicwrought.client.ClientArpgState.getCharacterScreenModel().updateProgression(
                        payload.characterLevel(), payload.currentLevelXp(), payload.xpForNextLevel(),
                        payload.totalXp(), payload.unspentAttributePoints(), payload.allocatedAttributes(),
                        payload.totalAttributes()
                );
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(io.github.bysenom.relicwrought.network.PlayerHudSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                io.github.bysenom.relicwrought.client.ClientArpgState.updateHud(
                        payload.currentHealth(), payload.maximumHealth(), payload.resourceState()
                );
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(io.github.bysenom.relicwrought.network.FloatingDamageNumberPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                io.github.bysenom.relicwrought.client.combattext.FloatingDamageNumberManager.addEvent(payload.event());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(io.github.bysenom.relicwrought.network.EnemyUiSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().level != null) {
                    io.github.bysenom.relicwrought.client.enemy.EnemyUiTracker.updateSnapshot(
                            payload.snapshot(), context.client().level.getGameTime()
                    );
                }
            });
        });

        io.github.bysenom.relicwrought.client.KeyBindingRegistry.register();
    }
}
