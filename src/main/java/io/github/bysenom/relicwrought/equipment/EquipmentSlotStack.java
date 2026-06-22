package io.github.bysenom.relicwrought.equipment;

import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record EquipmentSlotStack(ArpgEquipmentSlot slot, ItemStack stack) {
    public EquipmentSlotStack {
        if (slot == null) {
            throw new IllegalArgumentException("Equipment slot must not be null");
        }
        stack = stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentSlotStack> CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(index -> ArpgEquipmentSlot.values()[index], Enum::ordinal), EquipmentSlotStack::slot,
            ItemStack.OPTIONAL_STREAM_CODEC, EquipmentSlotStack::stack,
            EquipmentSlotStack::new
    );
}
