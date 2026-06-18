package io.github.bysenom.relicwrought.item.registry;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.KeyedDefinition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryDataRegistry<T extends KeyedDefinition> implements DataRegistry<T> {
    private final Map<DefinitionKey, T> definitions = new LinkedHashMap<>();

    @Override
    public void register(T definition) {
        if (definitions.containsKey(definition.id())) {
            throw new IllegalArgumentException("Duplicate definition id: " + definition.id());
        }
        definitions.put(definition.id(), definition);
    }

    @Override
    public Optional<T> get(DefinitionKey id) {
        return Optional.ofNullable(definitions.get(id));
    }

    @Override
    public Collection<T> values() {
        return definitions.values();
    }

    @Override
    public int size() {
        return definitions.size();
    }
}
