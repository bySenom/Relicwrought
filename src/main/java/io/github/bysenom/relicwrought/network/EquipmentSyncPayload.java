package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.equipment.EquipmentSlotStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record EquipmentSyncPayload(List<EquipmentSlotStack> slots) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EquipmentSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "equipment_sync"));

    private static final StreamCodec<RegistryFriendlyByteBuf, List<EquipmentSlotStack>> SLOT_LIST_CODEC =
            ByteBufCodecs.collection(ArrayList::new, EquipmentSlotStack.CODEC, 32);

    public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentSyncPayload> CODEC =
            StreamCodec.composite(
                    SLOT_LIST_CODEC, EquipmentSyncPayload::slots,
                    EquipmentSyncPayload::new
            );

    public EquipmentSyncPayload {
        slots = List.copyOf(slots);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
