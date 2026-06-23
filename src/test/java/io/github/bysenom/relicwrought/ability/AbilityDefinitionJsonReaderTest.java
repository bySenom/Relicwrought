package io.github.bysenom.relicwrought.ability;

import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.combat.damage.DamageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbilityDefinitionJsonReaderTest {

    private final AbilityDefinitionJsonReader reader = new AbilityDefinitionJsonReader();

    private JsonObject base(String id, String effectType, String... extra) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("translationKey", "ability.test." + id);
        com.google.gson.JsonArray classes = new com.google.gson.JsonArray();
        classes.add("warrior");
        obj.add("allowedClasses", classes);
        obj.addProperty("effectType", effectType);
        for (int i = 0; i < extra.length - 1; i += 2) {
            obj.addProperty(extra[i], extra[i + 1]);
        }
        return obj;
    }

    @Test
    void damageType_physical_parsedFromJson() {
        JsonObject json = base("test:strike", "DAMAGE", "damageType", "PHYSICAL");
        AbilityDefinition def = reader.read(json, "test");
        assertEquals(DamageType.PHYSICAL, def.damageType());
    }

    @Test
    void damageType_fire_parsedFromJson() {
        JsonObject json = base("test:fire_bolt", "DAMAGE", "damageType", "FIRE");
        AbilityDefinition def = reader.read(json, "test");
        assertEquals(DamageType.FIRE, def.damageType());
    }

    @Test
    void damageType_caseInsensitive() {
        JsonObject json = base("test:strike", "DAMAGE", "damageType", "physical");
        AbilityDefinition def = reader.read(json, "test");
        assertEquals(DamageType.PHYSICAL, def.damageType());
    }

    @Test
    void damageType_defaultsToPhysical_whenOmittedOnDamageAbility() {
        JsonObject json = base("test:strike", "DAMAGE");
        AbilityDefinition def = reader.read(json, "test");
        assertEquals(DamageType.PHYSICAL, def.damageType());
    }

    @Test
    void damageType_nullForHealAbility() {
        JsonObject json = base("test:heal", "HEAL");
        AbilityDefinition def = reader.read(json, "test");
        assertNull(def.damageType());
    }

    @Test
    void damageType_invalidValue_throwsIllegalArgumentException() {
        JsonObject json = base("test:strike", "DAMAGE", "damageType", "RAINBOW");
        assertThrows(IllegalArgumentException.class, () -> reader.read(json, "test"));
    }

    @Test
    void missingId_throws() {
        JsonObject json = new JsonObject();
        assertThrows(IllegalArgumentException.class, () -> reader.read(json, "test"));
    }

    @Test
    void missingAllowedClasses_throws() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "test:strike");
        json.addProperty("translationKey", "ability.test.strike");
        json.addProperty("effectType", "DAMAGE");
        assertThrows(IllegalArgumentException.class, () -> reader.read(json, "test"));
    }

    @Test
    void fullDefinition_loadedCorrectly() {
        JsonObject json = base("test:strike", "DAMAGE", "damageType", "PHYSICAL");
        json.addProperty("resourceType", "RAGE");
        json.addProperty("resourceCost", "25.0");
        json.addProperty("cooldownTicks", "80");
        json.addProperty("basePower", "8.0");
        json.addProperty("scaling", "1.2");
        json.addProperty("range", "4");
        AbilityDefinition def = reader.read(json, "test");

        assertEquals("test", def.id().namespace());
        assertEquals("strike", def.id().path());
        assertEquals(AbilityEffectType.DAMAGE, def.effectType());
        assertEquals(DamageType.PHYSICAL, def.damageType());
        assertEquals(25.0, def.resourceCost());
        assertEquals(80, def.cooldownTicks());
        assertEquals(8.0, def.basePower());
    }
}
