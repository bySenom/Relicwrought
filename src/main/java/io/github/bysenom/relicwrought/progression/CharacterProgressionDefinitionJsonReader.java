package io.github.bysenom.relicwrought.progression;

import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.item.io.DefinitionJsonReader;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;

public final class CharacterProgressionDefinitionJsonReader implements DefinitionJsonReader<CharacterProgressionDefinition> {
    @Override
    public CharacterProgressionDefinition read(JsonObject json, String defaultNamespace) {
        String id = json.get("id").getAsString();
        int minimumLevel = json.has("minimum_level") ? json.get("minimum_level").getAsInt() : CharacterLevel.MIN;
        int maximumLevel = json.has("maximum_level") ? json.get("maximum_level").getAsInt() : CharacterLevel.MAX;
        double baseXp = json.get("base_xp").getAsDouble();
        double exponent = json.get("exponent").getAsDouble();
        int dataVersion = json.has("data_version") ? json.get("data_version").getAsInt() : 1;

        return new CharacterProgressionDefinition(
                DefinitionKey.parse(id, defaultNamespace),
                minimumLevel, maximumLevel, baseXp, exponent, dataVersion
        );
    }
}
