package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EquipmentSyncRequestPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EquipmentSyncRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "equipment_sync_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentSyncRequestPayload> CODEC =
            StreamCodec.unit(new EquipmentSyncRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
