package io.github.bysenom.relicwrought.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class StarterKitDefinitionJsonReaderTest {
    private final StarterKitDefinitionJsonReader reader = new StarterKitDefinitionJsonReader();

    @Test
    void readsWarriorStarterKit() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "warrior_starter");
        json.addProperty("data_version", 1);
        JsonArray entries = new JsonArray();

        JsonObject sword = new JsonObject();
        sword.addProperty("item_base", "relicwrought:starter_training_sword");
        sword.addProperty("item_level", 1);
        sword.addProperty("quality", 0);
        sword.addProperty("count", 1);
        sword.addProperty("auto_equip", true);
        sword.addProperty("slot", "mainhand");
        entries.add(sword);

        JsonObject shield = new JsonObject();
        shield.addProperty("item_base", "relicwrought:starter_shield");
        shield.addProperty("item_level", 1);
        shield.addProperty("quality", 0);
        shield.addProperty("count", 1);
        shield.addProperty("auto_equip", true);
        shield.addProperty("slot", "offhand");
        entries.add(shield);

        json.add("entries", entries);

        StarterKitDefinition kit = reader.read(json, Relicwrought.MOD_ID);
        assertEquals(DefinitionKey.parse("warrior_starter", Relicwrought.MOD_ID), kit.id());
        assertEquals(2, kit.entries().size());
        assertEquals(1, kit.dataVersion());

        StarterKitEntry first = kit.entries().getFirst();
        assertEquals("relicwrought:starter_training_sword", first.itemBaseId());
        assertTrue(first.autoEquip());
        assertEquals("mainhand", first.slot());
    }

    @Test
    void defaultsQualityToZero() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "test_kit");
        json.addProperty("data_version", 1);
        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("item_base", "relicwrought:test");
        entry.addProperty("item_level", 1);
        entry.addProperty("count", 1);
        entries.add(entry);
        json.add("entries", entries);

        StarterKitDefinition kit = reader.read(json, Relicwrought.MOD_ID);
        assertEquals(0, kit.entries().getFirst().quality());
    }

    @Test
    void defaultsCountToOne() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "test_kit");
        json.addProperty("data_version", 1);
        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("item_base", "relicwrought:test");
        entry.addProperty("item_level", 1);
        entry.addProperty("quality", 0);
        entries.add(entry);
        json.add("entries", entries);

        StarterKitDefinition kit = reader.read(json, Relicwrought.MOD_ID);
        assertEquals(1, kit.entries().getFirst().count());
    }

    @Test
    void defaultsAutoEquipToFalse() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "test_kit");
        json.addProperty("data_version", 1);
        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("item_base", "relicwrought:test");
        entry.addProperty("item_level", 1);
        entry.addProperty("quality", 0);
        entry.addProperty("count", 1);
        entries.add(entry);
        json.add("entries", entries);

        StarterKitDefinition kit = reader.read(json, Relicwrought.MOD_ID);
        assertFalse(kit.entries().getFirst().autoEquip());
    }

    @Test
    void defaultsSlotToEmpty() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "test_kit");
        json.addProperty("data_version", 1);
        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("item_base", "relicwrought:test");
        entry.addProperty("item_level", 1);
        entry.addProperty("quality", 0);
        entry.addProperty("count", 1);
        entries.add(entry);
        json.add("entries", entries);

        StarterKitDefinition kit = reader.read(json, Relicwrought.MOD_ID);
        assertEquals("", kit.entries().getFirst().slot());
    }

    @Test
    void defaultsDataVersionToOne() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "test_kit");
        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("item_base", "relicwrought:test");
        entry.addProperty("item_level", 1);
        entry.addProperty("quality", 0);
        entry.addProperty("count", 1);
        entries.add(entry);
        json.add("entries", entries);

        StarterKitDefinition kit = reader.read(json, Relicwrought.MOD_ID);
        assertEquals(1, kit.dataVersion());
    }

    @Test
    void ignoresUnknownFields() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "test_kit");
        json.addProperty("data_version", 1);
        json.addProperty("unknown_field", "ignored");
        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("item_base", "relicwrought:test");
        entry.addProperty("item_level", 1);
        entry.addProperty("quality", 0);
        entry.addProperty("count", 1);
        entries.add(entry);
        json.add("entries", entries);

        assertDoesNotThrow(() -> reader.read(json, Relicwrought.MOD_ID));
    }
}
