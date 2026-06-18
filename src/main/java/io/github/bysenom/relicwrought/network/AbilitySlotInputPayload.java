package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AbilitySlotInputPayload(int slotIndex, boolean isPressed, long sequenceId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AbilitySlotInputPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "ability_slot_input"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilitySlotInputPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, AbilitySlotInputPayload::slotIndex,
                    ByteBufCodecs.BOOL, AbilitySlotInputPayload::isPressed,
                    ByteBufCodecs.VAR_LONG, AbilitySlotInputPayload::sequenceId,
                    AbilitySlotInputPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
