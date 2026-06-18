package io.github.bysenom.relicwrought.item.registry;

import io.github.bysenom.relicwrought.item.generation.RarityDefinition;
import io.github.bysenom.relicwrought.item.model.AffixDefinition;
import io.github.bysenom.relicwrought.item.model.AffixGroupDefinition;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.scaling.ScalingProfile;
import io.github.bysenom.relicwrought.loot.LootProfileDefinition;
import io.github.bysenom.relicwrought.player.ClassDefinition;
import io.github.bysenom.relicwrought.player.StarterKitDefinition;
import io.github.bysenom.relicwrought.progression.CharacterProgressionDefinition;

import java.util.List;

public record DefinitionLoadResult(
        InMemoryDataRegistry<ItemBaseDefinition> itemBases,
        InMemoryDataRegistry<AffixDefinition> affixes,
        InMemoryDataRegistry<AffixGroupDefinition> affixGroups,
        InMemoryDataRegistry<ScalingProfile> scalingProfiles,
        InMemoryDataRegistry<RarityDefinition> rarities,
        InMemoryDataRegistry<LootProfileDefinition> lootProfiles,
        InMemoryDataRegistry<ClassDefinition> classes,
        InMemoryDataRegistry<StarterKitDefinition> starterKits,
        InMemoryDataRegistry<CharacterProgressionDefinition> progressionProfiles,
        List<String> errors
) {
    public DefinitionLoadResult {
        errors = List.copyOf(errors);
    }

    public static DefinitionLoadResult empty() {
        return new DefinitionLoadResult(
                new InMemoryDataRegistry<>(), new InMemoryDataRegistry<>(),
                new InMemoryDataRegistry<>(), new InMemoryDataRegistry<>(),
                new InMemoryDataRegistry<>(), new InMemoryDataRegistry<>(),
                new InMemoryDataRegistry<>(), new InMemoryDataRegistry<>(),
                new InMemoryDataRegistry<>(),
                List.of()
        );
    }
}
