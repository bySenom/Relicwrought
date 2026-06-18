package io.github.bysenom.relicwrought.item.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.item.model.AffixComponentDefinition;
import io.github.bysenom.relicwrought.item.model.AffixDefinition;
import io.github.bysenom.relicwrought.item.model.AffixOperation;
import io.github.bysenom.relicwrought.item.model.AffixScope;
import io.github.bysenom.relicwrought.item.model.AffixTier;
import io.github.bysenom.relicwrought.item.model.AffixTierDefinition;
import io.github.bysenom.relicwrought.item.model.AffixType;
import io.github.bysenom.relicwrought.item.model.AffixValueRange;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemCategory;
import io.github.bysenom.relicwrought.item.scaling.RoundingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class AffixDefinitionJsonReader implements DefinitionJsonReader<AffixDefinition> {
    @Override
    public AffixDefinition read(JsonObject json, String defaultNamespace) {
        String fallbackCalculation = JsonReaderSupport.optionalString(json, "calculation", "additive");
        AffixScope fallbackScope = AffixScope.valueOf(JsonReaderSupport.optionalString(json, "scope", "global").toUpperCase());
        List<AffixComponentDefinition> components = readComponents(json, fallbackScope, AffixOperation.parse(fallbackCalculation));
        List<AffixTierDefinition> tiers = new ArrayList<>();
        for (JsonElement tierElement : JsonReaderSupport.optionalArray(json, "tiers")) {
            JsonObject tier = tierElement.getAsJsonObject();
            List<AffixValueRange> values = readValues(tier);
            tiers.add(new AffixTierDefinition(
                    AffixTier.valueOf(JsonReaderSupport.requiredString(tier, "tier").toUpperCase()),
                    JsonReaderSupport.requiredInt(tier, "minimum_item_level"),
                    JsonReaderSupport.optionalInt(tier, "weight", 100),
                    RoundingStrategy.parse(JsonReaderSupport.optionalString(tier, "rounding", "none")),
                    values
            ));
        }

        return new AffixDefinition(
                DefinitionKey.parse(JsonReaderSupport.requiredString(json, "id"), defaultNamespace),
                JsonReaderSupport.requiredString(json, "translation_key"),
                AffixType.valueOf(JsonReaderSupport.requiredString(json, "type").toUpperCase()),
                readGroups(json, defaultNamespace),
                JsonReaderSupport.enumSet(json, "valid_item_categories", ItemCategory::valueOf),
                components,
                JsonReaderSupport.requiredInt(json, "weight"),
                tiers,
                JsonReaderSupport.keySet(json, "conflict_groups", defaultNamespace),
                JsonReaderSupport.stringSet(json, "tags"),
                JsonReaderSupport.stringSet(json, "required_tags_any"),
                JsonReaderSupport.stringSet(json, "required_tags_all"),
                JsonReaderSupport.stringSet(json, "excluded_tags"),
                fallbackCalculation,
                JsonReaderSupport.requiredInt(json, "data_version")
        );
    }

    private static Set<DefinitionKey> readGroups(JsonObject json, String defaultNamespace) {
        Set<DefinitionKey> groups = JsonReaderSupport.keySet(json, "groups", defaultNamespace);
        if (!groups.isEmpty()) {
            return groups;
        }
        return Set.of(DefinitionKey.parse(JsonReaderSupport.requiredString(json, "group"), defaultNamespace));
    }

    private static List<AffixComponentDefinition> readComponents(
            JsonObject json,
            AffixScope fallbackScope,
            AffixOperation fallbackOperation
    ) {
        List<AffixComponentDefinition> components = new ArrayList<>();
        for (JsonElement element : JsonReaderSupport.optionalArray(json, "components")) {
            JsonObject component = element.getAsJsonObject();
            components.add(new AffixComponentDefinition(
                    JsonReaderSupport.requiredString(component, "stat"),
                    AffixScope.valueOf(JsonReaderSupport.optionalString(component, "scope", fallbackScope.name()).toUpperCase()),
                    AffixOperation.parse(JsonReaderSupport.optionalString(component, "operation", fallbackOperation.name()))
            ));
        }
        if (components.isEmpty()) {
            components.add(new AffixComponentDefinition(
                    JsonReaderSupport.optionalString(json, "stat", "generic"),
                    fallbackScope,
                    fallbackOperation
            ));
        }
        return components;
    }

    private static List<AffixValueRange> readValues(JsonObject tier) {
        List<AffixValueRange> values = new ArrayList<>();
        for (JsonElement element : JsonReaderSupport.optionalArray(tier, "values")) {
            JsonObject value = element.getAsJsonObject();
            values.add(new AffixValueRange(
                    JsonReaderSupport.optionalDouble(value, "min_value", 0.0D),
                    JsonReaderSupport.optionalDouble(value, "max_value", 0.0D)
            ));
        }
        if (values.isEmpty()) {
            values.add(new AffixValueRange(
                    JsonReaderSupport.optionalDouble(tier, "min_value", 0.0D),
                    JsonReaderSupport.optionalDouble(tier, "max_value", 0.0D)
            ));
        }
        return values;
    }
}
