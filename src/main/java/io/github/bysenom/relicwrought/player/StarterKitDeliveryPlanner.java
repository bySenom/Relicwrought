package io.github.bysenom.relicwrought.player;

import java.util.ArrayList;
import java.util.List;

public final class StarterKitDeliveryPlanner {

    private StarterKitDeliveryPlanner() {}

    public static DeliveryPlan planDelivery(StarterKitDefinition kit, InventorySnapshot inventory) {
        int inserted = 0;
        int equipped = 0;
        int dropped = 0;
        List<String> failed = new ArrayList<>();

        for (StarterKitEntry entry : kit.entries()) {
            try {
                if (!isValidEntry(entry)) {
                    failed.add("Invalid entry: " + entry.itemBaseId());
                    continue;
                }

                if (entry.autoEquip()) {
                    EquipmentSlotType slot = resolveSlot(entry.slot());
                    if (slot != null && inventory.isSlotOccupied(slot)) {
                        if (inventory.hasFreeSpace()) {
                            inserted++;
                        } else {
                            dropped++;
                        }
                    } else if (slot != null) {
                        equipped++;
                    } else {
                        if (inventory.hasFreeSpace()) {
                            inserted++;
                        } else {
                            dropped++;
                        }
                    }
                } else {
                    if (inventory.hasFreeSpace()) {
                        inserted++;
                    } else {
                        dropped++;
                    }
                }
            } catch (Exception e) {
                failed.add(entry.itemBaseId() + ": " + e.getMessage());
            }
        }

        return new DeliveryPlan(inserted, equipped, dropped, failed.size(), failed);
    }

    private static boolean isValidEntry(StarterKitEntry entry) {
        return entry.itemBaseId() != null && !entry.itemBaseId().isBlank()
                && entry.itemLevel() >= 1 && entry.itemLevel() <= 950
                && entry.quality() >= 0 && entry.quality() <= 20
                && entry.count() >= 1;
    }

    static EquipmentSlotType resolveSlot(String slot) {
        if (slot == null || slot.isBlank()) return null;
        return switch (slot.toLowerCase()) {
            case "head", "helmet" -> EquipmentSlotType.HEAD;
            case "chest", "chestplate" -> EquipmentSlotType.CHEST;
            case "legs", "leggings" -> EquipmentSlotType.LEGS;
            case "feet", "boots" -> EquipmentSlotType.FEET;
            case "mainhand", "weapon", "main_hand" -> EquipmentSlotType.MAINHAND;
            case "offhand", "shield", "off_hand" -> EquipmentSlotType.OFFHAND;
            default -> null;
        };
    }

    public enum EquipmentSlotType {
        HEAD, CHEST, LEGS, FEET, MAINHAND, OFFHAND
    }

    public record DeliveryPlan(
            int inserted,
            int equipped,
            int dropped,
            int failed,
            List<String> failureMessages
    ) {
        public boolean completeSuccess() {
            return failed == 0;
        }

        public int totalItems() {
            return inserted + equipped + dropped + failed;
        }
    }

    public interface InventorySnapshot {
        boolean hasFreeSpace();
        boolean isSlotOccupied(EquipmentSlotType slot);
    }
}
