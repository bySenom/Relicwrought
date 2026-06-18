package io.github.bysenom.relicwrought.item.registry;

import io.github.bysenom.relicwrought.item.model.BaseStatBlock;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ItemCategory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class InMemoryDataRegistryTest {
    @Test
    void rejectsDuplicateDefinitionIds() {
        InMemoryDataRegistry<ItemBaseDefinition> registry = new InMemoryDataRegistry<>();
        ItemBaseDefinition definition = new ItemBaseDefinition(
                DefinitionKey.parse("test_sword", "arpgmod"),
                "item_base.arpgmod.test_sword",
                "minecraft:wooden_sword",
                ItemCategory.SWORD,
                Set.of(ArpgEquipmentSlot.MAIN_HAND),
                BaseStatBlock.empty(),
                List.of(),
                Set.of("weapon"),
                DefinitionKey.parse("default_weapon", "arpgmod"),
                null,
                Set.of(),
                1
        );

        registry.register(definition);

        assertEquals(1, registry.size());
        assertThrows(IllegalArgumentException.class, () -> registry.register(definition));
    }
}
