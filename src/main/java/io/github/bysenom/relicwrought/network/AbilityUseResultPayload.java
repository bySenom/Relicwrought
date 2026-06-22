package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AbilityUseResultPayload(int slotIndex, boolean success, String message) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AbilityUseResultPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "ability_use_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityUseResultPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, AbilityUseResultPayload::slotIndex,
                    ByteBufCodecs.BOOL, AbilityUseResultPayload::success,
                    ByteBufCodecs.STRING_UTF8, AbilityUseResultPayload::message,
                    AbilityUseResultPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
