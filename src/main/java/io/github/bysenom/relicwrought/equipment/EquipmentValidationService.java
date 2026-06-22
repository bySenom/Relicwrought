package io.github.bysenom.relicwrought.equipment;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;
import net.minecraft.world.item.ItemStack;

public final class EquipmentValidationService {
    private final ArpgModConfig config;
    private final ArpgItemStackService itemService;
    private final DataRegistry<ItemBaseDefinition> itemBases;

    public EquipmentValidationService(
            ArpgModConfig config,
            ArpgItemStackService itemService,
            DataRegistry<ItemBaseDefinition> itemBases
    ) {
        this.config = config;
        this.itemService = itemService;
        this.itemBases = itemBases;
    }

    public EquipmentChangeResult validateForExtraSlot(ItemStack stack, ArpgEquipmentSlot slot) {
        if (slot == null) {
            return EquipmentChangeResult.failure("ui.relicwrought.inventory.invalid_slot");
        }
        if (!slot.isExtraSlot()) {
            return EquipmentChangeResult.failure("ui.relicwrought.inventory.vanilla_slot_read_only");
        }
        return validateForSlot(stack, slot);
    }

    public EquipmentChangeResult validateForSlot(ItemStack stack, ArpgEquipmentSlot slot) {
        if (slot == null) {
            return EquipmentChangeResult.failure("ui.relicwrought.inventory.invalid_slot");
        }
        if (stack == null || stack.isEmpty()) {
            return EquipmentChangeResult.failure("ui.relicwrought.inventory.no_selected_item");
        }
        if (stack.getCount() != 1) {
            return EquipmentChangeResult.failure("ui.relicwrought.inventory.stack_size_invalid");
        }
        if (!itemService.hasArpgData(stack)) {
            if (config.allowNonArpgItemsInEquipment()) {
                return EquipmentChangeResult.success("ui.relicwrought.inventory.equipped");
            }
            return EquipmentChangeResult.failure("ui.relicwrought.inventory.invalid_item");
        }

        var readResult = itemService.read(stack);
        var itemData = readResult.data().orElse(null);
        if (itemData == null) {
            return EquipmentChangeResult.failure("ui.relicwrought.inventory.invalid_item");
        }

        ItemBaseDefinition itemBase = itemBases.get(itemData.itemBaseId()).orElse(null);
        if (itemBase == null) {
            return EquipmentChangeResult.failure("tooltip.relicwrought.missing_definition");
        }
        if (!itemBase.validSlots().contains(slot)) {
            return EquipmentChangeResult.failure("ui.relicwrought.inventory.invalid_slot");
        }
        return EquipmentChangeResult.success("ui.relicwrought.inventory.equipped");
    }
}
