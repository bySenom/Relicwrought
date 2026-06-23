package io.github.bysenom.relicwrought.ability;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.combat.damage.DamageType;
import io.github.bysenom.relicwrought.item.io.DefinitionJsonReader;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;

import java.util.HashSet;
import java.util.Set;

public class AbilityDefinitionJsonReader implements DefinitionJsonReader<AbilityDefinition> {
    @Override
    public AbilityDefinition read(JsonObject json, String defaultNamespace) {
        if (json == null) throw new IllegalArgumentException("Ability JSON must not be null");

        String idStr = getString(json, "id");
        if (idStr == null || idStr.isBlank()) throw new IllegalArgumentException("Ability 'id' is required");
        DefinitionKey id = DefinitionKey.parse(idStr, defaultNamespace);

        String translationKey = getString(json, "translationKey");
        if (translationKey == null || translationKey.isBlank())
            throw new IllegalArgumentException("Ability " + id + " missing 'translationKey'");

        String descriptionKey = getString(json, "descriptionTranslationKey");
        if (descriptionKey == null) descriptionKey = translationKey + ".description";

        String icon = getString(json, "icon");
        if (icon == null || icon.isBlank()) icon = "relicwrought:textures/gui/abilities/placeholder.png";

        Set<String> allowedClasses = new HashSet<>();
        if (json.has("allowedClasses") && json.get("allowedClasses").isJsonArray()) {
            for (JsonElement el : json.getAsJsonArray("allowedClasses")) {
                allowedClasses.add(el.getAsString());
            }
        }
        if (allowedClasses.isEmpty())
            throw new IllegalArgumentException("Ability " + id + " must have at least one allowed class");

        AbilityResourceType resourceType;
        try {
            resourceType = AbilityResourceType.valueOf(getString(json, "resourceType", "NONE").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ability " + id + " has invalid resourceType");
        }

        double resourceCost = getDouble(json, "resourceCost", 0.0);
        int cooldownTicks = getInt(json, "cooldownTicks", 0);

        AbilityTargetingType targetingType;
        try {
            targetingType = AbilityTargetingType.valueOf(getString(json, "targetingType", "SELF").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ability " + id + " has invalid targetingType");
        }

        AbilityEffectType effectType;
        try {
            effectType = AbilityEffectType.valueOf(getString(json, "effectType", "DAMAGE").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ability " + id + " has invalid effectType");
        }

        DamageType damageType = DamageType.PHYSICAL;
        String damageTypeStr = getString(json, "damageType");
        if (damageTypeStr != null && !damageTypeStr.isBlank()) {
            try {
                damageType = DamageType.valueOf(damageTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Ability " + id + " has invalid damageType: " + damageTypeStr);
            }
        } else if (effectType == AbilityEffectType.HEAL) {
            damageType = null;
        }

        double basePower = getDouble(json, "basePower", 1.0);
        double scaling = getDouble(json, "scaling", 1.0);
        int range = getInt(json, "range", 4);
        int radius = getInt(json, "radius", 0);
        int dataVersion = getInt(json, "dataVersion", 1);

        return new AbilityDefinition(id, translationKey, descriptionKey, icon,
                allowedClasses, resourceType, resourceCost, cooldownTicks,
                targetingType, effectType, damageType, basePower, scaling, range, radius, dataVersion);
    }

    private static String getString(JsonObject json, String key) {
        return json.has(key) ? json.get(key).getAsString() : null;
    }

    private static String getString(JsonObject json, String key, String fallback) {
        return json.has(key) ? json.get(key).getAsString() : fallback;
    }

    private static double getDouble(JsonObject json, String key, double fallback) {
        return json.has(key) ? json.get(key).getAsDouble() : fallback;
    }

    private static int getInt(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }
}
