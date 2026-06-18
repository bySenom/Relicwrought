package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.ui.EnemyUiSnapshot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EnemyUiSyncPayload(EnemyUiSnapshot snapshot) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EnemyUiSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "enemy_ui_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnemyUiSyncPayload> CODEC =
            StreamCodec.composite(
                    EnemyUiSnapshot.CODEC, EnemyUiSyncPayload::snapshot,
                    EnemyUiSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
