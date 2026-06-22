package io.github.bysenom.relicwrought.client;

import io.github.bysenom.relicwrought.client.screen.CharacterScreenModel;
import io.github.bysenom.relicwrought.equipment.EquipmentSlotStack;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.ui.CharacterResourceState;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ClientArpgState {
    private static final CharacterScreenModel characterScreenModel = new CharacterScreenModel();
    
    private static double currentHealth = 20.0;
    private static double maximumHealth = 20.0;
    private static CharacterResourceState resourceState = CharacterResourceState.empty();
    private static boolean hudSyncReceived = false;
    private static final EnumMap<ArpgEquipmentSlot, ItemStack> equipmentSlots = new EnumMap<>(ArpgEquipmentSlot.class);
    private static boolean equipmentSyncReceived = false;

    public static CharacterScreenModel getCharacterScreenModel() {
        return characterScreenModel;
    }
    
    public static void updateHud(double health, double maxHealth, CharacterResourceState resource) {
        currentHealth = health;
        maximumHealth = maxHealth;
        resourceState = resource == null ? CharacterResourceState.empty() : resource.clamp();
        hudSyncReceived = true;
    }

    public static double getCurrentHealth() { return currentHealth; }
    public static double getMaximumHealth() { return maximumHealth; }
    public static CharacterResourceState getResourceState() { return resourceState; }
    public static boolean hasHudSync() { return hudSyncReceived; }

    public static void updateEquipment(List<EquipmentSlotStack> slots) {
        equipmentSlots.clear();
        if (slots != null) {
            for (EquipmentSlotStack slotStack : slots) {
                equipmentSlots.put(slotStack.slot(), slotStack.stack().copy());
            }
        }
        equipmentSyncReceived = true;
    }

    public static ItemStack getEquipmentStack(ArpgEquipmentSlot slot) {
        ItemStack stack = equipmentSlots.get(slot);
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public static Map<ArpgEquipmentSlot, ItemStack> copyEquipmentSlots() {
        EnumMap<ArpgEquipmentSlot, ItemStack> copy = new EnumMap<>(ArpgEquipmentSlot.class);
        for (var entry : equipmentSlots.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }

    public static boolean hasEquipmentSync() {
        return equipmentSyncReceived;
    }
}
