package io.github.bysenom.relicwrought.item.model;

import java.util.Objects;

public record DefinitionKey(String namespace, String path) {
    private static final String VALID_PATH = "[a-z0-9_./-]+";

    public DefinitionKey {
        namespace = requireValidPart(namespace, "namespace");
        path = requireValidPart(path, "path");
    }

    public static DefinitionKey parse(String value, String defaultNamespace) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(defaultNamespace, "defaultNamespace");

        int separator = value.indexOf(':');
        if (separator >= 0) {
            return new DefinitionKey(value.substring(0, separator), value.substring(separator + 1));
        }

        return new DefinitionKey(defaultNamespace, value);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    private static String requireValidPart(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        if (!normalized.matches(VALID_PATH)) {
            throw new IllegalArgumentException(name + " contains invalid characters: " + value);
        }
        return normalized;
    }
}
