package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EquipmentOpenPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EquipmentOpenPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "equipment_open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentOpenPayload> CODEC =
            StreamCodec.unit(new EquipmentOpenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
