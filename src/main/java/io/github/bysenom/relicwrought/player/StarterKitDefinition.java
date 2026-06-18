package io.github.bysenom.relicwrought.player;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.KeyedDefinition;

import java.util.List;

public record StarterKitDefinition(
        DefinitionKey id,
        List<StarterKitEntry> entries,
        int dataVersion
) implements KeyedDefinition {
    public StarterKitDefinition {
        entries = List.copyOf(entries);
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("Starter kit must have at least one entry");
        }
        if (dataVersion <= 0) {
            throw new IllegalArgumentException("Starter kit data version must be positive");
        }
    }
}
