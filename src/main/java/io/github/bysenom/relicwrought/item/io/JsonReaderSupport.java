package io.github.bysenom.relicwrought.item.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public final class JsonReaderSupport {
    private JsonReaderSupport() {
    }

    public static String requiredString(JsonObject json, String name) {
        JsonElement element = json.get(name);
        if (element == null || !element.isJsonPrimitive()) {
            throw new IllegalArgumentException("Missing string field: " + name);
        }
        return element.getAsString();
    }

    public static String optionalString(JsonObject json, String name, String fallback) {
        JsonElement element = json.get(name);
        return element == null ? fallback : element.getAsString();
    }

    public static int requiredInt(JsonObject json, String name) {
        JsonElement element = json.get(name);
        if (element == null || !element.isJsonPrimitive()) {
            throw new IllegalArgumentException("Missing int field: " + name);
        }
        return element.getAsInt();
    }

    public static int optionalInt(JsonObject json, String name, int fallback) {
        JsonElement element = json.get(name);
        return element == null ? fallback : element.getAsInt();
    }

    public static double optionalDouble(JsonObject json, String name, double fallback) {
        JsonElement element = json.get(name);
        return element == null ? fallback : element.getAsDouble();
    }

    public static boolean optionalBoolean(JsonObject json, String name, boolean fallback) {
        JsonElement element = json.get(name);
        return element == null ? fallback : element.getAsBoolean();
    }

    public static JsonObject requiredObject(JsonObject json, String name) {
        JsonElement element = json.get(name);
        if (element == null || !element.isJsonObject()) {
            throw new IllegalArgumentException("Missing object field: " + name);
        }
        return element.getAsJsonObject();
    }

    public static JsonObject optionalObject(JsonObject json, String name) {
        JsonElement element = json.get(name);
        return element == null ? null : element.getAsJsonObject();
    }

    public static JsonArray optionalArray(JsonObject json, String name) {
        JsonElement element = json.get(name);
        return element == null ? new JsonArray() : element.getAsJsonArray();
    }

    public static Set<DefinitionKey> keySet(JsonObject json, String name, String defaultNamespace) {
        return set(json, name, value -> DefinitionKey.parse(value, defaultNamespace));
    }

    public static Set<String> stringSet(JsonObject json, String name) {
        return set(json, name, Function.identity());
    }

    public static <T> Set<T> enumSet(JsonObject json, String name, Function<String, T> mapper) {
        return set(json, name, value -> mapper.apply(value.toUpperCase()));
    }

    private static <T> Set<T> set(JsonObject json, String name, Function<String, T> mapper) {
        Set<T> values = new LinkedHashSet<>();
        for (JsonElement element : optionalArray(json, name)) {
            values.add(mapper.apply(element.getAsString()));
        }
        return values;
    }
}
