package io.github.bysenom.relicwrought.item.registry;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.KeyedDefinition;

import java.util.Collection;
import java.util.Optional;

public interface DataRegistry<T extends KeyedDefinition> {
    void register(T definition);

    Optional<T> get(DefinitionKey id);

    Collection<T> values();

    int size();
}
