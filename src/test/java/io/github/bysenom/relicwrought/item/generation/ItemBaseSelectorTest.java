package io.github.bysenom.relicwrought.item.generation;

import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.item.model.BaseStatBlock;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ItemBaseScaling;
import io.github.bysenom.relicwrought.item.model.ItemCategory;
import io.github.bysenom.relicwrought.item.registry.InMemoryDataRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

class ItemBaseSelectorTest {
    static final DefinitionKey SWORD_ID = DefinitionKey.parse("starter_training_sword", "arpgmod");
    static final DefinitionKey AXE_ID = DefinitionKey.parse("iron_war_axe", "arpgmod");
    static final DefinitionKey PICKAXE_ID = DefinitionKey.parse("starter_pickaxe", "arpgmod");

    InMemoryDataRegistry<ItemBaseDefinition> registry;
    ItemBaseSelector selector;

    @BeforeEach
    void setUp() {
        registry = new InMemoryDataRegistry<>();
        registry.register(createBase(SWORD_ID, ItemCategory.SWORD, Set.of("weapon", "melee")));
        registry.register(createBase(AXE_ID, ItemCategory.COMBAT_AXE, Set.of("weapon", "melee", "axe", "tool")));
        registry.register(createBase(PICKAXE_ID, ItemCategory.PICKAXE, Set.of("tool", "pickaxe")));
        selector = new ItemBaseSelector(registry);
    }

    @Test
    void selectExplicitFound() {
        assertTrue(selector.selectExplicit(SWORD_ID).isPresent());
        assertEquals(SWORD_ID, selector.selectExplicit(SWORD_ID).get().id());
    }

    @Test
    void selectExplicitNotFound() {
        assertTrue(selector.selectExplicit(DefinitionKey.parse("unknown", "x")).isEmpty());
    }

    @Test
    void selectFromPoolByCategory() {
        ItemBaseDefinition base = selector.selectFromPool(
                Set.of(ItemCategory.COMBAT_AXE), null, null, null,
                new SplittableRandom(42)
        );
        assertEquals(AXE_ID, base.id());
    }

    @Test
    void selectFromPoolByRequiredTag() {
        ItemBaseDefinition base = selector.selectFromPool(
                null, Set.of("pickaxe"), null, null,
                new SplittableRandom(42)
        );
        assertEquals(PICKAXE_ID, base.id());
    }

    @Test
    void selectFromPoolExcludesByTag() {
        ItemBaseDefinition base = selector.selectFromPool(
                null, null, Set.of("axe"), null,
                new SplittableRandom(42)
        );
        assertNotEquals(AXE_ID, base.id(), "Axe should be excluded by 'axe' tag");
        assertTrue(base.id().equals(SWORD_ID) || base.id().equals(PICKAXE_ID),
                "Should select either sword or pickaxe");
    }

    @Test
    void selectFromPoolByAllowedIds() {
        ItemBaseDefinition base = selector.selectFromPool(
                null, null, null, Set.of(SWORD_ID),
                new SplittableRandom(42)
        );
        assertEquals(SWORD_ID, base.id());
    }

    @Test
    void throwWhenNoEligibleBase() {
        assertThrows(IllegalStateException.class, () ->
                selector.selectFromPool(
                        Set.of(ItemCategory.SHIELD), null, null, null,
                        new SplittableRandom(42)
                )
        );
    }

    @Test
    void deterministicSelection() {
        ItemBaseDefinition r1 = selector.selectFromPool(
                Set.of(ItemCategory.SWORD, ItemCategory.COMBAT_AXE), null, null, null,
                new SplittableRandom(100)
        );
        ItemBaseDefinition r2 = selector.selectFromPool(
                Set.of(ItemCategory.SWORD, ItemCategory.COMBAT_AXE), null, null, null,
                new SplittableRandom(100)
        );
        assertEquals(r1.id(), r2.id());
    }

    private static ItemBaseDefinition createBase(DefinitionKey id, ItemCategory category, Set<String> tags) {
        BaseStatBlock stats = BaseStatBlock.empty();
        return new ItemBaseDefinition(
                id, "item_base." + id,
                "minecraft:" + category.name().toLowerCase(),
                category, Set.of(ArpgEquipmentSlot.MAIN_HAND),
                stats, List.of(), tags,
                DefinitionKey.parse("default", "arpgmod"),
                ItemBaseScaling.defaults(DefinitionKey.parse("default", "arpgmod")),
                Set.of(), 1
        );
    }
}
