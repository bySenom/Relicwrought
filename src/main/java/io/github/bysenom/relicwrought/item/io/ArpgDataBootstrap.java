package io.github.bysenom.relicwrought.item.io;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.bysenom.relicwrought.item.generation.RarityDefinition;
import io.github.bysenom.relicwrought.item.generation.RarityDefinitionJsonReader;
import io.github.bysenom.relicwrought.item.model.AffixDefinition;
import io.github.bysenom.relicwrought.item.model.AffixGroupDefinition;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.registry.DefinitionLoadResult;
import io.github.bysenom.relicwrought.item.registry.InMemoryDataRegistry;
import io.github.bysenom.relicwrought.item.scaling.ScalingProfile;
import io.github.bysenom.relicwrought.loot.LootProfileDefinition;
import io.github.bysenom.relicwrought.loot.LootProfileDefinitionJsonReader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ArpgDataBootstrap {
    private static final Gson GSON = new Gson();

    private ArpgDataBootstrap() {
    }

    public static DefinitionLoadResult loadBundledDefinitions(String modId, Logger logger) {
        InMemoryDataRegistry<ItemBaseDefinition> itemBases = new InMemoryDataRegistry<>();
        InMemoryDataRegistry<AffixDefinition> affixes = new InMemoryDataRegistry<>();
        InMemoryDataRegistry<AffixGroupDefinition> affixGroups = new InMemoryDataRegistry<>();
        InMemoryDataRegistry<ScalingProfile> scalingProfiles = new InMemoryDataRegistry<>();
        InMemoryDataRegistry<RarityDefinition> rarities = new InMemoryDataRegistry<>();
        InMemoryDataRegistry<LootProfileDefinition> lootProfiles = new InMemoryDataRegistry<>();
        List<String> errors = new ArrayList<>();

        loadIndexedDefinitions(modId, "scaling_profiles", new ScalingProfileJsonReader(), scalingProfiles, errors, logger);
        loadIndexedDefinitions(modId, "item_bases", new ItemBaseDefinitionJsonReader(), itemBases, errors, logger);
        loadIndexedDefinitions(modId, "affix_groups", new AffixGroupDefinitionJsonReader(), affixGroups, errors, logger);
        loadIndexedDefinitions(modId, "affixes", new AffixDefinitionJsonReader(), affixes, errors, logger);
        loadIndexedDefinitions(modId, "rarities", new RarityDefinitionJsonReader(), rarities, errors, logger);
        loadIndexedDefinitions(modId, "loot_profiles", new LootProfileDefinitionJsonReader(), lootProfiles, errors, logger);

        return new DefinitionLoadResult(itemBases, affixes, affixGroups, scalingProfiles, rarities, lootProfiles, errors);
    }

    private static <T extends io.github.bysenom.relicwrought.item.model.KeyedDefinition> void loadIndexedDefinitions(
            String modId,
            String folder,
            DefinitionJsonReader<T> reader,
            InMemoryDataRegistry<T> registry,
            List<String> errors,
            Logger logger
    ) {
        String indexPath = "data/" + modId + "/" + folder + "/_index.json";
        try (Reader indexReader = openResource(indexPath)) {
            JsonArray index = GSON.fromJson(indexReader, JsonArray.class);
            for (JsonElement element : index) {
                String fileName = element.getAsString();
                String definitionPath = "data/" + modId + "/" + folder + "/" + fileName;
                try (Reader definitionReader = openResource(definitionPath)) {
                    registry.register(reader.read(JsonParser.parseReader(definitionReader).getAsJsonObject(), modId));
                } catch (RuntimeException | IOException exception) {
                    String message = "Failed to load " + definitionPath + ": " + exception.getMessage();
                    errors.add(message);
                    logger.warn(message);
                }
            }
        } catch (RuntimeException | IOException exception) {
            String message = "Failed to load index " + indexPath + ": " + exception.getMessage();
            errors.add(message);
            logger.warn(message);
        }
    }

    private static Reader openResource(String path) throws IOException {
        var stream = ArpgDataBootstrap.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("Missing resource: " + path);
        }
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }
}
