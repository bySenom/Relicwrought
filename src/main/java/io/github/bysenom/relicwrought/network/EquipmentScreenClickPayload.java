package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EquipmentScreenClickPayload(
        SourceType sourceType,
        int sourceSlot,
        SourceType targetType,
        int targetSlot,
        int mouseButton,
        ActionType actionType,
        int sequence
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EquipmentScreenClickPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "equipment_screen_click"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentScreenClickPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.idMapper(index -> SourceType.values()[index], Enum::ordinal),
                    EquipmentScreenClickPayload::sourceType,
                    ByteBufCodecs.INT,
                    EquipmentScreenClickPayload::sourceSlot,
                    ByteBufCodecs.idMapper(index -> SourceType.values()[index], Enum::ordinal),
                    EquipmentScreenClickPayload::targetType,
                    ByteBufCodecs.INT,
                    EquipmentScreenClickPayload::targetSlot,
                    ByteBufCodecs.INT,
                    EquipmentScreenClickPayload::mouseButton,
                    ByteBufCodecs.idMapper(index -> ActionType.values()[index], Enum::ordinal),
                    EquipmentScreenClickPayload::actionType,
                    ByteBufCodecs.INT,
                    EquipmentScreenClickPayload::sequence,
                    EquipmentScreenClickPayload::new
            );

    public EquipmentScreenClickPayload {
        sourceType = sourceType == null ? SourceType.NONE : sourceType;
        targetType = targetType == null ? SourceType.NONE : targetType;
        actionType = actionType == null ? ActionType.MOVE : actionType;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum SourceType {
        NONE,
        INVENTORY,
        EQUIPMENT
    }

    public enum ActionType {
        MOVE,
        SWAP
    }
}
