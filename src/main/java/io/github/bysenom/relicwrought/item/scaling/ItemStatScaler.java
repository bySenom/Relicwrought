package io.github.bysenom.relicwrought.item.scaling;

import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;

public final class ItemStatScaler {
    private final DataRegistry<ScalingProfile> scalingProfiles;

    public ItemStatScaler(DataRegistry<ScalingProfile> scalingProfiles) {
        this.scalingProfiles = scalingProfiles;
    }

    public WeaponBaseStats scaleWeaponBaseStats(ItemBaseDefinition itemBase, ScalingContext context) {
        ScalingProfile damageProfile = requireProfile(itemBase, itemBase.scaling().damageProfile(), ScalingStat.WEAPON_DAMAGE);
        ScalingProfile durabilityProfile = requireDurabilityProfile(itemBase);

        double minimumDamage = damageProfile.valueAt(context.itemLevel()) * itemBase.scaling().minimumDamageMultiplier();
        double maximumDamage = damageProfile.valueAt(context.itemLevel()) * itemBase.scaling().maximumDamageMultiplier();
        minimumDamage += itemBase.baseStats().damageMin();
        maximumDamage += itemBase.baseStats().damageMax();
        minimumDamage *= context.quality().multiplier();
        maximumDamage *= context.quality().multiplier();

        long durability = scaledDurability(durabilityProfile, itemBase, context);

        return new WeaponBaseStats(
                NumberSafety.requireFiniteNonNegative(minimumDamage, "scaled weapon minimum damage"),
                NumberSafety.requireFiniteNonNegative(maximumDamage, "scaled weapon maximum damage"),
                itemBase.baseStats().attackSpeed(),
                durability
        );
    }

    public ArmorBaseStats scaleArmorBaseStats(ItemBaseDefinition itemBase, ScalingContext context) {
        ScalingProfile armorProfile = requireProfile(itemBase, itemBase.scaling().armorProfile(), ScalingStat.ARMOR);
        ScalingProfile durabilityProfile = requireDurabilityProfile(itemBase);

        double armor = armorProfile.valueAt(context.itemLevel()) * itemBase.scaling().armorMultiplier();
        armor += itemBase.baseStats().armor();
        armor *= context.quality().multiplier();

        return new ArmorBaseStats(
                NumberSafety.requireFiniteNonNegative(armor, "scaled armor"),
                scaledDurability(durabilityProfile, itemBase, context)
        );
    }

    public ToolBaseStats scaleToolBaseStats(ItemBaseDefinition itemBase, ScalingContext context) {
        ScalingProfile miningSpeedProfile = requireProfile(itemBase, itemBase.scaling().miningSpeedProfile(), ScalingStat.MINING_SPEED);
        ScalingProfile miningTierProfile = requireProfile(itemBase, itemBase.scaling().miningTierProfile(), ScalingStat.MINING_TIER);
        ScalingProfile durabilityProfile = requireDurabilityProfile(itemBase);

        double miningSpeed = miningSpeedProfile.valueAt(context.itemLevel()) * itemBase.scaling().miningSpeedMultiplier();
        miningSpeed += itemBase.baseStats().miningSpeed();
        miningSpeed *= context.quality().multiplier();

        int miningTier = (int) Math.max(itemBase.baseStats().miningTier(), miningTierProfile.longValueAt(context.itemLevel()));

        return new ToolBaseStats(
                NumberSafety.requireFiniteNonNegative(miningSpeed, "scaled mining speed"),
                miningTier,
                scaledDurability(durabilityProfile, itemBase, context)
        );
    }

    private long scaledDurability(ScalingProfile durabilityProfile, ItemBaseDefinition itemBase, ScalingContext context) {
        double durability = durabilityProfile.valueAt(context.itemLevel()) * itemBase.scaling().durabilityMultiplier();
        durability += itemBase.baseStats().durability();
        return NumberSafety.toSafeLong(durability, "scaled durability");
    }

    private ScalingProfile requireDurabilityProfile(ItemBaseDefinition itemBase) {
        ScalingProfile profile = scalingProfiles.get(itemBase.scaling().durabilityProfile())
                .orElseThrow(() -> new IllegalStateException("Missing durability scaling profile " + itemBase.scaling().durabilityProfile() + " for item base " + itemBase.id()));

        return switch (profile.stat()) {
            case WEAPON_DURABILITY, ARMOR_DURABILITY, TOOL_DURABILITY, SHIELD_DURABILITY -> profile;
            default -> throw new IllegalStateException("Scaling profile " + profile.id() + " is not a durability profile: " + profile.stat());
        };
    }

    private ScalingProfile requireProfile(ItemBaseDefinition itemBase, io.github.bysenom.relicwrought.item.model.DefinitionKey profileId, ScalingStat expectedStat) {
        ScalingProfile profile = scalingProfiles.get(profileId)
                .orElseThrow(() -> new IllegalStateException("Missing scaling profile " + profileId + " for item base " + itemBase.id()));
        if (profile.stat() != expectedStat) {
            throw new IllegalStateException("Scaling profile " + profile.id() + " has stat " + profile.stat() + " but expected " + expectedStat);
        }
        return profile;
    }
}
