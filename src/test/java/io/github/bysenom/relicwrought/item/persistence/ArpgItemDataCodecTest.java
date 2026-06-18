package io.github.bysenom.relicwrought.item.persistence;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.ItemDataVersions;
import io.github.bysenom.relicwrought.item.model.AffixComponentRoll;
import io.github.bysenom.relicwrought.item.model.AffixOperation;
import io.github.bysenom.relicwrought.item.model.AffixRoll;
import io.github.bysenom.relicwrought.item.model.AffixScope;
import io.github.bysenom.relicwrought.item.model.AffixTier;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.model.Rarity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArpgItemDataCodecTest {
    @Test
    void roundtripCompleteItem() {
        DefinitionKey key = DefinitionKey.parse("starter_training_sword", Relicwrought.MOD_ID);
        UUID itemId = UUID.randomUUID();
        AffixComponentRoll componentRoll = new AffixComponentRoll(
                "physical_damage", AffixScope.LOCAL, AffixOperation.ADDITIVE_PERCENT, 0.75D, 45.0D
        );
        AffixRoll affixRoll = new AffixRoll(
                DefinitionKey.parse("local_physical_damage_percent", Relicwrought.MOD_ID),
                AffixTier.T3, 0.75D, 45.0D, List.of(componentRoll), 1
        );
        ArpgItemData original = new ArpgItemData(
                ItemDataVersions.CURRENT, itemId, key,
                new ItemLevel(300), 0, Rarity.RARE, 12, 123456789L,
                false, List.of(), List.of(affixRoll), List.of(affixRoll)
        );

        DataResult<com.google.gson.JsonElement> encodeResult = ArpgItemDataCodec.CODEC.encodeStart(JsonOps.INSTANCE, original);
        assertTrue(encodeResult.isSuccess(), "Encoding failed: " + encodeResult.error().map(Object::toString).orElse(""));

        ArpgItemData decoded = ArpgItemDataCodec.CODEC.decode(JsonOps.INSTANCE, encodeResult.getOrThrow()).getOrThrow().getFirst();

        assertEquals(original, decoded);
    }

    @Test
    void roundtripMinimalItem() {
        DefinitionKey key = DefinitionKey.parse("starter_pickaxe", Relicwrought.MOD_ID);
        UUID itemId = UUID.randomUUID();
        ArpgItemData original = new ArpgItemData(
                ItemDataVersions.CURRENT, itemId, key,
                new ItemLevel(1), 0, Rarity.COMMON, 0, 42L,
                false, List.of(), List.of(), List.of()
        );

        DynamicOps<com.google.gson.JsonElement> ops = JsonOps.INSTANCE;
        ArpgItemData decoded = ArpgItemDataCodec.CODEC.decode(ops, ArpgItemDataCodec.CODEC.encodeStart(ops, original).getOrThrow()).getOrThrow().getFirst();
        assertEquals(original, decoded);
    }

    @Test
    void uuidSurvivesRoundtrip() {
        DefinitionKey key = DefinitionKey.parse("scout_helmet", Relicwrought.MOD_ID);
        UUID originalId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ArpgItemData original = new ArpgItemData(
                ItemDataVersions.CURRENT, originalId, key,
                new ItemLevel(500), 0, Rarity.MAGIC, 5, 999L,
                false, List.of(), List.of(), List.of()
        );

        DynamicOps<com.google.gson.JsonElement> ops = JsonOps.INSTANCE;
        ArpgItemData decoded = ArpgItemDataCodec.CODEC.decode(ops, ArpgItemDataCodec.CODEC.encodeStart(ops, original).getOrThrow()).getOrThrow().getFirst();
        assertEquals(originalId, decoded.itemId(), "UUID must survive roundtrip unchanged");
    }

    @Test
    void deterministicEncoding() {
        DefinitionKey key = DefinitionKey.parse("guard_chestplate", Relicwrought.MOD_ID);
        ArpgItemData data = new ArpgItemData(
                ItemDataVersions.CURRENT, UUID.randomUUID(), key,
                new ItemLevel(700), 5, Rarity.RARE, 15, 555L,
                true, List.of(), List.of(), List.of()
        );

        DynamicOps<com.google.gson.JsonElement> ops = JsonOps.INSTANCE;
        var first = ArpgItemDataCodec.CODEC.encodeStart(ops, data).getOrThrow();
        var second = ArpgItemDataCodec.CODEC.encodeStart(ops, data).getOrThrow();
        assertEquals(first, second, "Same input must produce identical encoded form");
    }

    @Test
    void componentRollSurvivesRoundtrip() {
        AffixComponentRoll original = new AffixComponentRoll(
                "fire_resistance", AffixScope.GLOBAL, AffixOperation.ADDITIVE_PERCENT, 0.5D, 25.0D
        );

        DynamicOps<com.google.gson.JsonElement> ops = JsonOps.INSTANCE;
        AffixComponentRoll decoded = ArpgItemDataCodec.AFFIX_COMPONENT_ROLL_CODEC.decode(
                ops, ArpgItemDataCodec.AFFIX_COMPONENT_ROLL_CODEC.encodeStart(ops, original).getOrThrow()
        ).getOrThrow().getFirst();

        assertEquals(original, decoded);
    }
}
