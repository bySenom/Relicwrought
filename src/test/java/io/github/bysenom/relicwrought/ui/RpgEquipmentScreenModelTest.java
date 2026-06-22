package io.github.bysenom.relicwrought.ui;

import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStats;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.progression.CharacterAttribute;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

final class RpgEquipmentScreenModelTest {
    @Test
    void allSlotsAppearInStableOrder() {
        RpgEquipmentScreenModel model = emptyModel();

        assertEquals(List.of(
                ArpgEquipmentSlot.HEAD,
                ArpgEquipmentSlot.SHOULDERS,
                ArpgEquipmentSlot.NECK,
                ArpgEquipmentSlot.CLOAK,
                ArpgEquipmentSlot.CHEST,
                ArpgEquipmentSlot.BELT,
                ArpgEquipmentSlot.LEGS,
                ArpgEquipmentSlot.FEET,
                ArpgEquipmentSlot.RING_1,
                ArpgEquipmentSlot.RING_2,
                ArpgEquipmentSlot.TRINKET_1,
                ArpgEquipmentSlot.TRINKET_2,
                ArpgEquipmentSlot.MAIN_HAND,
                ArpgEquipmentSlot.OFF_HAND
        ), model.slots().stream()
                .map(RpgEquipmentScreenModel.SlotView::slot)
                .toList());
    }

    @Test
    void occupiedSlotProvidesItemStackDisplay() {
        ItemStack ring = stack("Starter Ring", 1);
        Map<ArpgEquipmentSlot, ItemStack> equipment = new EnumMap<>(ArpgEquipmentSlot.class);
        equipment.put(ArpgEquipmentSlot.RING_1, ring);

        RpgEquipmentScreenModel.SlotView slot = model(equipment, ItemStack.EMPTY).slots().stream()
                .filter(view -> view.slot() == ArpgEquipmentSlot.RING_1)
                .findFirst()
                .orElseThrow();

        assertTrue(slot.occupied());
        assertEquals("Starter Ring", slot.stack().getHoverName().getString());
        assertTrue(slot.hoverText().stream().anyMatch(component -> component.getString().contains("Starter Ring")));
    }

    @Test
    void emptySlotProvidesPlaceholderAndHoverText() {
        RpgEquipmentScreenModel.SlotView slot = emptyModel().slots().stream()
                .filter(view -> view.slot() == ArpgEquipmentSlot.NECK)
                .findFirst()
                .orElseThrow();

        assertFalse(slot.occupied());
        assertEquals("Nk", slot.placeholder());
        assertEquals(2, slot.hoverText().size());
    }

    @Test
    void selectedItemViewShowsSelectedStack() {
        RpgEquipmentScreenModel model = model(Map.of(), stack("Wooden Sword", 1));

        assertTrue(model.selectedItem().present());
        assertEquals("Wooden Sword", model.selectedItem().displayName().getString());
    }

    @Test
    void selectedItemViewShowsEmptyPlaceholder() {
        RpgEquipmentScreenModel model = model(Map.of(), ItemStack.EMPTY);

        assertFalse(model.selectedItem().present());
        assertFalse(model.selectedItem().displayName().getString().isBlank());
    }

    @Test
    void invalidVanillaSlotInteractionProducesError() {
        RpgEquipmentScreenModel.InteractionPreview preview = emptyModel()
                .previewClick(ArpgEquipmentSlot.HEAD, stack("Helmet", 1));

        assertFalse(preview.allowed());
        assertEquals("ui.relicwrought.inventory.vanilla_slot_read_only", preview.messageKey());
    }

    @Test
    void invalidStackSizeInteractionProducesError() {
        RpgEquipmentScreenModel.InteractionPreview preview = emptyModel()
                .previewClick(ArpgEquipmentSlot.RING_1, stack("Stacked Ring", 2));

        assertFalse(preview.allowed());
        assertEquals("ui.relicwrought.inventory.stack_size_invalid", preview.messageKey());
    }

    @Test
    void validExtraSlotInteractionRequestsServerValidation() {
        RpgEquipmentScreenModel.InteractionPreview preview = emptyModel()
                .previewClick(ArpgEquipmentSlot.RING_1, stack("Starter Ring", 1));

        assertTrue(preview.allowed());
        assertEquals("ui.relicwrought.inventory.request_sent", preview.messageKey());
    }

    @Test
    void statsContainLevelClassAttributesAndCombatValues() {
        RpgEquipmentScreenModel model = RpgEquipmentScreenModel.create(
                Map.of(),
                ItemStack.EMPTY,
                12,
                Map.of(
                        CharacterAttribute.STRENGTH, 8,
                        CharacterAttribute.DEXTERITY, 7,
                        CharacterAttribute.INTELLIGENCE, 6,
                        CharacterAttribute.VITALITY, 5
                ),
                new CharacterCombatStats(
                        0, 0, 0, 0, 0,
                        0.25, 0,
                        0, 0.10, 1.5,
                        0, 0,
                        42, 120, 0, 0, 0, 0,
                        0, 0,
                        0, 0, 0
                )
        );

        List<String> labels = model.stats().stream().map(RpgEquipmentScreenModel.StatLine::labelKey).toList();
        assertTrue(labels.containsAll(List.of(
                "ui.relicwrought.inventory.stat.class",
                "ui.relicwrought.inventory.stat.level",
                "ui.relicwrought.inventory.stat.strength",
                "ui.relicwrought.inventory.stat.dexterity",
                "ui.relicwrought.inventory.stat.intelligence",
                "ui.relicwrought.inventory.stat.vitality",
                "ui.relicwrought.inventory.stat.damage",
                "ui.relicwrought.inventory.stat.crit_chance",
                "ui.relicwrought.inventory.stat.armor",
                "ui.relicwrought.inventory.stat.life"
        )));
        assertTrue(model.stats().stream().anyMatch(line -> line.value().getString().equals("12")));
        assertTrue(model.stats().stream().anyMatch(line -> line.value().getString().equals("8")));
        assertTrue(model.stats().stream().anyMatch(line -> line.value().getString().equals("+25.0%")));
        assertTrue(model.stats().stream().anyMatch(line -> line.value().getString().equals("42.0")));
    }

    @Test
    void modelContainsNoCraftingComponents() {
        assertFalse(emptyModel().hasCraftingComponents());
    }

    private static RpgEquipmentScreenModel emptyModel() {
        return model(Map.of(), ItemStack.EMPTY);
    }

    private static RpgEquipmentScreenModel model(Map<ArpgEquipmentSlot, ItemStack> equipment, ItemStack selected) {
        return RpgEquipmentScreenModel.create(
                equipment,
                selected,
                1,
                Map.of(),
                CharacterCombatStats.empty()
        );
    }

    private static ItemStack stack(String name, int count) {
        ItemStack stack = mock(ItemStack.class);
        when(stack.isEmpty()).thenReturn(false);
        when(stack.getCount()).thenReturn(count);
        when(stack.getHoverName()).thenReturn(Component.literal(name));
        when(stack.copy()).thenReturn(stack);
        return stack;
    }
}
