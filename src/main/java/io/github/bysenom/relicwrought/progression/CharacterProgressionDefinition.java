package io.github.bysenom.relicwrought.progression;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.KeyedDefinition;

public record CharacterProgressionDefinition(
        DefinitionKey id,
        int minimumLevel,
        int maximumLevel,
        double baseXp,
        double exponent,
        int dataVersion
) implements KeyedDefinition {
    public CharacterProgressionDefinition {
        if (minimumLevel < CharacterLevel.MIN) throw new IllegalArgumentException("minimumLevel must be >= " + CharacterLevel.MIN);
        if (maximumLevel > CharacterLevel.MAX) throw new IllegalArgumentException("maximumLevel must be <= " + CharacterLevel.MAX);
        if (maximumLevel < minimumLevel) throw new IllegalArgumentException("maximumLevel must be >= minimumLevel");
        if (baseXp <= 0) throw new IllegalArgumentException("baseXp must be positive");
        if (exponent <= 0) throw new IllegalArgumentException("exponent must be positive");
        if (dataVersion <= 0) throw new IllegalArgumentException("dataVersion must be positive");
    }

    public ExperienceCurve createCurve() {
        return new ExperienceCurve(baseXp, exponent);
    }
}
