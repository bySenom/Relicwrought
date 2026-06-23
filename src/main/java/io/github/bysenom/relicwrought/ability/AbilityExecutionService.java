package io.github.bysenom.relicwrought.ability;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.combat.damage.DamageBundle;
import io.github.bysenom.relicwrought.combat.damage.DamageType;
import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStats;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.player.PlayerArpgProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public final class AbilityExecutionService {
    private final AbilityRegistry registry;

    public AbilityExecutionService(AbilityRegistry registry) {
        this.registry = registry;
    }

    public AbilityActivationResult activate(ServerPlayer player, int slotIndex, PlayerAbilityLoadout loadout,
                                            PlayerAbilityCooldowns cooldowns, PlayerArpgProfile profile) {
        if (player == null) {
            return AbilityActivationResult.failure("No player");
        }
        if (!player.isAlive()) {
            return AbilityActivationResult.failure("You are dead");
        }
        if (!Relicwrought.config().enableAbilities()) {
            return AbilityActivationResult.failure("Abilities are disabled");
        }

        Optional<String> slotAbilityId = loadout.getAbilityId(slotIndex);
        if (slotAbilityId.isEmpty()) {
            return AbilityActivationResult.failure("Slot " + (slotIndex + 1) + " is empty");
        }

        String abilityIdStr = slotAbilityId.get();
        Optional<AbilityDefinition> optDef = registry.get(DefinitionKey.parse(abilityIdStr, Relicwrought.MOD_ID));
        if (optDef.isEmpty()) {
            return AbilityActivationResult.failure("Ability not found: " + abilityIdStr);
        }
        AbilityDefinition ability = optDef.get();

        if (!ability.allowedClasses().isEmpty()) {
            String shortClassId = profile.classId();
            if (shortClassId.contains(":")) {
                shortClassId = shortClassId.substring(shortClassId.indexOf(':') + 1);
            }
            if (!ability.allowedClasses().contains(shortClassId) && !ability.allowedClasses().contains(profile.classId())) {
                return AbilityActivationResult.failure("Wrong class for " + abilityIdStr);
            }
        }

        if (cooldowns.isOnCooldown(abilityIdStr)) {
            int remaining = cooldowns.getRemainingTicks(abilityIdStr);
            return AbilityActivationResult.failure("Ability on cooldown (" + (remaining / 20.0) + "s)");
        }

        if (Relicwrought.config().enableAbilityResourceCosts()) {
            double currentResource = profile.currentResourceValue();
            if (ability.resourceType() != AbilityResourceType.NONE && ability.resourceCost() > 0) {
                if (currentResource < ability.resourceCost()) {
                    return AbilityActivationResult.failure("Not enough " + ability.resourceType().name().toLowerCase());
                }
            }
        }

        LivingEntity target = resolveTarget(player, ability);
        if (ability.targetingType() == AbilityTargetingType.TARGET_ENTITY && target == null) {
            return AbilityActivationResult.failure("No valid target");
        }

        // TODO: add dedicated attackPower/spellPower stat to CharacterCombatStats
        double effectivePower = ability.basePower();
        if (ability.scaling() > 0) {
            var handler = Relicwrought.getMeleeDamageHandler();
            if (handler != null) {
                CharacterCombatStats attStats = handler.getAttackerStats(player);
                DamageType dt = ability.damageType();
                if (dt == DamageType.PHYSICAL) {
                    effectivePower = ability.basePower() + ability.scaling() * attStats.flatPhysicalDamage();
                } else if (dt == DamageType.FIRE) {
                    effectivePower = ability.basePower() + ability.scaling() * attStats.fireDamage();
                }
                // For other types and heals, scaling is 0 until dedicated stats exist
            }
        }

        AbilityEffectResult effectResult = applyEffect(player, ability, target, effectivePower);

        if (!effectResult.success()) {
            return AbilityActivationResult.failure(effectResult.message());
        }

        PlayerArpgProfile updatedProfile = profile;
        if (Relicwrought.config().enableAbilityResourceCosts()) {
            double resourceCost = Math.max(0, Math.min(ability.resourceCost(), profile.currentResourceValue()));
            updatedProfile = profile.withResourceValue(profile.currentResourceValue() - resourceCost);
        }

        if (Relicwrought.config().enableAbilityCooldowns()) {
            cooldowns.startCooldown(abilityIdStr, ability.cooldownTicks());
        }

        return AbilityActivationResult.success(effectivePower, effectResult.message(), updatedProfile);
    }

    private LivingEntity resolveTarget(ServerPlayer player, AbilityDefinition ability) {
        return switch (ability.targetingType()) {
            case SELF -> player;
            case TARGET_ENTITY, LOOK_DIRECTION -> findEntityInSight(player, ability.range());
            case AREA_AROUND_PLAYER -> null;
            case GROUND_TARGET_LATER -> null;
        };
    }

    private LivingEntity findEntityInSight(ServerPlayer player, double range) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.scale(range));

        EntityHitResult hitResult = findEntityHit(player, eyePos, endPos);
        if (hitResult != null && hitResult.getEntity() instanceof LivingEntity living) {
            if (living != player) return living;
        }
        return null;
    }

    private EntityHitResult findEntityHit(Player player, Vec3 from, Vec3 to) {
        AABB box = new AABB(from, to).inflate(1.0);
        double range = from.distanceTo(to);
        Entity closest = null;
        double closestDist = range + 1.0;

        for (Entity entity : player.level().getEntities(player, box, e -> e instanceof LivingEntity && e.isAlive())) {
            AABB entityBox = entity.getBoundingBox().inflate(0.3);
            Optional<Vec3> hit = entityBox.clip(from, to);
            if (hit.isPresent()) {
                double dist = from.distanceTo(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = entity;
                }
            }
        }
        return closest != null ? new EntityHitResult(closest) : null;
    }

    public List<LivingEntity> resolveAreaTargets(ServerPlayer player, AbilityDefinition ability) {
        if (ability.targetingType() != AbilityTargetingType.AREA_AROUND_PLAYER) return List.of();
        AABB box = new AABB(player.blockPosition()).inflate(ability.radius());
        return player.level().getEntities(player, box, e -> e instanceof LivingEntity && e.isAlive() && e != player)
                .stream().map(e -> (LivingEntity) e).toList();
    }

    private AbilityEffectResult applyEffect(ServerPlayer player, AbilityDefinition ability, LivingEntity target, double power) {
        return switch (ability.effectType()) {
            case DAMAGE -> applyDamage(player, ability, target, power);
            case HEAL -> applyHeal(player, ability, power);
            case BUFF_PLACEHOLDER -> new AbilityEffectResult(false, "Buff not implemented");
            case DASH_PLACEHOLDER -> new AbilityEffectResult(false, "Dash not implemented");
        };
    }

    private AbilityEffectResult applyDamage(ServerPlayer player, AbilityDefinition ability, LivingEntity target, double power) {
        if (target == null) return new AbilityEffectResult(false, "No target");

        DamageType damageType = ability.damageType() != null ? ability.damageType() : DamageType.PHYSICAL;
        DamageBundle damage = DamageBundle.single(damageType, power);
        float finalDamage = (float) damage.getTotalDamage();

        target.hurt(player.damageSources().playerAttack(player), finalDamage);

        return new AbilityEffectResult(true, "Dealt " + String.format("%.1f", finalDamage) + " damage");
    }

    private AbilityEffectResult applyHeal(ServerPlayer player, AbilityDefinition ability, double power) {
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healAmount = Math.min((float) power, maxHealth - currentHealth);
        if (healAmount > 0) {
            player.heal(healAmount);
        }
        return new AbilityEffectResult(true, "Healed for " + String.format("%.1f", healAmount));
    }

    public record AbilityActivationResult(boolean success, String message, PlayerArpgProfile updatedProfile) {
        public static AbilityActivationResult success(double power, String message, PlayerArpgProfile updatedProfile) {
            return new AbilityActivationResult(true, message, updatedProfile);
        }

        public static AbilityActivationResult failure(String message) {
            return new AbilityActivationResult(false, message, null);
        }
    }

    private record AbilityEffectResult(boolean success, String message) {
    }
}
