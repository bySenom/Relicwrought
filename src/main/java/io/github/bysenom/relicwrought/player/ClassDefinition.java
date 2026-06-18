package io.github.bysenom.relicwrought.player;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.KeyedDefinition;

import java.util.List;
import java.util.Map;

public record ClassDefinition(
        DefinitionKey id,
        String displayNameKey,
        String descriptionKey,
        String starterKitId,
        int sortOrder,
        boolean enabled,
        int dataVersion
) implements KeyedDefinition {
    public ClassDefinition {
        if (displayNameKey == null || displayNameKey.isBlank()) {
            throw new IllegalArgumentException("Class display name key must not be blank");
        }
        if (starterKitId == null || starterKitId.isBlank()) {
            throw new IllegalArgumentException("Class starter kit ID must not be blank");
        }
        if (dataVersion <= 0) {
            throw new IllegalArgumentException("Class data version must be positive");
        }
    }
}
