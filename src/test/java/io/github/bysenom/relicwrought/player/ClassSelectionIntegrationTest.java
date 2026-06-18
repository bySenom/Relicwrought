package io.github.bysenom.relicwrought.player;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.io.ArpgDataBootstrap;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.registry.DefinitionLoadResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

final class ClassSelectionIntegrationTest {
    private static DefinitionLoadResult definitions;

    @BeforeAll
    static void loadDefinitions() {
        definitions = ArpgDataBootstrap.loadBundledDefinitions(
                Relicwrought.MOD_ID,
                LoggerFactory.getLogger("class-selection-test")
        );
        assertTrue(definitions.errors().isEmpty(), () -> String.join("\n", definitions.errors()));
    }

    @Test
    void fourClassesAreLoaded() {
        assertEquals(4, definitions.classes().size());
    }

    @Test
    void fourStarterKitsAreLoaded() {
        assertEquals(4, definitions.starterKits().size());
    }

    @Test
    void allClassesAreEnabled() {
        for (var cls : definitions.classes().values()) {
            assertTrue(cls.enabled(), "Class " + cls.id() + " should be enabled");
        }
    }

    @Test
    void allClassIdsAreUnique() {
        List<DefinitionKey> ids = definitions.classes().values().stream()
                .map(ClassDefinition::id).toList();
        assertEquals(Set.copyOf(ids).size(), ids.size());
    }

    @Test
    void allClassesReferenceExistingKits() {
        for (var cls : definitions.classes().values()) {
            DefinitionKey kitKey = DefinitionKey.parse(cls.starterKitId(), Relicwrought.MOD_ID);
            assertTrue(definitions.starterKits().get(kitKey).isPresent(),
                    "Class " + cls.id() + " references non-existent kit: " + cls.starterKitId());
        }
    }

    @Test
    void allKitsAreReferencedByAtLeastOneClass() {
        Set<DefinitionKey> referencedKits = definitions.classes().values().stream()
                .map(cls -> DefinitionKey.parse(cls.starterKitId(), Relicwrought.MOD_ID))
                .collect(Collectors.toSet());
        for (var kit : definitions.starterKits().values()) {
            assertTrue(referencedKits.contains(kit.id()),
                    "Kit " + kit.id() + " is not referenced by any class");
        }
    }

    @Test
    void everyKitContainsPickaxe() {
        for (var kit : definitions.starterKits().values()) {
            boolean hasPickaxe = kit.entries().stream()
                    .anyMatch(e -> e.itemBaseId().contains("pickaxe"));
            assertTrue(hasPickaxe, "Kit " + kit.id() + " is missing a pickaxe");
        }
    }

    @Test
    void everyKitContainsAxe() {
        for (var kit : definitions.starterKits().values()) {
            boolean hasAxe = kit.entries().stream()
                    .anyMatch(e -> e.itemBaseId().contains("axe"));
            assertTrue(hasAxe, "Kit " + kit.id() + " is missing an axe");
        }
    }

    @Test
    void everyKitContainsShovel() {
        for (var kit : definitions.starterKits().values()) {
            boolean hasShovel = kit.entries().stream()
                    .anyMatch(e -> e.itemBaseId().contains("shovel"));
            assertTrue(hasShovel, "Kit " + kit.id() + " is missing a shovel");
        }
    }

    @Test
    void allKitEntriesHaveItemLevel1() {
        for (var kit : definitions.starterKits().values()) {
            for (var entry : kit.entries()) {
                assertEquals(1, entry.itemLevel(),
                        "Kit " + kit.id() + " entry " + entry.itemBaseId() + " has item level " + entry.itemLevel());
            }
        }
    }

    @Test
    void allKitEntriesHaveQuality0() {
        for (var kit : definitions.starterKits().values()) {
            for (var entry : kit.entries()) {
                assertEquals(0, entry.quality(),
                        "Kit " + kit.id() + " entry " + entry.itemBaseId() + " has quality " + entry.quality());
            }
        }
    }

    @Test
    void allNonDaggerEntriesHaveCount1() {
        for (var kit : definitions.starterKits().values()) {
            for (var entry : kit.entries()) {
                if (entry.itemBaseId().contains("dagger")) {
                    assertTrue(entry.count() >= 1,
                            "Kit " + kit.id() + " dagger " + entry.itemBaseId() + " has count " + entry.count());
                } else {
                    assertEquals(1, entry.count(),
                            "Kit " + kit.id() + " entry " + entry.itemBaseId() + " has count " + entry.count());
                }
            }
        }
    }

    @Test
    void classSortOrderIsStable() {
        List<ClassDefinition> sorted = definitions.classes().values().stream()
                .sorted(java.util.Comparator.comparingInt(ClassDefinition::sortOrder))
                .toList();
        assertEquals(4, sorted.size());
        for (int i = 0; i < sorted.size() - 1; i++) {
            assertTrue(sorted.get(i).sortOrder() <= sorted.get(i + 1).sortOrder());
        }
    }
}
