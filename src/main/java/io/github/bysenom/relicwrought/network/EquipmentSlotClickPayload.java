package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EquipmentSlotClickPayload(ArpgEquipmentSlot slot) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EquipmentSlotClickPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "equipment_slot_click"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentSlotClickPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.idMapper(index -> ArpgEquipmentSlot.values()[index], Enum::ordinal),
                    EquipmentSlotClickPayload::slot,
                    EquipmentSlotClickPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
