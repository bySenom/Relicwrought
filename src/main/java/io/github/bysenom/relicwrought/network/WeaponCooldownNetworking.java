package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.combat.cooldown.WeaponAttackState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class WeaponCooldownNetworking {

    public static void registerPayloads() {
        PayloadTypeRegistry.clientboundPlay().register(WeaponCooldownSyncPayload.TYPE, WeaponCooldownSyncPayload.STREAM_CODEC);
    }

    public static void sendSync(ServerPlayer player, WeaponAttackState state, boolean triggerReset) {
        WeaponCooldownSyncPayload payload = new WeaponCooldownSyncPayload(
                state.getCooldownDurationTicks(),
                state.getCurrentAttackSpeed(),
                triggerReset
        );
        ServerPlayNetworking.send(player, payload);
    }
}
