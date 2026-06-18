package io.github.bysenom.relicwrought.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.item.io.DefinitionJsonReader;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;

import java.util.ArrayList;
import java.util.List;

public final class StarterKitDefinitionJsonReader implements DefinitionJsonReader<StarterKitDefinition> {
    @Override
    public StarterKitDefinition read(JsonObject json, String defaultNamespace) {
        String id = json.get("id").getAsString();
        int dataVersion = json.has("data_version") ? json.get("data_version").getAsInt() : 1;

        JsonArray entriesArray = json.getAsJsonArray("entries");
        List<StarterKitEntry> entries = new ArrayList<>();
        for (var elem : entriesArray) {
            JsonObject entryObj = elem.getAsJsonObject();
            String itemBase = entryObj.get("item_base").getAsString();
            int itemLevel = entryObj.get("item_level").getAsInt();
            int quality = entryObj.has("quality") ? entryObj.get("quality").getAsInt() : 0;
            int count = entryObj.has("count") ? entryObj.get("count").getAsInt() : 1;
            boolean autoEquip = entryObj.has("auto_equip") && entryObj.get("auto_equip").getAsBoolean();
            String slot = entryObj.has("slot") ? entryObj.get("slot").getAsString() : "";

            entries.add(new StarterKitEntry(itemBase, itemLevel, quality, count, autoEquip, slot));
        }

        return new StarterKitDefinition(
                DefinitionKey.parse(id, defaultNamespace),
                entries,
                dataVersion
        );
    }
}
