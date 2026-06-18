package io.github.bysenom.relicwrought.progression;

import com.google.gson.JsonParser;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class CharacterProgressionDefinitionJsonReaderTest {
    private final CharacterProgressionDefinitionJsonReader reader = new CharacterProgressionDefinitionJsonReader();

    @Test
    void readsFullDefinition() {
        var json = JsonParser.parseString("""
                {
                    "id": "test_progression",
                    "minimum_level": 1,
                    "maximum_level": 100,
                    "base_xp": 100,
                    "exponent": 1.65,
                    "data_version": 1
                }
                """).getAsJsonObject();

        var def = reader.read(json, "relicwrought");
        assertEquals(DefinitionKey.parse("test_progression", "relicwrought"), def.id());
        assertEquals(1, def.minimumLevel());
        assertEquals(100, def.maximumLevel());
        assertEquals(100, def.baseXp());
        assertEquals(1.65, def.exponent(), 0.001);
        assertEquals(1, def.dataVersion());
    }

    @Test
    void usesDefaultMinimumAndMaximumLevel() {
        var json = JsonParser.parseString("""
                {
                    "id": "test",
                    "base_xp": 100,
                    "exponent": 1.65
                }
                """).getAsJsonObject();

        var def = reader.read(json, "relicwrought");
        assertEquals(CharacterLevel.MIN, def.minimumLevel());
        assertEquals(CharacterLevel.MAX, def.maximumLevel());
    }

    @Test
    void usesDefaultDataVersion() {
        var json = JsonParser.parseString("""
                {
                    "id": "test",
                    "base_xp": 100,
                    "exponent": 1.65
                }
                """).getAsJsonObject();

        var def = reader.read(json, "relicwrought");
        assertEquals(1, def.dataVersion());
    }

    @Test
    void parsesDecimalExponent() {
        var json = JsonParser.parseString("""
                {
                    "id": "test",
                    "base_xp": 50,
                    "exponent": 2.0
                }
                """).getAsJsonObject();

        var def = reader.read(json, "relicwrought");
        assertEquals(2.0, def.exponent(), 0.001);
    }
}
