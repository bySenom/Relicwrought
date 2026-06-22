package io.github.bysenom.relicwrought.equipment;

import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.network.EquipmentSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class EquipmentSyncService {
    private EquipmentSyncService() {
    }

    public static void send(ServerPlayer player, PlayerEquipmentRepository repository) {
        List<EquipmentSlotStack> slots = new ArrayList<>();
        for (ArpgEquipmentSlot slot : ArpgEquipmentSlot.displayOrder()) {
            ItemStack stack = slot.vanillaSlot()
                    .map(player::getItemBySlot)
                    .orElseGet(() -> repository.getStack(player.getUUID(), slot));
            slots.add(new EquipmentSlotStack(slot, stack));
        }
        ServerPlayNetworking.send(player, new EquipmentSyncPayload(slots));
    }
}
