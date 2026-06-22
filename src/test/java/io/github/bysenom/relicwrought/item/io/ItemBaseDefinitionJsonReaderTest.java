package io.github.bysenom.relicwrought.item.io;

import com.google.gson.JsonParser;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.item.model.ItemCategory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class ItemBaseDefinitionJsonReaderTest {
    @Test
    void readsExistingValidSlotsField() {
        var definition = new ItemBaseDefinitionJsonReader().read(JsonParser.parseString("""
                {
                  "id": "starter_ring",
                  "translation_key": "item_base.relicwrought.starter_ring",
                  "minecraft_item": "minecraft:gold_nugget",
                  "category": "ring",
                  "valid_slots": ["ring_1", "ring_2"],
                  "base_stats": {},
                  "implicit_affixes": [],
                  "affix_tags": ["jewelry"],
                  "loot_sources": [],
                  "data_version": 1
                }
                """).getAsJsonObject(), "relicwrought");

        assertEquals(ItemCategory.RING, definition.category());
        assertEquals(Set.of(ArpgEquipmentSlot.RING_1, ArpgEquipmentSlot.RING_2), definition.validSlots());
    }

    @Test
    void readsAllowedEquipmentSlotsAlias() {
        var definition = new ItemBaseDefinitionJsonReader().read(JsonParser.parseString("""
                {
                  "id": "starter_necklace",
                  "translation_key": "item_base.relicwrought.starter_necklace",
                  "minecraft_item": "minecraft:emerald",
                  "category": "necklace",
                  "allowed_equipment_slots": ["neck"],
                  "base_stats": {},
                  "implicit_affixes": [],
                  "affix_tags": ["jewelry"],
                  "loot_sources": [],
                  "data_version": 1
                }
                """).getAsJsonObject(), "relicwrought");

        assertEquals(Set.of(ArpgEquipmentSlot.NECK), definition.validSlots());
    }
}
