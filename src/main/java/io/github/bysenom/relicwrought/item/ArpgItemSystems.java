package io.github.bysenom.relicwrought.item;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.io.ArpgDataBootstrap;
import io.github.bysenom.relicwrought.item.registry.DefinitionLoadResult;

public final class ArpgItemSystems {
    private static DefinitionLoadResult bootstrapResult = DefinitionLoadResult.empty();

    private ArpgItemSystems() {
    }

    public static void initialize() {
        bootstrapResult = ArpgDataBootstrap.loadBundledDefinitions(Relicwrought.MOD_ID, Relicwrought.LOGGER);
        Relicwrought.LOGGER.info(
                "ARPG item definitions loaded: {} item bases, {} affixes, {} affix groups, {} scaling profiles, {} rarities, {} loot profiles, {} classes, {} starter kits, {} errors.",
                bootstrapResult.itemBases().size(),
                bootstrapResult.affixes().size(),
                bootstrapResult.affixGroups().size(),
                bootstrapResult.scalingProfiles().size(),
                bootstrapResult.rarities().size(),
                bootstrapResult.lootProfiles().size(),
                bootstrapResult.classes().size(),
                bootstrapResult.starterKits().size(),
                bootstrapResult.errors().size()
        );
    }

    public static DefinitionLoadResult bootstrapResult() {
        return bootstrapResult;
    }
}
