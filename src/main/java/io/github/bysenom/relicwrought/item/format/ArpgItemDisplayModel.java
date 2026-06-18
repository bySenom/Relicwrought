package io.github.bysenom.relicwrought.item.format;

import io.github.bysenom.relicwrought.item.format.ArpgItemDisplayNameService.ArpgItemDisplayName;
import io.github.bysenom.relicwrought.item.model.AffixComponentRoll;
import io.github.bysenom.relicwrought.item.model.AffixOperation;
import io.github.bysenom.relicwrought.item.model.AffixRoll;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ItemCategory;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadResult;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadStatus;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;
import io.github.bysenom.relicwrought.item.scaling.ScalingProfile;

import java.util.ArrayList;
import java.util.List;

public record ArpgItemDisplayModel(
        boolean hasArpgData,
        ArpgItemDisplayName displayName,
        ArpgItemData itemData,
        ItemBaseDefinition itemBase,
        List<DisplayLine> baseStatLines,
        List<DisplayLine> implicitLines,
        List<DisplayLine> prefixLines,
        List<DisplayLine> suffixLines,
        List<String> tooltipStatus,
        List<DisplayLine> technicalLines
) {
    public static ArpgItemDisplayModel fromReadResult(
            ArpgItemReadResult readResult,
            DataRegistry<ItemBaseDefinition> itemBases,
            DataRegistry<ScalingProfile> scalingProfiles
    ) {
        ArpgItemDisplayName name = ArpgItemDisplayNameService.resolve(readResult, itemBases);

        if (readResult.status() == ArpgItemReadStatus.NOT_ARPG_ITEM) {
            return new ArpgItemDisplayModel(false, name, null, null, List.of(), List.of(), List.of(), List.of(), List.of("not_arpg_item"), List.of());
        }

        List<String> statusMessages = new ArrayList<>();
        if (readResult.status() == ArpgItemReadStatus.INVALID) {
            statusMessages.addAll(readResult.messages());
        } else if (readResult.status() == ArpgItemReadStatus.UNSUPPORTED_VERSION) {
            statusMessages.add("unsupported_version");
        } else if (readResult.status() == ArpgItemReadStatus.MISSING_DEFINITION) {
            statusMessages.add("missing_definition");
        }

        ArpgItemData data = readResult.data().orElse(null);
        if (data == null) {
            return new ArpgItemDisplayModel(true, name, null, null, List.of(), List.of(), List.of(), List.of(), statusMessages, List.of());
        }

        ItemBaseDefinition base = itemBases.get(data.itemBaseId()).orElse(null);

        if (readResult.status() == ArpgItemReadStatus.MIGRATED) {
            statusMessages.add("migrated");
        }

        List<DisplayLine> baseStatLines = buildBaseStatLines(data, base, scalingProfiles);
        List<DisplayLine> implicitLines = buildAffixLines(data.implicitAffixes(), false);
        List<DisplayLine> prefixLines = buildAffixLines(data.prefixes(), true);
        List<DisplayLine> suffixLines = buildAffixLines(data.suffixes(), true);
        List<DisplayLine> technicalLines = buildTechnicalLines(data, base, readResult, scalingProfiles);

        return new ArpgItemDisplayModel(
                true, name, data, base,
                baseStatLines, implicitLines, prefixLines, suffixLines,
                statusMessages, technicalLines
        );
    }

    public static ArpgItemDisplayModel fromReadResult(
            ArpgItemReadResult readResult,
            DataRegistry<ItemBaseDefinition> itemBases
    ) {
        return fromReadResult(readResult, itemBases, null);
    }

    private static List<DisplayLine> buildBaseStatLines(
            ArpgItemData data, ItemBaseDefinition base, DataRegistry<ScalingProfile> scalingProfiles
    ) {
        List<DisplayLine> lines = new ArrayList<>();
        lines.add(new DisplayLine("level", String.valueOf(data.itemLevel().value()), "tooltip.relicwrought.level", false));
        if (data.quality() > 0) {
            lines.add(new DisplayLine("quality", String.valueOf(data.quality()), "tooltip.relicwrought.quality", false));
        }

        if (base != null && scalingProfiles != null) {
            addScaledBaseStatLines(lines, data, base, scalingProfiles);
        }

        return lines;
    }

    private static void addScaledBaseStatLines(
            List<DisplayLine> lines, ArpgItemData data, ItemBaseDefinition base,
            DataRegistry<ScalingProfile> scalingProfiles
    ) {
        LocalAffixResolver.LocalAffixModifiers localMods = LocalAffixResolver.resolve(data);

        ItemCategory category = base.category();
        boolean isWeapon = category == ItemCategory.SWORD || category == ItemCategory.COMBAT_AXE;
        boolean isArmor = category == ItemCategory.HELMET || category == ItemCategory.CHESTPLATE
                || category == ItemCategory.LEGGINGS || category == ItemCategory.BOOTS;
        boolean isTool = category == ItemCategory.PICKAXE || category == ItemCategory.TOOL_AXE
                || category == ItemCategory.SHOVEL || category == ItemCategory.HOE;

        ItemLevel itemLevel = data.itemLevel();
        double qualityMultiplier = 1.0 + (data.quality() / 100.0);

        if (isWeapon) {
            ScalingProfile damageProfile = scalingProfiles.get(base.scaling().damageProfile()).orElse(null);
            if (damageProfile != null) {
                double baseMin = base.baseStats().damageMin();
                double baseMax = base.baseStats().damageMax();
                double damageVal = damageProfile.valueAt(itemLevel);
                double scaledMin = (baseMin + damageVal * base.scaling().minimumDamageMultiplier()) * qualityMultiplier;
                double scaledMax = (baseMax + damageVal * base.scaling().maximumDamageMultiplier()) * qualityMultiplier;

                scaledMin += localMods.flatModifiers().getOrDefault("minimum_damage", 0.0);
                scaledMax += localMods.flatModifiers().getOrDefault("maximum_damage", 0.0);
                scaledMin *= (1.0 + localMods.percentModifiers().getOrDefault("minimum_damage", 0.0) / 100.0);
                scaledMax *= (1.0 + localMods.percentModifiers().getOrDefault("maximum_damage", 0.0) / 100.0);

                lines.add(new DisplayLine("damage",
                        ArpgNumberFormatter.formatDouble(scaledMin, 0) + "\u2013" + ArpgNumberFormatter.formatDouble(scaledMax, 0),
                        "tooltip.relicwrought.damage", false));
            }
            if (base.baseStats().attackSpeed() > 0) {
                double attackSpeed = base.baseStats().attackSpeed();
                attackSpeed += localMods.flatModifiers().getOrDefault("attack_speed", 0.0);
                attackSpeed *= (1.0 + localMods.percentModifiers().getOrDefault("attack_speed", 0.0) / 100.0);
                lines.add(new DisplayLine("attack_speed",
                        ArpgNumberFormatter.formatDouble(attackSpeed, 2),
                        "tooltip.relicwrought.attack_speed", false));
            }
        }

        if (isArmor) {
            ScalingProfile armorProfile = scalingProfiles.get(base.scaling().armorProfile()).orElse(null);
            if (armorProfile != null) {
                double armorVal = (base.baseStats().armor() + armorProfile.valueAt(itemLevel) * base.scaling().armorMultiplier()) * qualityMultiplier;
                armorVal += localMods.flatModifiers().getOrDefault("armor", 0.0);
                armorVal *= (1.0 + localMods.percentModifiers().getOrDefault("armor", 0.0) / 100.0);
                lines.add(new DisplayLine("armor",
                        ArpgNumberFormatter.formatDouble(armorVal, 0),
                        "tooltip.relicwrought.armor", false));
            }
        }

        if (isTool) {
            ScalingProfile miningSpeedProfile = scalingProfiles.get(base.scaling().miningSpeedProfile()).orElse(null);
            if (miningSpeedProfile != null) {
                double speedVal = (base.baseStats().miningSpeed() + miningSpeedProfile.valueAt(itemLevel) * base.scaling().miningSpeedMultiplier()) * qualityMultiplier;
                speedVal += localMods.flatModifiers().getOrDefault("mining_speed", 0.0);
                speedVal *= (1.0 + localMods.percentModifiers().getOrDefault("mining_speed", 0.0) / 100.0);
                lines.add(new DisplayLine("mining_speed",
                        ArpgNumberFormatter.formatDouble(speedVal, 1),
                        "tooltip.relicwrought.mining_speed", false));
            }
            ScalingProfile miningTierProfile = scalingProfiles.get(base.scaling().miningTierProfile()).orElse(null);
            if (miningTierProfile != null) {
                int tierVal = (int) Math.max(base.baseStats().miningTier(), miningTierProfile.longValueAt(itemLevel));
                long localMiningTier = Math.round(localMods.flatModifiers().getOrDefault("mining_tier", 0.0));
                tierVal = (int) Math.max(tierVal + localMiningTier, 0);
                lines.add(new DisplayLine("mining_tier",
                        String.valueOf(tierVal),
                        "tooltip.relicwrought.mining_tier", false));
            }
        }

        if (isWeapon || isArmor || isTool) {
            ScalingProfile durabilityProfile = scalingProfiles.get(base.scaling().durabilityProfile()).orElse(null);
            if (durabilityProfile != null) {
                double durVal = base.baseStats().durability() + durabilityProfile.valueAt(itemLevel) * base.scaling().durabilityMultiplier();
                durVal += localMods.flatModifiers().getOrDefault("maximum_durability", 0.0);
                durVal *= (1.0 + localMods.percentModifiers().getOrDefault("maximum_durability", 0.0) / 100.0);
                lines.add(new DisplayLine("durability",
                        ArpgNumberFormatter.formatInt((int) Math.round(durVal)),
                        "tooltip.relicwrought.durability", false));
            }
        }
    }

    private static List<DisplayLine> buildAffixLines(List<AffixRoll> rolls, boolean showTier) {
        List<DisplayLine> lines = new ArrayList<>();
        for (AffixRoll roll : rolls) {
            for (int i = 0; i < roll.componentRolls().size(); i++) {
                AffixComponentRoll comp = roll.componentRolls().get(i);
                StringBuilder prefix = new StringBuilder();
                if (showTier && i == 0) {
                    prefix.append("T").append(roll.tier().displayTier()).append(" ");
                } else if (showTier) {
                    prefix.append("   ");
                }
                prefix.append(formatAffixValue(comp.value(), comp.operation()));
                String translationKey = ArpgStatDisplayResolver.resolveTranslationKey(comp.stat());
                lines.add(new DisplayLine(roll.affixId().toString(), prefix.toString(), translationKey, false));
            }
        }
        return lines;
    }

    private static String formatAffixValue(double value, AffixOperation operation) {
        return switch (operation) {
            case ADD_FLAT, ADDITIVE -> {
                if (value == Math.floor(value) && !Double.isInfinite(value)) {
                    yield ArpgNumberFormatter.formatSigned(value, 0);
                }
                yield ArpgNumberFormatter.formatSigned(value, 1);
            }
            case ADDITIVE_PERCENT -> ArpgNumberFormatter.formatSignedPercent(value, 1);
            case MULTIPLICATIVE_PERCENT -> ArpgNumberFormatter.formatSignedPercent(value, 1);
        };
    }

    private static List<DisplayLine> buildTechnicalLines(
            ArpgItemData data, ItemBaseDefinition base, ArpgItemReadResult result,
            DataRegistry<ScalingProfile> scalingProfiles
    ) {
        List<DisplayLine> lines = new ArrayList<>();
        lines.add(new DisplayLine("UUID", data.itemId().toString(), true));
        lines.add(new DisplayLine("item_base", data.itemBaseId().toString(), true));
        if (base != null) {
            lines.add(new DisplayLine("minecraft_item", base.minecraftItemId(), true));
        }
        lines.add(new DisplayLine("data_version", String.valueOf(data.dataVersion()), true));
        lines.add(new DisplayLine("seed", String.valueOf(data.seed()), true));
        lines.add(new DisplayLine("rarity", data.rarity().name(), true));
        lines.add(new DisplayLine("prefixes", String.valueOf(data.prefixes().size()), true));
        lines.add(new DisplayLine("suffixes", String.valueOf(data.suffixes().size()), true));
        lines.add(new DisplayLine("implicits", String.valueOf(data.implicitAffixes().size()), true));
        lines.add(new DisplayLine("status", result.status().name(), true));
        if (!result.messages().isEmpty()) {
            lines.add(new DisplayLine("messages", String.join("; ", result.messages()), true));
        }

        if (base != null && scalingProfiles != null) {
            addBaseStatTechnicalLines(lines, data, base, scalingProfiles);
        }

        return lines;
    }

    private static void addBaseStatTechnicalLines(
            List<DisplayLine> lines, ArpgItemData data, ItemBaseDefinition base,
            DataRegistry<ScalingProfile> scalingProfiles
    ) {
        ItemCategory category = base.category();
        boolean isWeapon = category == ItemCategory.SWORD || category == ItemCategory.COMBAT_AXE;
        boolean isArmor = category == ItemCategory.HELMET || category == ItemCategory.CHESTPLATE
                || category == ItemCategory.LEGGINGS || category == ItemCategory.BOOTS;
        boolean isTool = category == ItemCategory.PICKAXE || category == ItemCategory.TOOL_AXE
                || category == ItemCategory.SHOVEL || category == ItemCategory.HOE;

        double qualityMultiplier = 1.0 + (data.quality() / 100.0);
        int itemLevel = data.itemLevel().value();

        if (isWeapon) {
            ScalingProfile damageProfile = scalingProfiles.get(base.scaling().damageProfile()).orElse(null);
            if (damageProfile != null) {
                double damageVal = damageProfile.valueAt(data.itemLevel());
                double baseMin = (base.baseStats().damageMin() + damageVal * base.scaling().minimumDamageMultiplier()) * qualityMultiplier;
                double baseMax = (base.baseStats().damageMax() + damageVal * base.scaling().maximumDamageMultiplier()) * qualityMultiplier;
                lines.add(new DisplayLine("base_damage",
                        ArpgNumberFormatter.formatDouble(baseMin, 0) + "\u2013" + ArpgNumberFormatter.formatDouble(baseMax, 0), true));
            }
        }
        if (isArmor) {
            ScalingProfile armorProfile = scalingProfiles.get(base.scaling().armorProfile()).orElse(null);
            if (armorProfile != null) {
                double baseArmor = (base.baseStats().armor() + armorProfile.valueAt(data.itemLevel()) * base.scaling().armorMultiplier()) * qualityMultiplier;
                lines.add(new DisplayLine("base_armor", ArpgNumberFormatter.formatDouble(baseArmor, 0), true));
            }
        }

        LocalAffixResolver.LocalAffixModifiers localMods = LocalAffixResolver.resolve(data);
        for (var entry : localMods.flatModifiers().entrySet()) {
            lines.add(new DisplayLine("local_flat_" + entry.getKey(),
                    ArpgNumberFormatter.formatSigned(entry.getValue(), 0), true));
        }
        for (var entry : localMods.percentModifiers().entrySet()) {
            lines.add(new DisplayLine("local_pct_" + entry.getKey(),
                    ArpgNumberFormatter.formatSignedPercent(entry.getValue(), 1), true));
        }
    }

    public record DisplayLine(String label, String value, String translationKey, boolean isTechnical) {
        public DisplayLine(String label, String value, boolean isTechnical) {
            this(label, value, null, isTechnical);
        }
    }
}
