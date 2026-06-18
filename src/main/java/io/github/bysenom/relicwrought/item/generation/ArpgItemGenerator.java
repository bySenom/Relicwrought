package io.github.bysenom.relicwrought.item.generation;

import io.github.bysenom.relicwrought.item.affix.AffixGenerationPolicy;
import io.github.bysenom.relicwrought.item.affix.AffixGenerationMode;
import io.github.bysenom.relicwrought.item.affix.AffixGenerationRequest;
import io.github.bysenom.relicwrought.item.affix.AffixGenerationResult;
import io.github.bysenom.relicwrought.item.affix.AffixGenerator;
import io.github.bysenom.relicwrought.item.affix.AffixSlotLimits;
import io.github.bysenom.relicwrought.item.model.AffixRoll;
import io.github.bysenom.relicwrought.item.model.AffixType;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ItemCategory;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.model.Rarity;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;
import io.github.bysenom.relicwrought.item.scaling.ItemQuality;
import io.github.bysenom.relicwrought.item.scaling.ItemStatScaler;
import io.github.bysenom.relicwrought.item.scaling.ScalingContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.UUID;

public final class ArpgItemGenerator {
    private final DataRegistry<ItemBaseDefinition> itemBases;
    private final DataRegistry<RarityDefinition> rarities;
    private final AffixGenerator affixGenerator;
    private final ItemStatScaler statScaler;
    private final ArpgItemStackService persistenceService;
    private final RaritySelector raritySelector;
    private final SlotDistributionSelector slotSelector;
    private final QualityGenerator qualityGenerator;
    private final ItemBaseSelector itemBaseSelector;

    public ArpgItemGenerator(
            DataRegistry<ItemBaseDefinition> itemBases,
            DataRegistry<RarityDefinition> rarities,
            AffixGenerator affixGenerator,
            ItemStatScaler statScaler,
            ArpgItemStackService persistenceService
    ) {
        this.itemBases = itemBases;
        this.rarities = rarities;
        this.affixGenerator = affixGenerator;
        this.statScaler = statScaler;
        this.persistenceService = persistenceService;
        this.raritySelector = new RaritySelector(rarities);
        this.slotSelector = new SlotDistributionSelector();
        this.qualityGenerator = new QualityGenerator();
        this.itemBaseSelector = new ItemBaseSelector(itemBases);
    }

    public ItemGenerationResult generate(ItemGenerationRequest request) {
        try {
            GenerationSeedSplitter seeds = new GenerationSeedSplitter(request.seed());

            ItemBaseDefinition base = resolveItemBase(request, seeds.itemBaseSeed());
            if (base == null) {
                return ItemGenerationResult.failure(GenerationErrorCode.MISSING_ITEM_BASE, "Item base not found: " + request.itemBaseId());
            }

            RarityDefinition rarity = resolveRarity(request, base, seeds.raritySeed());
            if (rarity == null) {
                return ItemGenerationResult.failure(GenerationErrorCode.INVALID_RARITY, "Requested rarity not found");
            }

            try {
                raritySelector.validateRarityForLevel(rarity, request.itemLevel());
            } catch (IllegalArgumentException e) {
                return ItemGenerationResult.failure(GenerationErrorCode.RARITY_NOT_UNLOCKED, e.getMessage());
            }

            AffixSlotLimits slotLimits;
            try {
                slotLimits = slotSelector.select(rarity, seeds.slotsSeed(), limits -> {
                    int availablePrefixes = affixGenerator.countAvailableAffixes(
                            AffixType.PREFIX, base, request.itemLevel());
                    int availableSuffixes = affixGenerator.countAvailableAffixes(
                            AffixType.SUFFIX, base, request.itemLevel());
                    return limits.prefixes() <= availablePrefixes
                            && limits.suffixes() <= availableSuffixes;
                });
            } catch (Exception e) {
                return ItemGenerationResult.failure(GenerationErrorCode.NO_VALID_SLOT_DISTRIBUTION, e.getMessage());
            }

            int quality;
            try {
                quality = qualityGenerator.resolveQuality(request.quality(), seeds.qualitySeed());
            } catch (IllegalArgumentException e) {
                return ItemGenerationResult.failure(GenerationErrorCode.INVALID_QUALITY, e.getMessage());
            }

            ItemQuality itemQuality = ItemQuality.of(quality);
            ScalingContext scalingContext = ScalingContext.of(request.itemLevel(), itemQuality);

            scaleBaseStats(base, scalingContext);

            List<AffixRoll> implicitRolls = rollImplicitAffixes(base, seeds.affixSeed());

            AffixGenerationResult affixResult;
            try {
                AffixGenerationPolicy policy = new AffixGenerationPolicy(AffixGenerationMode.STRICT, 2);
                AffixGenerationRequest affixRequest = new AffixGenerationRequest(
                        base,
                        request.itemLevel(),
                        slotLimits,
                        seeds.affixSeed().nextLong(),
                        policy
                );
                affixResult = affixGenerator.generate(affixRequest);
            } catch (Exception e) {
                return ItemGenerationResult.failure(GenerationErrorCode.AFFIX_GENERATION_FAILED, e.getMessage());
            }

            if (!affixResult.complete()) {
                List<String> messages = new ArrayList<>(affixResult.messages());
                messages.add(0, "Affix generation incomplete for " + rarity.id());
                return ItemGenerationResult.failure(GenerationErrorCode.AFFIX_GENERATION_FAILED, messages);
            }

            UUID itemId = new UUID(seeds.uuidSeed().nextLong(), seeds.uuidSeed().nextLong());

            ArpgItemData itemData;
            try {
                itemData = new ArpgItemData(
                        io.github.bysenom.relicwrought.item.ItemDataVersions.CURRENT,
                        itemId,
                        base.id(),
                        request.itemLevel(),
                        0,
                        Rarity.valueOf(rarity.id().path().toUpperCase()),
                        quality,
                        request.seed(),
                        false,
                        implicitRolls,
                        affixResult.prefixes(),
                        affixResult.suffixes()
                );
            } catch (Exception e) {
                return ItemGenerationResult.failure(GenerationErrorCode.INVALID_GENERATED_ITEM, e.getMessage());
            }

            validateGeneratedItem(itemData, base, slotLimits);

            ItemStack resultStack;
            try {
                resultStack = createItemStack(base);
            } catch (Exception e) {
                return ItemGenerationResult.failure(GenerationErrorCode.UNSUPPORTED_ITEM_CATEGORY, e.getMessage());
            }

            if (request.persistentWrite() && request.targetStack() != null) {
                var writeResult = persistenceService.write(request.targetStack(), itemData);
                if (!writeResult.succeeded()) {
                    return ItemGenerationResult.failure(GenerationErrorCode.PERSISTENCE_WRITE_FAILED, writeResult.messages());
                }
                resultStack = request.targetStack();
            } else {
                var writeResult = persistenceService.write(resultStack, itemData);
                if (!writeResult.succeeded()) {
                    return ItemGenerationResult.failure(GenerationErrorCode.PERSISTENCE_WRITE_FAILED, writeResult.messages());
                }
            }

            return new ItemGenerationResult(
                    true,
                    itemData,
                    resultStack,
                    base.id(),
                    request.itemLevel(),
                    quality,
                    rarity.id(),
                    slotLimits.prefixes(),
                    slotLimits.suffixes(),
                    request.seed(),
                    List.of(),
                    GenerationErrorCode.NONE
            );
        } catch (Exception e) {
            return ItemGenerationResult.failure(GenerationErrorCode.INVALID_GENERATED_ITEM, List.of("Unexpected error: " + e.getMessage()));
        }
    }

    private ItemBaseDefinition resolveItemBase(ItemGenerationRequest request, SplittableRandom baseRandom) {
        if (request.itemBaseId() != null) {
            return itemBaseSelector.selectExplicit(request.itemBaseId()).orElse(null);
        }
        try {
            return itemBaseSelector.selectFromPool(
                    request.allowedCategories(),
                    request.requiredTags(),
                    request.excludedTags(),
                    request.allowedBaseIds(),
                    baseRandom
            );
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private RarityDefinition resolveRarity(ItemGenerationRequest request, ItemBaseDefinition base, SplittableRandom rarityRandom) {
        if (request.rarityId() != null) {
            return raritySelector.selectExplicit(request.rarityId()).orElse(null);
        }
        try {
            return raritySelector.selectWeighted(request.itemLevel(), rarityRandom);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private List<AffixRoll> rollImplicitAffixes(ItemBaseDefinition base, SplittableRandom affixRandom) {
        return List.of();
    }

    private void scaleBaseStats(ItemBaseDefinition base, ScalingContext context) {
        ItemCategory category = base.category();
        switch (category) {
            case SWORD, COMBAT_AXE, BOW, CROSSBOW -> statScaler.scaleWeaponBaseStats(base, context);
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS, SHIELD -> statScaler.scaleArmorBaseStats(base, context);
            case PICKAXE, TOOL_AXE, SHOVEL, HOE -> statScaler.scaleToolBaseStats(base, context);
        }
    }

    private void validateGeneratedItem(ArpgItemData data, ItemBaseDefinition base, AffixSlotLimits slotLimits) {
        if (data.prefixes().size() != slotLimits.prefixes()) {
            throw new IllegalStateException("Expected " + slotLimits.prefixes() + " prefixes but got " + data.prefixes().size());
        }
        if (data.suffixes().size() != slotLimits.suffixes()) {
            throw new IllegalStateException("Expected " + slotLimits.suffixes() + " suffixes but got " + data.suffixes().size());
        }
    }

    private ItemStack createItemStack(ItemBaseDefinition base) {
        net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.parse(base.minecraftItemId());
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
        if (item == null) {
            throw new IllegalArgumentException("Unknown Minecraft item: " + base.minecraftItemId());
        }
        return new ItemStack(item, 1);
    }
}
