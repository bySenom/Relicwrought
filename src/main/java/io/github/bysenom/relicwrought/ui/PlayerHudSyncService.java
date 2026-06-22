package io.github.bysenom.relicwrought.ui;

import io.github.bysenom.relicwrought.network.PlayerHudSyncPayload;
import io.github.bysenom.relicwrought.player.PlayerArpgProfile;
import io.github.bysenom.relicwrought.player.PlayerProfileManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerHudSyncService {
    private PlayerHudSyncService() {
    }

    public static void send(ServerPlayer player, PlayerProfileManager profileManager) {
        if (player == null) {
            return;
        }
        PlayerArpgProfile profile = profileManager != null
                ? profileManager.getProfile(player.getUUID())
                : PlayerArpgProfile.empty();
        CharacterResourceState resourceState = profile.classSelected()
                ? CharacterResourceResolver.resolveState(profile.classId(), profile.currentResourceValue())
                : CharacterResourceState.empty();
        ServerPlayNetworking.send(player, new PlayerHudSyncPayload(
                player.getHealth(),
                player.getMaxHealth(),
                resourceState
        ));
    }
}
