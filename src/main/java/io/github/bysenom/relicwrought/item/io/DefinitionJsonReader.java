package io.github.bysenom.relicwrought.item.io;

import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.item.model.KeyedDefinition;

public interface DefinitionJsonReader<T extends KeyedDefinition> {
    T read(JsonObject json, String defaultNamespace);
}
