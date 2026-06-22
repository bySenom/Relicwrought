package io.github.bysenom.relicwrought.item.io;

import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.item.model.BaseStatBlock;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ItemBaseScaling;
import io.github.bysenom.relicwrought.item.model.ItemCategory;

import java.util.List;

public final class ItemBaseDefinitionJsonReader implements DefinitionJsonReader<ItemBaseDefinition> {
    @Override
    public ItemBaseDefinition read(JsonObject json, String defaultNamespace) {
        DefinitionKey id = DefinitionKey.parse(JsonReaderSupport.requiredString(json, "id"), defaultNamespace);
        JsonObject stats = JsonReaderSupport.requiredObject(json, "base_stats");
        DefinitionKey fallbackScalingProfile = DefinitionKey.parse(
                JsonReaderSupport.optionalString(json, "scaling_profile", "relicwrought:default"),
                defaultNamespace
        );

        return new ItemBaseDefinition(
                id,
                JsonReaderSupport.requiredString(json, "translation_key"),
                JsonReaderSupport.requiredString(json, "minecraft_item"),
                ItemCategory.valueOf(JsonReaderSupport.requiredString(json, "category").toUpperCase()),
                readValidSlots(json),
                new BaseStatBlock(
                        JsonReaderSupport.optionalDouble(stats, "damage_min", 0.0D),
                        JsonReaderSupport.optionalDouble(stats, "damage_max", 0.0D),
                        JsonReaderSupport.optionalDouble(stats, "attack_speed", 0.0D),
                        JsonReaderSupport.optionalDouble(stats, "armor", 0.0D),
                        JsonReaderSupport.optionalInt(stats, "durability", 0),
                        JsonReaderSupport.optionalDouble(stats, "mining_speed", 0.0D),
                        JsonReaderSupport.optionalInt(stats, "mining_tier", 0)
                ),
                List.copyOf(JsonReaderSupport.keySet(json, "implicit_affixes", defaultNamespace)),
                JsonReaderSupport.stringSet(json, "affix_tags"),
                fallbackScalingProfile,
                readScaling(json, fallbackScalingProfile, defaultNamespace),
                JsonReaderSupport.keySet(json, "loot_sources", defaultNamespace),
                JsonReaderSupport.requiredInt(json, "data_version")
        );
    }

    private static java.util.Set<ArpgEquipmentSlot> readValidSlots(JsonObject json) {
        if (json.has("allowed_equipment_slots")) {
            return JsonReaderSupport.enumSet(json, "allowed_equipment_slots", ArpgEquipmentSlot::valueOf);
        }
        return JsonReaderSupport.enumSet(json, "valid_slots", ArpgEquipmentSlot::valueOf);
    }

    private static ItemBaseScaling readScaling(JsonObject json, DefinitionKey fallbackScalingProfile, String defaultNamespace) {
        JsonObject scaling = JsonReaderSupport.optionalObject(json, "scaling");
        if (scaling == null) {
            return ItemBaseScaling.defaults(fallbackScalingProfile);
        }

        return new ItemBaseScaling(
                DefinitionKey.parse(JsonReaderSupport.optionalString(scaling, "damage_profile", fallbackScalingProfile.toString()), defaultNamespace),
                DefinitionKey.parse(JsonReaderSupport.optionalString(scaling, "armor_profile", fallbackScalingProfile.toString()), defaultNamespace),
                DefinitionKey.parse(JsonReaderSupport.optionalString(scaling, "durability_profile", fallbackScalingProfile.toString()), defaultNamespace),
                DefinitionKey.parse(JsonReaderSupport.optionalString(scaling, "mining_speed_profile", fallbackScalingProfile.toString()), defaultNamespace),
                DefinitionKey.parse(JsonReaderSupport.optionalString(scaling, "mining_tier_profile", fallbackScalingProfile.toString()), defaultNamespace),
                JsonReaderSupport.optionalDouble(scaling, "minimum_damage_multiplier", 0.85D),
                JsonReaderSupport.optionalDouble(scaling, "maximum_damage_multiplier", 1.15D),
                JsonReaderSupport.optionalDouble(scaling, "armor_multiplier", 1.0D),
                JsonReaderSupport.optionalDouble(scaling, "durability_multiplier", 1.0D),
                JsonReaderSupport.optionalDouble(scaling, "mining_speed_multiplier", 1.0D)
        );
    }
}
