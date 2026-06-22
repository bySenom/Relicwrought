package io.github.bysenom.relicwrought.client;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.client.hud.AbilityHotbarState;
import io.github.bysenom.relicwrought.client.hud.HotbarMode;
import io.github.bysenom.relicwrought.network.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RelicwroughtClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Relicwrought.LOGGER.info("[Relicwrought Client] Initializing client systems");

        io.github.bysenom.relicwrought.client.tooltip.ArpgItemTooltipAppender.register();
        io.github.bysenom.relicwrought.client.screen.ClassSelectionClientState.register();

        // --- Network Receivers ---
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

        ClientPlayNetworking.registerGlobalReceiver(CharacterStatSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                io.github.bysenom.relicwrought.client.ClientArpgState.getCharacterScreenModel().updateStats(payload.stats());
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

        ClientPlayNetworking.registerGlobalReceiver(io.github.bysenom.relicwrought.network.EquipmentSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                io.github.bysenom.relicwrought.client.ClientArpgState.updateEquipment(payload.slots());
                io.github.bysenom.relicwrought.client.screen.RpgEquipmentScreen.refreshOpenScreen();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(io.github.bysenom.relicwrought.network.EquipmentOpenPayload.TYPE, (payload, context) -> {
            context.client().execute(io.github.bysenom.relicwrought.client.screen.RpgEquipmentScreen::open);
        });

        // --- Ability Sync Receivers ---
        ClientPlayNetworking.registerGlobalReceiver(AbilityLoadoutSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientAbilityState.updateLoadout(payload.slots());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(AbilityCooldownSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientAbilityState.updateCooldowns(payload.cooldowns());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(AbilityUseResultPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().player != null && !payload.success() && payload.message() != null) {
                    context.client().player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal("§c" + payload.message())
                    );
                }
            });
        });

        // --- Client Tick ---
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) return;
            io.github.bysenom.relicwrought.client.combattext.FloatingDamageNumberManager.tick();
            if (Relicwrought.config().enableAbilities() && Relicwrought.config().enableAbilityHotbar()) {
                ClientAbilityState.tick();
            }
        });

        io.github.bysenom.relicwrought.client.KeyBindingRegistry.register();

        Relicwrought.LOGGER.info("[Relicwrought Client] Client systems initialized:");
        Relicwrought.LOGGER.info("  - health bar: true");
        Relicwrought.LOGGER.info("  - resource bar: true");
        Relicwrought.LOGGER.info("  - weapon cooldown: true");
        Relicwrought.LOGGER.info("  - dual hotbar: true");
        Relicwrought.LOGGER.info("  - enemy nameplates: true");
        Relicwrought.LOGGER.info("  - floating damage numbers: true");
    }
}
