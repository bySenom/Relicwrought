package io.github.bysenom.relicwrought.client.combattext;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.combat.damage.CombatTextEvent;
import io.github.bysenom.relicwrought.ui.CombatTextVisibility;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages floating damage number events on the client.
 * Events are added when a FloatingDamageNumberPayload is received.
 * Each event has a position (looked up from the target entity), a lifetime, and drift.
 */
public class FloatingDamageNumberManager {

    private static final List<ActiveDamageNumber> activeNumbers = new ArrayList<>();
    private static final int MAX_LIFETIME_TICKS = 30; // ~1.5 seconds
    private static final int MAX_ACTIVE = 20;
    private static long lastPayloadClientTick = -1L;

    public static void addEvent(CombatTextEvent event) {
        Minecraft client = Minecraft.getInstance();
        java.util.UUID localPlayerUuid = client.player != null ? client.player.getUUID() : null;
        if (!CombatTextVisibility.isVisible(
                event,
                Relicwrought.config().enableCombatText(),
                Relicwrought.config().showFloatingDamageNumbers(),
                Relicwrought.config().showOwnDamageNumbers(),
                localPlayerUuid
        )) {
            return;
        }
        lastPayloadClientTick = client.level != null ? client.level.getGameTime() : 0L;

        // Look up entity position
        net.minecraft.world.entity.Entity target = client.level != null ? client.level.getEntity(event.targetEntityId()) : null;
        double x, y, z;
        if (target != null) {
            x = target.getX() + (client.level.getRandom().nextFloat() - 0.5) * 0.5;
            y = target.getY() + target.getBbHeight() + 0.3;
            z = target.getZ() + (client.level.getRandom().nextFloat() - 0.5) * 0.5;
        } else {
            x = event.sequenceId();
            y = 0.0;
            z = 0.0;
        }

        // Remove oldest if over limit
        while (activeNumbers.size() >= MAX_ACTIVE) {
            activeNumbers.removeFirst();
        }

        activeNumbers.add(new ActiveDamageNumber(
                event.totalDamage(), event.critical(), event.damageTypeKey(),
                x, y, z, 0
        ));

        if (Relicwrought.config().enableCombatDebugLogging()) {
            Relicwrought.LOGGER.info("[CombatText] Added damage number: {} {} at ({}, {}, {})",
                    event.totalDamage(), event.critical() ? "CRIT" : "", x, y, z);
        }
    }

    public static void tick() {
        Iterator<ActiveDamageNumber> it = activeNumbers.iterator();
        while (it.hasNext()) {
            ActiveDamageNumber n = it.next();
            n.age++;
            n.y += 0.03; // Drift upward
            if (n.age >= MAX_LIFETIME_TICKS) {
                it.remove();
            }
        }
    }

    public static List<ActiveDamageNumber> getActiveNumbers() {
        return activeNumbers;
    }

    public static int getActiveCount() {
        return activeNumbers.size();
    }

    public static boolean hasRecentPayload(long currentGameTime) {
        return lastPayloadClientTick >= 0L && currentGameTime - lastPayloadClientTick <= MAX_LIFETIME_TICKS;
    }

    public static void clear() {
        activeNumbers.clear();
        lastPayloadClientTick = -1L;
    }

    public static class ActiveDamageNumber {
        public final double damage;
        public final boolean critical;
        public final String damageType;
        public double x, y, z;
        public int age;

        public ActiveDamageNumber(double damage, boolean critical, String damageType,
                                  double x, double y, double z, int age) {
            this.damage = damage;
            this.critical = critical;
            this.damageType = damageType;
            this.x = x;
            this.y = y;
            this.z = z;
            this.age = age;
        }

        public float getAlpha() {
            if (age < 10) return 1.0f;
            return Math.max(0, 1.0f - (age - 10) / (float) (MAX_LIFETIME_TICKS - 10));
        }

        public float getScale() {
            if (critical) return 0.035f;
            return 0.025f;
        }

        public int getColor() {
            if (critical) return 0xFFFFDD00; // Yellow for crit
            return switch (damageType) {
                case "fire" -> 0xFFFF6600;
                case "cold" -> 0xFF66CCFF;
                case "lightning" -> 0xFFFFFF44;
                case "poison" -> 0xFF44DD44;
                default -> 0xFFFFFFFF; // White for physical
            };
        }

        public String getText() {
            String text = String.valueOf((int) Math.max(0.0, damage));
            if (critical) text += "!";
            return text;
        }
    }
}
