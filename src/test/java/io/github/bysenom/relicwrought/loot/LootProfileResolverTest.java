package io.github.bysenom.relicwrought.loot;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.registry.InMemoryDataRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class LootProfileResolverTest {
    private static final DefinitionKey OVERWORLD_PROFILE = DefinitionKey.parse("overworld_normal", Relicwrought.MOD_ID);
    private static final DefinitionKey NETHER_PROFILE = DefinitionKey.parse("nether_normal", Relicwrought.MOD_ID);
    private static final DefinitionKey BOSS_PROFILE = DefinitionKey.parse("boss", Relicwrought.MOD_ID);

    private InMemoryDataRegistry<LootProfileDefinition> registry;
    private LootProfileResolver resolver;

    @BeforeEach
    void setUp() {
        registry = new InMemoryDataRegistry<>();
        registry.register(new LootProfileDefinition(
                OVERWORLD_PROFILE, LootSourceType.NORMAL_MOB, 0.08, 1, 1,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5),
                Set.of("minecraft:overworld"), Map.of(), true, 1
        ));
        registry.register(new LootProfileDefinition(
                NETHER_PROFILE, LootSourceType.NORMAL_MOB, 0.12, 1, 2,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 250, 650, 5),
                Set.of("minecraft:nether"), Map.of(), true, 1
        ));
        registry.register(new LootProfileDefinition(
                BOSS_PROFILE, LootSourceType.BOSS, 1.0, 2, 4,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 650, 850, 10),
                Set.of("minecraft:overworld", "minecraft:nether"), Map.of(), true, 1
        ));
        resolver = new LootProfileResolver(registry);
    }

    @Test
    void resolveByKeyFindsProfile() {
        var profile = resolver.resolve(OVERWORLD_PROFILE);
        assertEquals(OVERWORLD_PROFILE, profile.id());
    }

    @Test
    void resolveByKeyThrowsOnUnknown() {
        assertThrows(IllegalArgumentException.class, () ->
                resolver.resolve(DefinitionKey.parse("unknown", Relicwrought.MOD_ID)));
    }

    @Test
    void resolveForContextMatchesSourceTypeAndDimension() {
        var context = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 100.0, 20.0, 10.0, true, 0);
        var profile = resolver.resolveForContext(context);
        assertEquals(OVERWORLD_PROFILE, profile.id());
    }

    @Test
    void resolveForContextPrefersMoreSpecificDimension() {
        var context = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:nether", "test",
                null, 100.0, 20.0, 10.0, true, 0);
        var profile = resolver.resolveForContext(context);
        assertEquals(NETHER_PROFILE, profile.id());
    }

    @Test
    void resolveForContextMatchesBossSourceType() {
        var context = new LootContextData(
                LootSourceType.BOSS, "minecraft:overworld", "test",
                null, 100.0, 20.0, 10.0, true, 0);
        var profile = resolver.resolveForContext(context);
        assertEquals(BOSS_PROFILE, profile.id());
    }

    @Test
    void resolveForContextThrowsOnNoMatch() {
        var context = new LootContextData(
                LootSourceType.CHEST, "minecraft:overworld", "test",
                null, 0.0, 0.0, 0.0, false, 0);
        assertThrows(IllegalArgumentException.class, () -> resolver.resolveForContext(context));
    }

    @Test
    void resolveForContextThrowsOnUnmatchedDimension() {
        var context = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:the_end", "test",
                null, 100.0, 20.0, 10.0, true, 0);
        assertThrows(IllegalArgumentException.class, () -> resolver.resolveForContext(context));
    }

    @Test
    void findOverrideReturnsNullWhenNonexistent() {
        var profile = resolver.resolve(OVERWORLD_PROFILE);
        assertNull(resolver.findOverride(profile, "minecraft:zombie"));
    }

    @Test
    void findOverrideReturnsNonNullWhenPresent() {
        var override = new EntityLootOverride(OVERWORLD_PROFILE, 10, 0.0, 1);
        var profileWithOverride = new LootProfileDefinition(
                OVERWORLD_PROFILE, LootSourceType.NORMAL_MOB, 0.08, 1, 1,
                Set.of(), Set.of(), Set.of(), Map.of(),
                new LootItemLevelConfig(LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 5),
                Set.of("minecraft:overworld"),
                Map.of("minecraft:zombie", override), true, 1
        );
        var found = resolver.findOverride(profileWithOverride, "minecraft:zombie");
        assertNotNull(found);
        assertEquals(10, found.itemLevelBonus());
    }
}
