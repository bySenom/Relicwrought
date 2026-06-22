package io.github.bysenom.relicwrought.equipment;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.network.EquipmentScreenClickPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;

public final class PlayerEquipmentService {
    private static final int PLAYER_INVENTORY_SLOTS = 36;

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

    public EquipmentChangeResult handleScreenClick(ServerPlayer player, EquipmentScreenClickPayload payload) {
        if (!config.enableRpgInventory()) {
            return fail(player, "ui.relicwrought.inventory.disabled");
        }
        if (payload == null) {
            return fail(player, "ui.relicwrought.inventory.invalid_slot");
        }
        if (payload.sourceType() == EquipmentScreenClickPayload.SourceType.INVENTORY
                && payload.targetType() == EquipmentScreenClickPayload.SourceType.EQUIPMENT) {
            return moveInventoryToEquipment(player, payload.sourceSlot(), resolveEquipmentSlot(payload.targetSlot()));
        }
        if (payload.sourceType() == EquipmentScreenClickPayload.SourceType.EQUIPMENT
                && payload.targetType() == EquipmentScreenClickPayload.SourceType.INVENTORY) {
            return moveEquipmentToInventory(player, resolveEquipmentSlot(payload.sourceSlot()), payload.targetSlot());
        }
        if (payload.sourceType() == EquipmentScreenClickPayload.SourceType.EQUIPMENT
                && payload.targetType() == EquipmentScreenClickPayload.SourceType.EQUIPMENT) {
            return moveEquipmentToEquipment(player, resolveEquipmentSlot(payload.sourceSlot()), resolveEquipmentSlot(payload.targetSlot()));
        }
        return fail(player, "ui.relicwrought.inventory.invalid_slot");
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

    private EquipmentChangeResult moveInventoryToEquipment(ServerPlayer player, int inventorySlot, ArpgEquipmentSlot targetSlot) {
        if (!isInventorySlot(inventorySlot) || targetSlot == null) {
            return fail(player, "ui.relicwrought.inventory.invalid_slot");
        }
        ItemStack sourceStack = player.getInventory().getItem(inventorySlot);
        EquipmentChangeResult validation = validationService.validateForSlot(sourceStack, targetSlot);
        if (!validation.success()) {
            return fail(player, validation.translationKey());
        }

        ItemStack existingTarget = getEquipmentStack(player, targetSlot);
        player.getInventory().setItem(inventorySlot, existingTarget.isEmpty() ? ItemStack.EMPTY : existingTarget.copy());
        setEquipmentStack(player, targetSlot, sourceStack.copy());
        return finish(player, "ui.relicwrought.inventory.equipped");
    }

    private EquipmentChangeResult moveEquipmentToInventory(ServerPlayer player, ArpgEquipmentSlot sourceSlot, int inventorySlot) {
        if (sourceSlot == null || !isInventorySlot(inventorySlot)) {
            return fail(player, "ui.relicwrought.inventory.invalid_slot");
        }
        ItemStack sourceStack = getEquipmentStack(player, sourceSlot);
        if (sourceStack.isEmpty()) {
            return fail(player, "ui.relicwrought.inventory.no_selected_item");
        }

        ItemStack existingTarget = player.getInventory().getItem(inventorySlot);
        if (!existingTarget.isEmpty()) {
            EquipmentChangeResult validation = validationService.validateForSlot(existingTarget, sourceSlot);
            if (!validation.success()) {
                return fail(player, validation.translationKey());
            }
        }

        player.getInventory().setItem(inventorySlot, sourceStack.copy());
        setEquipmentStack(player, sourceSlot, existingTarget.isEmpty() ? ItemStack.EMPTY : existingTarget.copy());
        return finish(player, existingTarget.isEmpty()
                ? "ui.relicwrought.inventory.unequipped"
                : "ui.relicwrought.inventory.swapped");
    }

    private EquipmentChangeResult moveEquipmentToEquipment(ServerPlayer player, ArpgEquipmentSlot sourceSlot, ArpgEquipmentSlot targetSlot) {
        if (sourceSlot == null || targetSlot == null || sourceSlot == targetSlot) {
            return fail(player, "ui.relicwrought.inventory.invalid_slot");
        }
        ItemStack sourceStack = getEquipmentStack(player, sourceSlot);
        if (sourceStack.isEmpty()) {
            return fail(player, "ui.relicwrought.inventory.no_selected_item");
        }
        EquipmentChangeResult sourceValidation = validationService.validateForSlot(sourceStack, targetSlot);
        if (!sourceValidation.success()) {
            return fail(player, sourceValidation.translationKey());
        }

        ItemStack targetStack = getEquipmentStack(player, targetSlot);
        if (!targetStack.isEmpty()) {
            EquipmentChangeResult targetValidation = validationService.validateForSlot(targetStack, sourceSlot);
            if (!targetValidation.success()) {
                return fail(player, targetValidation.translationKey());
            }
        }

        setEquipmentStack(player, targetSlot, sourceStack.copy());
        setEquipmentStack(player, sourceSlot, targetStack.isEmpty() ? ItemStack.EMPTY : targetStack.copy());
        return finish(player, targetStack.isEmpty()
                ? "ui.relicwrought.inventory.equipped"
                : "ui.relicwrought.inventory.swapped");
    }

    private EquipmentChangeResult finish(ServerPlayer player, String translationKey) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        EquipmentSyncService.send(player, repository);
        player.sendSystemMessage(Component.translatable(translationKey));
        return EquipmentChangeResult.success(translationKey);
    }

    private ItemStack getEquipmentStack(ServerPlayer player, ArpgEquipmentSlot slot) {
        return slot.vanillaSlot()
                .map(player::getItemBySlot)
                .orElseGet(() -> repository.getStack(player.getUUID(), slot));
    }

    private void setEquipmentStack(ServerPlayer player, ArpgEquipmentSlot slot, ItemStack stack) {
        ItemStack safeStack = stack == null ? ItemStack.EMPTY : stack.copy();
        if (slot.vanillaSlot().isPresent()) {
            EquipmentSlot vanillaSlot = slot.vanillaSlot().orElseThrow();
            player.setItemSlot(vanillaSlot, safeStack);
            return;
        }
        if (safeStack.isEmpty()) {
            repository.removeStack(player.getUUID(), slot);
        } else {
            repository.setStack(player.getUUID(), slot, safeStack);
        }
    }

    private static ArpgEquipmentSlot resolveEquipmentSlot(int ordinal) {
        ArpgEquipmentSlot[] values = ArpgEquipmentSlot.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return null;
        }
        return values[ordinal];
    }

    private static boolean isInventorySlot(int slot) {
        return slot >= 0 && slot < PLAYER_INVENTORY_SLOTS;
    }

    private EquipmentChangeResult fail(ServerPlayer player, String translationKey) {
        player.sendSystemMessage(Component.translatable(translationKey));
        EquipmentSyncService.send(player, repository);
        return EquipmentChangeResult.failure(translationKey);
    }
}
