package io.github.bysenom.relicwrought.equipment;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;

public final class PlayerEquipmentService {
    private final ArpgModConfig config;
    private final PlayerEquipmentRepository repository;
    private final EquipmentValidationService validationService;

    public PlayerEquipmentService(
            ArpgModConfig config,
            PlayerEquipmentRepository repository,
            EquipmentValidationService validationService
    ) {
        this.config = config;
        this.repository = repository;
        this.validationService = validationService;
    }

    public EquipmentChangeResult handleSlotClick(ServerPlayer player, ArpgEquipmentSlot slot) {
        if (!config.enableRpgInventory()) {
            return fail(player, "ui.relicwrought.inventory.disabled");
        }
        if (slot == null || !slot.isExtraSlot()) {
            return fail(player, "ui.relicwrought.inventory.vanilla_slot_read_only");
        }

        ItemStack existing = repository.getStack(player.getUUID(), slot);
        if (!existing.isEmpty()) {
            EquipmentChangeResult result = unequip(player, slot, existing);
            EquipmentSyncService.send(player, repository);
            return result;
        }

        ItemStack selected = player.getInventory().getSelectedItem();
        EquipmentChangeResult validation = validationService.validateForExtraSlot(selected, slot);
        if (!validation.success()) {
            return fail(player, validation.translationKey());
        }

        ItemStack equipped = selected.split(1);
        repository.setStack(player.getUUID(), slot, equipped);
        player.inventoryMenu.broadcastChanges();
        EquipmentSyncService.send(player, repository);
        player.sendSystemMessage(Component.translatable("ui.relicwrought.inventory.equipped"));
        return EquipmentChangeResult.success("ui.relicwrought.inventory.equipped");
    }

    public void sync(ServerPlayer player) {
        if (config.enableRpgInventory()) {
            EquipmentSyncService.send(player, repository);
        }
    }

    public void handleDeath(ServerPlayer player) {
        if (!config.enableRpgInventory() || !config.dropExtraEquipmentOnDeath()) {
            return;
        }
        if (player.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return;
        }
        for (ArpgEquipmentSlot slot : ArpgEquipmentSlot.extraSlots()) {
            ItemStack stack = repository.removeStack(player.getUUID(), slot);
            if (!stack.isEmpty()) {
                player.drop(stack, true, false);
            }
        }
        EquipmentSyncService.send(player, repository);
    }

    private EquipmentChangeResult unequip(ServerPlayer player, ArpgEquipmentSlot slot, ItemStack stack) {
        ItemStack removed = repository.removeStack(player.getUUID(), slot);
        if (removed.isEmpty()) {
            return EquipmentChangeResult.success("ui.relicwrought.inventory.unequipped");
        }
        boolean added = player.getInventory().add(removed.copy());
        if (!added) {
            player.drop(removed.copy(), false, false);
        }
        player.inventoryMenu.broadcastChanges();
        player.sendSystemMessage(Component.translatable("ui.relicwrought.inventory.unequipped"));
        return EquipmentChangeResult.success("ui.relicwrought.inventory.unequipped");
    }

    private EquipmentChangeResult fail(ServerPlayer player, String translationKey) {
        player.sendSystemMessage(Component.translatable(translationKey));
        EquipmentSyncService.send(player, repository);
        return EquipmentChangeResult.failure(translationKey);
    }
}
