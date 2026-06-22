package io.github.bysenom.relicwrought.item.model;

import net.minecraft.world.entity.EquipmentSlot;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

final class ArpgEquipmentSlotTest {
    @Test
    void containsPhaseEightSixSlots() {
        assertEquals(14, ArpgEquipmentSlot.values().length);
        assertEquals(8, ArpgEquipmentSlot.extraSlots().size());
        assertTrue(Set.of(ArpgEquipmentSlot.values()).containsAll(Set.of(
                ArpgEquipmentSlot.HEAD,
                ArpgEquipmentSlot.NECK,
                ArpgEquipmentSlot.SHOULDERS,
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
        )));
    }

    @Test
    void vanillaSlotsMapToMinecraftEquipmentSlots() {
        assertEquals(EquipmentSlot.HEAD, ArpgEquipmentSlot.HEAD.vanillaSlot().orElseThrow());
        assertEquals(EquipmentSlot.CHEST, ArpgEquipmentSlot.CHEST.vanillaSlot().orElseThrow());
        assertEquals(EquipmentSlot.LEGS, ArpgEquipmentSlot.LEGS.vanillaSlot().orElseThrow());
        assertEquals(EquipmentSlot.FEET, ArpgEquipmentSlot.FEET.vanillaSlot().orElseThrow());
        assertEquals(EquipmentSlot.MAINHAND, ArpgEquipmentSlot.MAIN_HAND.vanillaSlot().orElseThrow());
        assertEquals(EquipmentSlot.OFFHAND, ArpgEquipmentSlot.OFF_HAND.vanillaSlot().orElseThrow());
    }

    @Test
    void extraSlotsAreNotVanillaMapped() {
        Set<ArpgEquipmentSlot> extras = ArpgEquipmentSlot.extraSlots().stream().collect(Collectors.toSet());

        assertTrue(extras.contains(ArpgEquipmentSlot.NECK));
        assertTrue(extras.contains(ArpgEquipmentSlot.RING_1));
        assertTrue(extras.contains(ArpgEquipmentSlot.RING_2));
        assertTrue(extras.contains(ArpgEquipmentSlot.TRINKET_1));
        assertTrue(extras.contains(ArpgEquipmentSlot.TRINKET_2));
        assertTrue(extras.stream().noneMatch(ArpgEquipmentSlot::isVanillaMappedSlot));
    }

    @Test
    void serializedNamesAreStable() {
        assertEquals("ring_1", ArpgEquipmentSlot.RING_1.serializedName());
        assertEquals(ArpgEquipmentSlot.TRINKET_2, ArpgEquipmentSlot.parseSerialized("trinket_2"));
        assertEquals("ui.relicwrought.equipment.neck", ArpgEquipmentSlot.NECK.translationKey());
    }
}
