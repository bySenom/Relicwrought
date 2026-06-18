package io.github.bysenom.relicwrought.player;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class StarterKitDeliveryPlannerTest {

    private static final DefinitionKey KIT_ID = DefinitionKey.parse("test_kit", "relicwrought");

    @Test
    void freeInventoryInsertsAllItems() {
        StarterKitDefinition kit = kit(
                entry("relicwrought:starter_pickaxe", 1, 0, 1, false, ""),
                entry("relicwrought:starter_axe", 1, 0, 1, false, ""),
                entry("relicwrought:starter_shovel", 1, 0, 1, false, "")
        );
        var plan = StarterKitDeliveryPlanner.planDelivery(kit, freeInventory());
        assertTrue(plan.completeSuccess());
        assertEquals(3, plan.inserted());
        assertEquals(0, plan.equipped());
        assertEquals(0, plan.dropped());
        assertEquals(0, plan.failed());
    }

    @Test
    void fullInventoryDropsAllItems() {
        StarterKitDefinition kit = kit(
                entry("relicwrought:starter_pickaxe", 1, 0, 1, false, "")
        );
        var plan = StarterKitDeliveryPlanner.planDelivery(kit, fullInventory());
        assertEquals(0, plan.inserted());
        assertEquals(0, plan.equipped());
        assertEquals(1, plan.dropped());
        assertTrue(plan.completeSuccess());
    }

    @Test
    void emptyEquipSlotEquipsItem() {
        StarterKitDefinition kit = kit(
                entry("relicwrought:starter_training_sword", 1, 0, 1, true, "mainhand")
        );
        var plan = StarterKitDeliveryPlanner.planDelivery(kit, freeInventory());
        assertEquals(0, plan.inserted());
        assertEquals(1, plan.equipped());
        assertEquals(0, plan.dropped());
    }

    @Test
    void occupiedEquipSlotInsertsInstead() {
        StarterKitDefinition kit = kit(
                entry("relicwrought:starter_training_sword", 1, 0, 1, true, "mainhand")
        );
        var plan = StarterKitDeliveryPlanner.planDelivery(kit, occupiedMainhand());
        assertEquals(1, plan.inserted());
        assertEquals(0, plan.equipped());
        assertEquals(0, plan.dropped());
    }

    @Test
    void occupiedEquipSlotWithFullInventoryDrops() {
        StarterKitDefinition kit = kit(
                entry("relicwrought:starter_training_sword", 1, 0, 1, true, "mainhand")
        );
        var plan = StarterKitDeliveryPlanner.planDelivery(kit, occupiedMainhandAndFull());
        assertEquals(0, plan.inserted());
        assertEquals(0, plan.equipped());
        assertEquals(1, plan.dropped());
    }

    @Test
    void unknownBaseEntryIsStillCounted() {
        StarterKitDefinition kit = kit(
                entry("relicwrought:nonexistent_item", 1, 0, 1, false, "")
        );
        var plan = StarterKitDeliveryPlanner.planDelivery(kit, freeInventory());
        assertEquals(1, plan.totalItems());
        assertEquals(1, plan.inserted());
        assertTrue(plan.completeSuccess());
    }

    @Test
    void countsMatchTotalEntries() {
        StarterKitDefinition kit = kit(
                entry("relicwrought:a", 1, 0, 1, false, ""),
                entry("relicwrought:b", 1, 0, 1, true, "mainhand"),
                entry("relicwrought:c", 1, 0, 1, false, "")
        );
        var plan = StarterKitDeliveryPlanner.planDelivery(kit, occupiedMainhand());
        assertEquals(3, plan.totalItems());
        assertEquals(3, plan.inserted() + plan.dropped() + plan.equipped());
        assertEquals(3, plan.inserted());
        assertEquals(0, plan.equipped());
    }

    @Test
    void autoEquipWithUnknownSlotInserts() {
        StarterKitDefinition kit = kit(
                entry("relicwrought:test", 1, 0, 1, true, "unknown_slot")
        );
        var plan = StarterKitDeliveryPlanner.planDelivery(kit, freeInventory());
        assertEquals(1, plan.inserted());
        assertEquals(0, plan.equipped());
    }

    @Test
    void autoEquipWithUnknownSlotAndFullDrops() {
        StarterKitDefinition kit = kit(
                entry("relicwrought:test", 1, 0, 1, true, "unknown_slot")
        );
        var plan = StarterKitDeliveryPlanner.planDelivery(kit, fullInventory());
        assertEquals(0, plan.inserted());
        assertEquals(0, plan.equipped());
        assertEquals(1, plan.dropped());
    }

    private static StarterKitEntry entry(String baseId, int level, int quality, int count, boolean autoEquip, String slot) {
        return new StarterKitEntry(baseId, level, quality, count, autoEquip, slot);
    }

    private static StarterKitDefinition kit(StarterKitEntry... entries) {
        return new StarterKitDefinition(KIT_ID, List.of(entries), 1);
    }

    private static StarterKitDeliveryPlanner.InventorySnapshot freeInventory() {
        return new StarterKitDeliveryPlanner.InventorySnapshot() {
            @Override public boolean hasFreeSpace() { return true; }
            @Override public boolean isSlotOccupied(StarterKitDeliveryPlanner.EquipmentSlotType slot) { return false; }
        };
    }

    private static StarterKitDeliveryPlanner.InventorySnapshot fullInventory() {
        return new StarterKitDeliveryPlanner.InventorySnapshot() {
            @Override public boolean hasFreeSpace() { return false; }
            @Override public boolean isSlotOccupied(StarterKitDeliveryPlanner.EquipmentSlotType slot) { return false; }
        };
    }

    private static StarterKitDeliveryPlanner.InventorySnapshot occupiedMainhand() {
        return new StarterKitDeliveryPlanner.InventorySnapshot() {
            @Override public boolean hasFreeSpace() { return true; }
            @Override public boolean isSlotOccupied(StarterKitDeliveryPlanner.EquipmentSlotType slot) {
                return slot == StarterKitDeliveryPlanner.EquipmentSlotType.MAINHAND;
            }
        };
    }

    private static StarterKitDeliveryPlanner.InventorySnapshot occupiedMainhandAndFull() {
        return new StarterKitDeliveryPlanner.InventorySnapshot() {
            @Override public boolean hasFreeSpace() { return false; }
            @Override public boolean isSlotOccupied(StarterKitDeliveryPlanner.EquipmentSlotType slot) {
                return slot == StarterKitDeliveryPlanner.EquipmentSlotType.MAINHAND;
            }
        };
    }
}
