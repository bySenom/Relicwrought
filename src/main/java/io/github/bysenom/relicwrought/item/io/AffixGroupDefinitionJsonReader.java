package io.github.bysenom.relicwrought.item.io;

import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.item.model.AffixGroupDefinition;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;

public final class AffixGroupDefinitionJsonReader implements DefinitionJsonReader<AffixGroupDefinition> {
    @Override
    public AffixGroupDefinition read(JsonObject json, String defaultNamespace) {
        return new AffixGroupDefinition(
                DefinitionKey.parse(JsonReaderSupport.requiredString(json, "id"), defaultNamespace),
                JsonReaderSupport.optionalInt(json, "max_per_item", 1),
                JsonReaderSupport.keySet(json, "conflicts", defaultNamespace),
                JsonReaderSupport.stringSet(json, "tags"),
                JsonReaderSupport.requiredInt(json, "data_version")
        );
    }
}
