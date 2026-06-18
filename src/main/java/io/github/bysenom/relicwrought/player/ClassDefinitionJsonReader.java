package io.github.bysenom.relicwrought.player;

import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.item.io.DefinitionJsonReader;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;

public final class ClassDefinitionJsonReader implements DefinitionJsonReader<ClassDefinition> {
    @Override
    public ClassDefinition read(JsonObject json, String defaultNamespace) {
        String id = json.get("id").getAsString();
        String displayName = json.get("display_name").getAsString();
        String description = json.get("description").getAsString();
        String starterKit = json.get("starter_kit").getAsString();
        int sortOrder = json.has("sort_order") ? json.get("sort_order").getAsInt() : 0;
        boolean enabled = !json.has("enabled") || json.get("enabled").getAsBoolean();
        int dataVersion = json.has("data_version") ? json.get("data_version").getAsInt() : 1;

        return new ClassDefinition(
                DefinitionKey.parse(id, defaultNamespace),
                displayName, description, starterKit,
                sortOrder, enabled, dataVersion
        );
    }
}
