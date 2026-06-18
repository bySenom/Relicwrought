package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClassSelectionResponse(
        boolean success,
        String message,
        String classId
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClassSelectionResponse> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "class_select_response"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClassSelectionResponse> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, ClassSelectionResponse::success,
                    ByteBufCodecs.STRING_UTF8, ClassSelectionResponse::message,
                    ByteBufCodecs.STRING_UTF8, ClassSelectionResponse::classId,
                    ClassSelectionResponse::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
