package io.github.bysenom.relicwrought.player;

import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ClassDefinitionJsonReaderTest {
    private final ClassDefinitionJsonReader reader = new ClassDefinitionJsonReader();

    @Test
    void readsValidWarrior() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "warrior");
        json.addProperty("display_name", "class.relicwrought.warrior");
        json.addProperty("description", "class.relicwrought.warrior.description");
        json.addProperty("starter_kit", "relicwrought:warrior_starter");
        json.addProperty("sort_order", 0);
        json.addProperty("enabled", true);
        json.addProperty("data_version", 1);

        ClassDefinition def = reader.read(json, Relicwrought.MOD_ID);
        assertEquals(DefinitionKey.parse("warrior", Relicwrought.MOD_ID), def.id());
        assertEquals("class.relicwrought.warrior", def.displayNameKey());
        assertTrue(def.enabled());
    }

    @Test
    void defaultsSortOrderToZero() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "rogue");
        json.addProperty("display_name", "class.relicwrought.rogue");
        json.addProperty("description", "desc");
        json.addProperty("starter_kit", "relicwrought:rogue_starter");
        json.addProperty("data_version", 1);

        ClassDefinition def = reader.read(json, Relicwrought.MOD_ID);
        assertEquals(0, def.sortOrder());
    }

    @Test
    void defaultsEnabledToTrue() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "rogue");
        json.addProperty("display_name", "class.relicwrought.rogue");
        json.addProperty("description", "desc");
        json.addProperty("starter_kit", "relicwrought:rogue_starter");
        json.addProperty("data_version", 1);

        ClassDefinition def = reader.read(json, Relicwrought.MOD_ID);
        assertTrue(def.enabled());
    }

    @Test
    void readsDisabledClass() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "disabled_test");
        json.addProperty("display_name", "class.relicwrought.disabled");
        json.addProperty("description", "desc");
        json.addProperty("starter_kit", "relicwrought:kit");
        json.addProperty("enabled", false);
        json.addProperty("data_version", 1);

        ClassDefinition def = reader.read(json, Relicwrought.MOD_ID);
        assertFalse(def.enabled());
    }

    @Test
    void readsDescription() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "arcanist");
        json.addProperty("display_name", "class.relicwrought.arcanist");
        json.addProperty("description", "class.relicwrought.arcanist.description");
        json.addProperty("starter_kit", "relicwrought:arcanist_starter");
        json.addProperty("data_version", 1);

        ClassDefinition def = reader.read(json, Relicwrought.MOD_ID);
        assertEquals("class.relicwrought.arcanist.description", def.descriptionKey());
    }

    @Test
    void ignoresUnknownFields() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "test");
        json.addProperty("display_name", "name");
        json.addProperty("description", "desc");
        json.addProperty("starter_kit", "relicwrought:kit");
        json.addProperty("data_version", 1);
        json.addProperty("unknown_field", "ignored");
        json.addProperty("another_unknown", 42);

        assertDoesNotThrow(() -> reader.read(json, Relicwrought.MOD_ID));
    }
}
