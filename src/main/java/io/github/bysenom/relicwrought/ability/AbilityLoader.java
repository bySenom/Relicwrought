package io.github.bysenom.relicwrought.ability;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class AbilityLoader {
    private static final Gson GSON = new Gson();

    private AbilityLoader() {
    }

    public static AbilityRegistry loadAbilities(String modId, Logger logger) {
        AbilityRegistry registry = new AbilityRegistry();
        List<String> errors = new ArrayList<>();

        String indexPath = "data/" + modId + "/abilities/_index.json";
        try (Reader indexReader = openResource(indexPath)) {
            JsonArray index = GSON.fromJson(indexReader, JsonArray.class);
            if (index != null) {
                AbilityDefinitionJsonReader reader = new AbilityDefinitionJsonReader();
                for (JsonElement element : index) {
                    String fileName = element.getAsString();
                    String definitionPath = "data/" + modId + "/abilities/" + fileName;
                    try (Reader definitionReader = openResource(definitionPath)) {
                        registry.register(reader.read(JsonParser.parseReader(definitionReader).getAsJsonObject(), modId));
                    } catch (RuntimeException | IOException exception) {
                        String message = "Failed to load " + definitionPath + ": " + exception.getMessage();
                        errors.add(message);
                        logger.warn(message);
                    }
                }
            }
        } catch (RuntimeException | IOException exception) {
            String message = "Failed to load index " + indexPath + ": " + exception.getMessage();
            errors.add(message);
            logger.warn(message);
        }

        if (!errors.isEmpty()) {
            logger.warn("Ability loading completed with {} error(s)", errors.size());
        }
        registry.logSummary(logger);
        return registry;
    }

    private static Reader openResource(String path) throws IOException {
        var stream = AbilityLoader.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("Missing resource: " + path);
        }
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }
}
