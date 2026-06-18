package io.github.bysenom.relicwrought.item.generation;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.registry.InMemoryDataRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

class RaritySelectorTest {
    static final DefinitionKey COMMON = DefinitionKey.parse("common", "arpgmod");
    static final DefinitionKey MAGIC = DefinitionKey.parse("magic", "arpgmod");
    static final DefinitionKey RARE = DefinitionKey.parse("rare", "arpgmod");

    InMemoryDataRegistry<RarityDefinition> registry;
    RaritySelector selector;

    @BeforeEach
    void setUp() {
        registry = new InMemoryDataRegistry<>();
        registry.register(new RarityDefinition(COMMON, "c", 650, 1, 0, List.of(), 0, "#FFF", 1));
        registry.register(new RarityDefinition(MAGIC, "m", 300, 1, 0, List.of(), 1, "#55F", 1));
        registry.register(new RarityDefinition(RARE, "r", 50, 10, 0, List.of(), 2, "#FF5", 1));
        selector = new RaritySelector(registry);
    }

    @Test
    void selectExplicitFound() {
        assertTrue(selector.selectExplicit(RARE).isPresent());
        assertEquals(RARE, selector.selectExplicit(RARE).get().id());
    }

    @Test
    void selectExplicitNotFound() {
        assertTrue(selector.selectExplicit(DefinitionKey.parse("unknown", "a")).isEmpty());
    }

    @Test
    void weightedSelectReturnsOnlyUnlocked() {
        ItemLevel level = ItemLevel.of(5);
        for (int i = 0; i < 100; i++) {
            RarityDefinition r = selector.selectWeighted(level, new SplittableRandom(i));
            assertNotEquals(RARE, r.id(), "Rare should not be selectable below level 10");
        }
    }

    @Test
    void weightedSelectIncludesRareAtHigherLevel() {
        ItemLevel level = ItemLevel.of(50);
        boolean foundRare = false;
        for (int i = 0; i < 200; i++) {
            RarityDefinition r = selector.selectWeighted(level, new SplittableRandom(i));
            if (r.id().equals(RARE)) {
                foundRare = true;
                break;
            }
        }
        assertTrue(foundRare, "Rare should be selectable at level 50");
    }

    @Test
    void deterministicSelection() {
        ItemLevel level = ItemLevel.of(50);
        RarityDefinition r1 = selector.selectWeighted(level, new SplittableRandom(42));
        RarityDefinition r2 = selector.selectWeighted(level, new SplittableRandom(42));
        assertEquals(r1.id(), r2.id());
    }

    @Test
    void registryOrderDoesNotMatter() {
        InMemoryDataRegistry<RarityDefinition> reversed = new InMemoryDataRegistry<>();
        reversed.register(new RarityDefinition(RARE, "r", 50, 10, 0, List.of(), 2, "#FF5", 1));
        reversed.register(new RarityDefinition(MAGIC, "m", 300, 1, 0, List.of(), 1, "#55F", 1));
        reversed.register(new RarityDefinition(COMMON, "c", 650, 1, 0, List.of(), 0, "#FFF", 1));
        RaritySelector revSelector = new RaritySelector(reversed);

        ItemLevel level = ItemLevel.of(50);
        for (int i = 0; i < 50; i++) {
            RarityDefinition r1 = selector.selectWeighted(level, new SplittableRandom(i));
            RarityDefinition r2 = revSelector.selectWeighted(level, new SplittableRandom(i));
            assertEquals(r1.id(), r2.id(), "Registry order should not affect selection for seed " + i);
        }
    }

    @Test
    void validateRarityForLevel() {
        RarityDefinition common = registry.get(COMMON).orElseThrow();
        RarityDefinition rare = registry.get(RARE).orElseThrow();
        assertDoesNotThrow(() -> selector.validateRarityForLevel(common, ItemLevel.of(1)));
        assertDoesNotThrow(() -> selector.validateRarityForLevel(rare, ItemLevel.of(10)));
        assertThrows(IllegalArgumentException.class, () ->
                selector.validateRarityForLevel(rare, ItemLevel.of(5))
        );
    }

    @Test
    void throwsOnEmptyRegistry() {
        InMemoryDataRegistry<RarityDefinition> empty = new InMemoryDataRegistry<>();
        RaritySelector emptySelector = new RaritySelector(empty);
        assertThrows(IllegalStateException.class, () ->
                emptySelector.selectWeighted(ItemLevel.of(50), new SplittableRandom(1))
        );
    }
}
