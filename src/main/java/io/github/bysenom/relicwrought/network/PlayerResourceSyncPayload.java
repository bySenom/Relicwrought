package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlayerResourceSyncPayload(double currentResource, double maxResource, String resourceType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerResourceSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "player_resource_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerResourceSyncPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, PlayerResourceSyncPayload::currentResource,
                    ByteBufCodecs.DOUBLE, PlayerResourceSyncPayload::maxResource,
                    ByteBufCodecs.STRING_UTF8, PlayerResourceSyncPayload::resourceType,
                    PlayerResourceSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
