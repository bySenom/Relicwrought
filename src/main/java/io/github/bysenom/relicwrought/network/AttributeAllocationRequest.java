package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.progression.CharacterAttribute;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AttributeAllocationRequest(
        String attributeName,
        int amount
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AttributeAllocationRequest> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "attribute_alloc_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeAllocationRequest> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, AttributeAllocationRequest::attributeName,
                    ByteBufCodecs.INT, AttributeAllocationRequest::amount,
                    AttributeAllocationRequest::new
            );

    public CharacterAttribute resolveAttribute() {
        try {
            return CharacterAttribute.valueOf(attributeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
