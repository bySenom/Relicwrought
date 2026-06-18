package io.github.bysenom.relicwrought.combat.cooldown;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStatResolver;
import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStats;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import net.minecraft.world.entity.player.Player;

public class WeaponCooldownResolver {
    private final ArpgModConfig config;
    private final java.util.function.Function<Player, CharacterCombatStats> statProvider;

    public WeaponCooldownResolver(ArpgModConfig config, java.util.function.Function<Player, CharacterCombatStats> statProvider) {
        this.config = config;
        this.statProvider = statProvider;
    }

    /**
     * Resolves the final attack speed in Attacks Per Second (APS).
     */
    public double resolveAttackSpeed(Player player, ArpgItemData weaponData) {
        if (weaponData == null) {
            // Unarmed or non-ARPG weapon - default to vanilla generic speed
            return 4.0; 
        }

        double baseAps = weaponData.itemBaseId() != null 
                ? getWeaponBaseAps(weaponData) 
                : 1.0;

        io.github.bysenom.relicwrought.item.format.LocalAffixResolver.LocalAffixModifiers localMods = 
                io.github.bysenom.relicwrought.item.format.LocalAffixResolver.resolve(weaponData);
        
        double localFlat = localMods.flatModifiers().getOrDefault("attack_speed", 0.0);
        double localPercent = localMods.percentModifiers().getOrDefault("attack_speed", 0.0) / 100.0;
        
        double weaponAps = (baseAps + localFlat) * (1.0 + localPercent);

        CharacterCombatStats stats = statProvider.apply(player);
        double globalPercent = stats != null ? stats.attackSpeedPercent() : 0.0;

        double finalAps = weaponAps * (1.0 + globalPercent);

        // Clamp
        if (Double.isNaN(finalAps)) finalAps = 1.0;
        if (finalAps < config.minimumAttackSpeed()) finalAps = config.minimumAttackSpeed();
        if (finalAps > config.maximumAttackSpeed()) finalAps = config.maximumAttackSpeed();

        return finalAps;
    }

    /**
     * Resolves the cooldown duration in ticks.
     */
    public int resolveCooldownTicks(Player player, ArpgItemData weaponData) {
        double aps = resolveAttackSpeed(player, weaponData);
        if (aps <= 0) return config.minimumWeaponCooldownTicks(); // Safety against division by zero

        double ticks = 20.0 / aps;
        int roundedTicks = (int) Math.round(ticks);

        return Math.max(roundedTicks, config.minimumWeaponCooldownTicks());
    }

    private double getWeaponBaseAps(ArpgItemData data) {
        io.github.bysenom.relicwrought.item.model.ItemBaseDefinition itemBase = 
                io.github.bysenom.relicwrought.item.ArpgItemSystems.bootstrapResult().itemBases().get(data.itemBaseId()).orElse(null);
        if (itemBase != null) {
            return itemBase.baseStats().attackSpeed();
        }
        return 1.6; // Fallback
    }
}
