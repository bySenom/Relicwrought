package io.github.bysenom.relicwrought.loot;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;

public record EntityLootOverride(
        DefinitionKey profileId,
        int itemLevelBonus,
        double dropChanceBonus,
        int additionalDropCount
) {
    public EntityLootOverride {
        if (dropChanceBonus < -1.0) dropChanceBonus = -1.0;
        if (dropChanceBonus > 1.0) dropChanceBonus = 1.0;
        if (additionalDropCount < 0) additionalDropCount = 0;
        if (additionalDropCount > 10) additionalDropCount = 10;
    }
}
