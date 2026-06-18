package io.github.bysenom.relicwrought.item.generation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.item.io.DefinitionJsonReader;
import io.github.bysenom.relicwrought.item.io.JsonReaderSupport;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;

import java.util.ArrayList;
import java.util.List;

public final class RarityDefinitionJsonReader implements DefinitionJsonReader<RarityDefinition> {
    @Override
    public RarityDefinition read(JsonObject json, String defaultNamespace) {
        DefinitionKey id = DefinitionKey.parse(JsonReaderSupport.requiredString(json, "id"), defaultNamespace);
        List<RarityDefinition.AllowedAffixCount> counts = readAllowedAffixCounts(json);
        return new RarityDefinition(
                id,
                JsonReaderSupport.optionalString(json, "display_name", "rarity." + defaultNamespace + "." + id.path()),
                JsonReaderSupport.optionalInt(json, "weight", 0),
                JsonReaderSupport.optionalInt(json, "minimum_item_level", 1),
                JsonReaderSupport.optionalInt(json, "maximum_item_level", 0),
                counts,
                JsonReaderSupport.optionalInt(json, "display_order", 0),
                JsonReaderSupport.optionalString(json, "color", "#FFFFFF"),
                JsonReaderSupport.requiredInt(json, "data_version")
        );
    }

    private static List<RarityDefinition.AllowedAffixCount> readAllowedAffixCounts(JsonObject json) {
        JsonArray array = JsonReaderSupport.optionalArray(json, "allowed_affix_counts");
        List<RarityDefinition.AllowedAffixCount> counts = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();
            counts.add(new RarityDefinition.AllowedAffixCount(
                    JsonReaderSupport.requiredInt(obj, "prefixes"),
                    JsonReaderSupport.requiredInt(obj, "suffixes"),
                    JsonReaderSupport.requiredInt(obj, "weight")
            ));
        }
        return counts;
    }
}
