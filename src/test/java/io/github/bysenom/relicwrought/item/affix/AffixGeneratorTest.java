package io.github.bysenom.relicwrought.item.affix;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.io.ArpgDataBootstrap;
import io.github.bysenom.relicwrought.item.model.AffixComponentDefinition;
import io.github.bysenom.relicwrought.item.model.AffixDefinition;
import io.github.bysenom.relicwrought.item.model.AffixGroupDefinition;
import io.github.bysenom.relicwrought.item.model.AffixOperation;
import io.github.bysenom.relicwrought.item.model.AffixRoll;
import io.github.bysenom.relicwrought.item.model.AffixScope;
import io.github.bysenom.relicwrought.item.model.AffixTier;
import io.github.bysenom.relicwrought.item.model.AffixTierDefinition;
import io.github.bysenom.relicwrought.item.model.AffixType;
import io.github.bysenom.relicwrought.item.model.AffixValueRange;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ItemCategory;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.registry.DefinitionLoadResult;
import io.github.bysenom.relicwrought.item.registry.InMemoryDataRegistry;
import io.github.bysenom.relicwrought.item.scaling.RoundingStrategy;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AffixGeneratorTest {
    @Test
    void sameSeedProducesSameRolls() {
        DefinitionLoadResult result = loadDefinitions();
        ItemBaseDefinition itemBase = result.itemBases().get(key("starter_training_sword")).orElseThrow();
        AffixGenerator generator = new AffixGenerator(result.affixes(), result.affixGroups());
        AffixGenerationRequest request = new AffixGenerationRequest(
                itemBase,
                new ItemLevel(900),
                new AffixSlotLimits(3, 3),
                123456789L,
                AffixGenerationPolicy.STRICT_DEFAULT
        );

        AffixGenerationResult first = generator.generate(request);
        AffixGenerationResult second = generator.generate(request);

        assertEquals(first, second);
        assertTrue(first.complete(), () -> String.join("\n", first.messages()));
        assertEquals(3, first.prefixes().size());
        assertEquals(3, first.suffixes().size());
    }

    @Test
    void registryInsertionOrderDoesNotAffectSelection() {
        DefinitionLoadResult result = loadDefinitions();
        ItemBaseDefinition itemBase = result.itemBases().get(key("starter_training_sword")).orElseThrow();
        AffixGenerationRequest request = new AffixGenerationRequest(
                itemBase,
                new ItemLevel(900),
                new AffixSlotLimits(3, 3),
                987654321L,
                AffixGenerationPolicy.STRICT_DEFAULT
        );

        AffixGenerationResult normal = new AffixGenerator(result.affixes(), result.affixGroups()).generate(request);
        AffixGenerationResult reversed = new AffixGenerator(reverse(result.affixes().values()), reverse(result.affixGroups().values())).generate(request);

        assertEquals(normal, reversed);
    }

    @Test
    void bestEffortReturnsPartialResultWhenSlotsCannotBeFilled() {
        DefinitionLoadResult result = loadDefinitions();
        ItemBaseDefinition chest = result.itemBases().get(key("guard_chestplate")).orElseThrow();
        AffixGenerationRequest request = new AffixGenerationRequest(
                chest,
                new ItemLevel(900),
                new AffixSlotLimits(3, 0),
                42L,
                AffixGenerationPolicy.BEST_EFFORT_DEFAULT
        );

        AffixGenerationResult resultValue = new AffixGenerator(result.affixes(), result.affixGroups()).generate(request);

        assertFalse(resultValue.complete());
        assertEquals(2, resultValue.prefixes().size());
        assertTrue(resultValue.messages().stream().anyMatch(message -> message.startsWith("no_affix_candidate:prefix")));
    }

    @Test
    void strictReturnsIncompleteResultWhenSlotsCannotBeFilled() {
        DefinitionLoadResult result = loadDefinitions();
        ItemBaseDefinition chest = result.itemBases().get(key("guard_chestplate")).orElseThrow();
        AffixGenerationRequest request = new AffixGenerationRequest(
                chest,
                new ItemLevel(900),
                new AffixSlotLimits(3, 0),
                42L,
                AffixGenerationPolicy.STRICT_DEFAULT
        );

        AffixGenerationResult resultValue = new AffixGenerator(result.affixes(), result.affixGroups()).generate(request);

        assertFalse(resultValue.complete());
        assertEquals(2, resultValue.prefixes().size());
        assertEquals(1, resultValue.messages().size());
    }

    @Test
    void groupConflictsAreBidirectional() {
        InMemoryDataRegistry<AffixDefinition> affixes = new InMemoryDataRegistry<>();
        InMemoryDataRegistry<AffixGroupDefinition> groups = new InMemoryDataRegistry<>();
        DefinitionKey firstGroup = key("first_group");
        DefinitionKey secondGroup = key("second_group");
        groups.register(new AffixGroupDefinition(firstGroup, 1, Set.of(secondGroup), Set.of(), 1));
        groups.register(new AffixGroupDefinition(secondGroup, 1, Set.of(), Set.of(), 1));
        affixes.register(testAffix("aaa_first", firstGroup, 1, Set.of()));
        affixes.register(testAffix("zzz_second", secondGroup, 10_000, Set.of()));
        ItemBaseDefinition itemBase = loadDefinitions().itemBases().get(key("starter_training_sword")).orElseThrow();

        AffixGenerationResult result = new AffixGenerator(affixes, groups).generate(new AffixGenerationRequest(
                itemBase,
                new ItemLevel(1),
                new AffixSlotLimits(2, 0),
                1L,
                AffixGenerationPolicy.BEST_EFFORT_DEFAULT
        ));

        assertEquals(1, result.prefixes().size());
        assertFalse(result.complete());
    }

    @Test
    void tagRulesFilterCandidates() {
        InMemoryDataRegistry<AffixDefinition> affixes = new InMemoryDataRegistry<>();
        InMemoryDataRegistry<AffixGroupDefinition> groups = new InMemoryDataRegistry<>();
        groups.register(new AffixGroupDefinition(key("allowed"), 1, Set.of(), Set.of(), 1));
        groups.register(new AffixGroupDefinition(key("blocked"), 1, Set.of(), Set.of(), 1));
        affixes.register(testAffix("allowed_affix", key("allowed"), 100, Set.of("weapon")));
        affixes.register(testAffix("blocked_affix", key("blocked"), 10_000, Set.of("missing_tag")));
        ItemBaseDefinition itemBase = loadDefinitions().itemBases().get(key("starter_training_sword")).orElseThrow();

        AffixGenerationResult result = new AffixGenerator(affixes, groups).generate(new AffixGenerationRequest(
                itemBase,
                new ItemLevel(1),
                new AffixSlotLimits(1, 0),
                1L,
                AffixGenerationPolicy.STRICT_DEFAULT
        ));

        assertTrue(result.complete(), () -> String.join("\n", result.messages()));
        assertEquals(key("allowed_affix"), result.prefixes().getFirst().affixId());
    }

    @Test
    void tierWindowSelectsOnlyBestUnlockedAndConfiguredWorseTiers() {
        InMemoryDataRegistry<AffixDefinition> affixes = new InMemoryDataRegistry<>();
        InMemoryDataRegistry<AffixGroupDefinition> groups = new InMemoryDataRegistry<>();
        groups.register(new AffixGroupDefinition(key("tier_group"), 1, Set.of(), Set.of(), 1));
        affixes.register(new AffixDefinition(
                key("tiered_affix"),
                "affix.arpgmod.tiered_affix",
                AffixType.PREFIX,
                Set.of(key("tier_group")),
                Set.of(ItemCategory.SWORD),
                List.of(new AffixComponentDefinition("test", AffixScope.LOCAL, AffixOperation.ADD_FLAT)),
                100,
                List.of(
                        new AffixTierDefinition(AffixTier.T5, 500, 10, RoundingStrategy.NONE, List.of(new AffixValueRange(5.0D, 5.0D))),
                        new AffixTierDefinition(AffixTier.T6, 400, 10_000, RoundingStrategy.NONE, List.of(new AffixValueRange(6.0D, 6.0D))),
                        new AffixTierDefinition(AffixTier.T7, 300, 10_000, RoundingStrategy.NONE, List.of(new AffixValueRange(7.0D, 7.0D))),
                        new AffixTierDefinition(AffixTier.T8, 200, 10_000, RoundingStrategy.NONE, List.of(new AffixValueRange(8.0D, 8.0D)))
                ),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                "add_flat",
                1
        ));
        ItemBaseDefinition itemBase = loadDefinitions().itemBases().get(key("starter_training_sword")).orElseThrow();

        AffixGenerationResult result = new AffixGenerator(affixes, groups).generate(new AffixGenerationRequest(
                itemBase,
                new ItemLevel(500),
                new AffixSlotLimits(1, 0),
                5L,
                new AffixGenerationPolicy(AffixGenerationMode.STRICT, 1)
        ));

        assertTrue(result.complete(), () -> String.join("\n", result.messages()));
        AffixTier tier = result.prefixes().getFirst().tier();
        assertTrue(tier == AffixTier.T5 || tier == AffixTier.T6);
    }

    @Test
    void multiComponentAffixesRollEachComponent() {
        DefinitionLoadResult result = loadDefinitions();
        ItemBaseDefinition itemBase = result.itemBases().get(key("starter_training_sword")).orElseThrow();
        AffixRoll roll = new AffixGenerator(result.affixes(), result.affixGroups()).generate(new AffixGenerationRequest(
                itemBase,
                new ItemLevel(900),
                new AffixSlotLimits(3, 0),
                27L,
                AffixGenerationPolicy.STRICT_DEFAULT
        )).prefixes().stream()
                .filter(prefix -> prefix.componentRolls().size() > 1)
                .findFirst()
                .orElseThrow();

        assertEquals(2, roll.componentRolls().size());
        assertTrue(roll.normalizedRoll() >= 0.0D && roll.normalizedRoll() <= 1.0D);
    }

    private static DefinitionLoadResult loadDefinitions() {
        return ArpgDataBootstrap.loadBundledDefinitions(Relicwrought.MOD_ID, LoggerFactory.getLogger("arpgmod-test"));
    }

    private static DefinitionKey key(String path) {
        return DefinitionKey.parse(path, Relicwrought.MOD_ID);
    }

    private static <T extends io.github.bysenom.relicwrought.item.model.KeyedDefinition> InMemoryDataRegistry<T> reverse(Iterable<T> definitions) {
        ArrayList<T> values = new ArrayList<>();
        definitions.forEach(values::add);
        values.sort(Comparator.comparing(definition -> definition.id().toString(), Comparator.reverseOrder()));
        InMemoryDataRegistry<T> registry = new InMemoryDataRegistry<>();
        values.forEach(registry::register);
        return registry;
    }

    private static AffixDefinition testAffix(String id, DefinitionKey group, int weight, Set<String> requiredTagsAny) {
        return new AffixDefinition(
                key(id),
                "affix.arpgmod." + id,
                AffixType.PREFIX,
                Set.of(group),
                Set.of(ItemCategory.SWORD),
                List.of(new AffixComponentDefinition("test", AffixScope.LOCAL, AffixOperation.ADD_FLAT)),
                weight,
                List.of(new AffixTierDefinition(AffixTier.T10, 1, 100, RoundingStrategy.NONE, List.of(new AffixValueRange(1.0D, 1.0D)))),
                Set.of(),
                Set.of(),
                requiredTagsAny,
                Set.of(),
                Set.of(),
                "add_flat",
                1
        );
    }
}
