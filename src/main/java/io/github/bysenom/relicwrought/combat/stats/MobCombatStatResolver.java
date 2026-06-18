package io.github.bysenom.relicwrought.combat.stats;

import io.github.bysenom.relicwrought.ArpgModConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class MobCombatStatResolver {

    private MobCombatStatResolver() {
    }

    public static CharacterCombatStats resolve(Mob mob, ArpgModConfig config) {
        double vanillaArmor = 0;
        if (mob.getAttributes().hasAttribute(Attributes.ARMOR)) {
            vanillaArmor = mob.getAttributeValue(Attributes.ARMOR);
        }
        
        double convertedArmor = vanillaArmor * config.vanillaArmorConversion();

        double vanillaMaxHealth = 0;
        if (mob.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
            vanillaMaxHealth = mob.getAttributeValue(Attributes.MAX_HEALTH);
        }

        // Return a mostly empty stat sheet but with armor and health populated
        return new CharacterCombatStats(
                0, 0, 0, 0, 0,
                0, 0,
                0, config.baseCriticalChance(), config.baseCriticalMultiplier(),
                0, 0,
                convertedArmor, vanillaMaxHealth, 0, 0, 0, 0,
                0, 0,
                0, 0, 0
        );
    }
}
