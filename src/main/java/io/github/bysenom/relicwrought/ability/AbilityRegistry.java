package io.github.bysenom.relicwrought.ability;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.registry.InMemoryDataRegistry;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;

public final class AbilityRegistry {
    private final InMemoryDataRegistry<AbilityDefinition> registry = new InMemoryDataRegistry<>();

    public void register(AbilityDefinition ability) {
        registry.register(ability);
    }

    public Optional<AbilityDefinition> get(DefinitionKey id) {
        return registry.get(id);
    }

    public AbilityDefinition getOrThrow(DefinitionKey id) {
        return get(id).orElseThrow(() -> new IllegalArgumentException("Unknown ability: " + id));
    }

    public Collection<AbilityDefinition> all() {
        return registry.values();
    }

    public int size() {
        return registry.size();
    }

    public void logSummary(Logger logger) {
        logger.info("Ability registry loaded {} abilities", size());
        for (AbilityDefinition a : all()) {
            logger.info("  {} (class: {}, type: {}, resource: {})",
                    a.id(), a.allowedClasses(), a.effectType(), a.resourceType());
        }
    }
}
