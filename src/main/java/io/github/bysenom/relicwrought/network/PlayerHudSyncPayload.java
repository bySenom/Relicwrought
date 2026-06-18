package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.ui.CharacterResourceState;
import io.github.bysenom.relicwrought.ui.CharacterResourceType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlayerHudSyncPayload(
        double currentHealth,
        double maximumHealth,
        CharacterResourceState resourceState
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerHudSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "player_hud_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CharacterResourceState> RESOURCE_STATE_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(i -> CharacterResourceType.values()[i], Enum::ordinal), CharacterResourceState::type,
            ByteBufCodecs.DOUBLE, CharacterResourceState::currentValue,
            ByteBufCodecs.DOUBLE, CharacterResourceState::maximumValue,
            ByteBufCodecs.INT, CharacterResourceState::dataVersion,
            CharacterResourceState::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerHudSyncPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, PlayerHudSyncPayload::currentHealth,
                    ByteBufCodecs.DOUBLE, PlayerHudSyncPayload::maximumHealth,
                    RESOURCE_STATE_CODEC, PlayerHudSyncPayload::resourceState,
                    PlayerHudSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
