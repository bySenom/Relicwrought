package io.github.bysenom.relicwrought.loot;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.item.generation.ArpgItemGenerator;
import io.github.bysenom.relicwrought.item.generation.ItemGenerationRequest;
import io.github.bysenom.relicwrought.item.generation.ItemGenerationResult;
import io.github.bysenom.relicwrought.item.generation.RarityDefinition;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public final class ArpgDropGenerator {
    private final ArpgItemGenerator itemGenerator;
    private final DataRegistry<LootProfileDefinition> lootProfiles;
    private final DataRegistry<ItemBaseDefinition> itemBases;
    private final DataRegistry<RarityDefinition> rarities;
    private final LootProfileResolver profileResolver;
    private final ArpgModConfig config;

    public ArpgDropGenerator(
            ArpgItemGenerator itemGenerator,
            DataRegistry<LootProfileDefinition> lootProfiles,
            DataRegistry<ItemBaseDefinition> itemBases,
            DataRegistry<RarityDefinition> rarities,
            ArpgModConfig config
    ) {
        this.itemGenerator = itemGenerator;
        this.lootProfiles = lootProfiles;
        this.itemBases = itemBases;
        this.rarities = rarities;
        this.profileResolver = new LootProfileResolver(lootProfiles);
        this.config = config;
    }

    public LootDropResult generateDrops(
            LootProfileDefinition profile,
            LootContextData context,
            long seed
    ) {
        if (!config.enableArpgMobDrops()) {
            return LootDropResult.failure(profile.id(), context.sourceType(), seed,
                    LootErrorCode.FEATURE_DISABLED, List.of("ARPG mob drops are disabled"));
        }
        if (config.requirePlayerKill() && !context.playerKill()) {
            return LootDropResult.failure(profile.id(), context.sourceType(), seed,
                    LootErrorCode.PLAYER_KILL_REQUIRED, List.of("Player kill required for ARPG drops"));
        }

        EntityLootOverride override = profileResolver.findOverride(profile, context.entityId());

        double effectiveChance = profile.dropChance();
        effectiveChance += (override != null) ? override.dropChanceBonus() : 0.0;
        effectiveChance *= config.normalMobDropChanceMultiplier();
        if (config.lootingDropChanceMultiplier() > 0 && context.lootingLevel() > 0) {
            effectiveChance *= (1.0 + config.lootingDropChanceMultiplier() * context.lootingLevel());
        }
        effectiveChance = Math.max(0.0, Math.min(1.0, effectiveChance));

        SplittableRandom random = new SplittableRandom(seed);

        if (random.nextDouble() >= effectiveChance) {
            return LootDropResult.noDrop(profile.id(), context.sourceType(), effectiveChance, seed);
        }

        int dropRange = profile.dropCountMaximum() - profile.dropCountMinimum() + 1;
        int count = profile.dropCountMinimum() + (dropRange > 1 ? random.nextInt(dropRange) : 0);
        count += (override != null) ? override.additionalDropCount() : 0;
        count = Math.min(count, config.maximumArpgDropsPerSource());
        count = Math.max(0, count);

        if (count <= 0) {
            return LootDropResult.noDrop(profile.id(), context.sourceType(), effectiveChance, seed);
        }

        List<ItemStack> generatedItems = new ArrayList<>();
        List<Integer> itemLevels = new ArrayList<>();
        int succeeded = 0;
        int failed = 0;

        for (int i = 0; i < count; i++) {
            SplittableRandom itemRandom = random.split();
            ItemLevel resolvedLevel = ItemLevelResolver.resolve(profile, context, override, itemRandom);
            itemLevels.add(resolvedLevel.value());

            ItemGenerationRequest request = buildRequest(profile, resolvedLevel, itemRandom.nextLong(), context);
            ItemGenerationResult result = itemGenerator.generate(request);

            if (result.success() && result.itemStack() != null) {
                generatedItems.add(result.itemStack());
                succeeded++;
            } else {
                if (config.enableDebugLootLogging()) {
                    Relicwrought.LOGGER.warn("Loot generation failed for profile {}: {}",
                            profile.id(), result.errorCode());
                }
                failed++;
            }
        }

        boolean didDrop = !generatedItems.isEmpty();
        LootErrorCode error = (failed > 0 && generatedItems.isEmpty())
                ? LootErrorCode.ITEM_GENERATION_FAILED
                : LootErrorCode.NONE;
        List<String> warnings = error != LootErrorCode.NONE
                ? List.of("Some items failed to generate: " + failed + " failures")
                : List.of();

        return new LootDropResult(
                profile.id(), context.sourceType(), effectiveChance,
                didDrop, count, succeeded, failed,
                itemLevels, seed, generatedItems, error, warnings
        );
    }

    private ItemGenerationRequest buildRequest(
            LootProfileDefinition profile,
            ItemLevel itemLevel,
            long seed,
            LootContextData context
    ) {
        DefinitionKey rarityId = selectRarity(profile, seed);
        return new ItemGenerationRequest(
                null, itemLevel, seed, rarityId, null,
                profile.allowedCategories().isEmpty() ? null : profile.allowedCategories(),
                profile.requiredItemTags().isEmpty() ? null : profile.requiredItemTags(),
                profile.excludedItemTags().isEmpty() ? null : profile.excludedItemTags(),
                null, null, true, "loot:" + profile.id()
        );
    }

    private DefinitionKey selectRarity(LootProfileDefinition profile, long seed) {
        if (profile.rarityWeights().isEmpty()) return null;
        SplittableRandom random = new SplittableRandom(seed);
        long totalWeight = 0;
        for (int w : profile.rarityWeights().values()) totalWeight += w;
        if (totalWeight <= 0) return null;
        long roll = random.nextLong(totalWeight);
        long cursor = 0;
        for (var entry : profile.rarityWeights().entrySet()) {
            cursor += entry.getValue();
            if (roll < cursor) return entry.getKey();
        }
        return profile.rarityWeights().keySet().iterator().next();
    }
}
