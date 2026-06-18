package io.github.bysenom.relicwrought.item.model;

import java.util.Set;

public record AffixGroupDefinition(
        DefinitionKey id,
        int maxPerItem,
        Set<DefinitionKey> conflicts,
        Set<String> tags,
        int dataVersion
) implements KeyedDefinition {
    public AffixGroupDefinition {
        if (maxPerItem < 1 || maxPerItem > 6) {
            throw new IllegalArgumentException("Affix group max per item must be between 1 and 6: " + maxPerItem);
        }
        conflicts = Set.copyOf(conflicts);
        tags = Set.copyOf(tags);
        if (dataVersion <= 0) {
            throw new IllegalArgumentException("Affix group data version must be positive");
        }
    }
}
