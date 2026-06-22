package io.github.bysenom.relicwrought.equipment;

import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class PlayerEquipmentData {
    private final EnumMap<ArpgEquipmentSlot, ItemStack> extraSlots;

    private PlayerEquipmentData(EnumMap<ArpgEquipmentSlot, ItemStack> extraSlots) {
        this.extraSlots = copyExtraSlots(extraSlots);
    }

    public static PlayerEquipmentData empty() {
        return new PlayerEquipmentData(new EnumMap<>(ArpgEquipmentSlot.class));
    }

    public ItemStack get(ArpgEquipmentSlot slot) {
        if (slot == null || !slot.isExtraSlot()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = extraSlots.get(slot);
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public boolean has(ArpgEquipmentSlot slot) {
        return !get(slot).isEmpty();
    }

    public PlayerEquipmentData withSlot(ArpgEquipmentSlot slot, ItemStack stack) {
        requireExtraSlot(slot);
        EnumMap<ArpgEquipmentSlot, ItemStack> next = copyExtraSlots(extraSlots);
        if (stack == null || stack.isEmpty()) {
            next.remove(slot);
        } else {
            next.put(slot, stack.copy());
        }
        return new PlayerEquipmentData(next);
    }

    public PlayerEquipmentData withoutSlot(ArpgEquipmentSlot slot) {
        requireExtraSlot(slot);
        EnumMap<ArpgEquipmentSlot, ItemStack> next = copyExtraSlots(extraSlots);
        next.remove(slot);
        return new PlayerEquipmentData(next);
    }

    public Map<ArpgEquipmentSlot, ItemStack> copySlots() {
        return Map.copyOf(copyExtraSlots(extraSlots));
    }

    public List<EquipmentSlotStack> toSlotStacks() {
        return ArpgEquipmentSlot.extraSlots().stream()
                .map(slot -> new EquipmentSlotStack(slot, get(slot)))
                .toList();
    }

    private static void requireExtraSlot(ArpgEquipmentSlot slot) {
        if (slot == null || !slot.isExtraSlot()) {
            throw new IllegalArgumentException("Only Relicwrought extra equipment slots are stored here: " + slot);
        }
    }

    private static EnumMap<ArpgEquipmentSlot, ItemStack> copyExtraSlots(Map<ArpgEquipmentSlot, ItemStack> source) {
        EnumMap<ArpgEquipmentSlot, ItemStack> copy = new EnumMap<>(ArpgEquipmentSlot.class);
        if (source == null) {
            return copy;
        }
        for (var entry : source.entrySet()) {
            ArpgEquipmentSlot slot = entry.getKey();
            ItemStack stack = entry.getValue();
            if (slot != null && slot.isExtraSlot() && stack != null && !stack.isEmpty()) {
                copy.put(slot, stack.copy());
            }
        }
        return copy;
    }
}
