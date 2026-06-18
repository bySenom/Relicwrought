package io.github.bysenom.relicwrought.client.combattext;

import io.github.bysenom.relicwrought.combat.damage.CombatTextEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class FloatingDamageNumberManager {
    private static final List<CombatTextEvent> events = new ArrayList<>();

    public static void addEvent(CombatTextEvent event) {
        events.add(event);
        
        // Fallback: print to chat if 3D rendering is not yet implemented
        if (Minecraft.getInstance().player != null) {
            String text = String.format("Damage: %.1f%s", event.totalDamage(), event.critical() ? " (CRIT)" : "");
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(text));
        }
    }
    
    public static void tick() {
        // Here we would remove old events
    }
    
    // public static void render(com.mojang.blaze3d.vertex.PoseStack poseStack, ...) { ... }
}
