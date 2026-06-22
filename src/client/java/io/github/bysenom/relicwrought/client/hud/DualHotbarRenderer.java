package io.github.bysenom.relicwrought.client.hud;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.client.ClientAbilityState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class DualHotbarRenderer {
    private static final Identifier ABILITY_SLOT_TEXTURE = Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "textures/gui/ability_slot.png");
    
    private static final int[] SLOT_COLORS = {
        0xFFFF4444, 0xFF4488FF, 0xFFFFAA00, 0xFF44FF44,
        0xFFFF88FF, 0xFF88FFFF, 0xFFFFFF44, 0xFFFF8844,
        0xFF44FF88
    };

    public static void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null) return;
        
        HotbarMode currentMode = AbilityHotbarState.getCurrentMode();
        if (currentMode != HotbarMode.ABILITY) return;
        
        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();
        
        int abilityHotbarX = scaledWidth / 2 - 91;
        int abilityHotbarY = scaledHeight - 22;
        
        guiGraphics.fill(abilityHotbarX, abilityHotbarY, abilityHotbarX + 182, abilityHotbarY + 22, 0x88000000);
        
        for (int i = 0; i < 9; i++) {
            int slotX = abilityHotbarX + i * 20 + 2;
            int slotY = abilityHotbarY + 2;
            
            String abilityId = ClientAbilityState.getAbilityId(i);
            boolean hasAbility = abilityId != null;
            boolean onCooldown = hasAbility && ClientAbilityState.isOnCooldown(abilityId);
            
            guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, hasAbility ? SLOT_COLORS[i] : 0xAA222222);
            
            if (hasAbility) {
                String label = getAbilityLabel(abilityId);
                if (!label.isEmpty()) {
                    int textColor = onCooldown ? 0xFF888888 : 0xFFFFFFFF;
                    guiGraphics.centeredText(client.font, label, slotX + 9, slotY + 5, textColor);
                }
            }
            
            if (onCooldown) {
                int cdRemaining = ClientAbilityState.getCooldownRemaining(abilityId);
                float cdSeconds = cdRemaining / 20.0f;
                String cdText = String.format("%.1f", cdSeconds);
                int cdColor = 0xCCFFFFFF;
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0x88000000);
                guiGraphics.centeredText(client.font, cdText, slotX + 9, slotY + 10, cdColor);
            }
            
            String keyLabel = String.valueOf(i + 1);
            int keyColor = currentMode == HotbarMode.ABILITY ? 0xFFFFAA00 : 0xFF888888;
            guiGraphics.centeredText(client.font, keyLabel, slotX + 9, slotY - 1, keyColor);
        }
    }

    private static String getAbilityLabel(String abilityId) {
        if (abilityId == null) return "";
        if (abilityId.contains("power_strike")) return "P";
        if (abilityId.contains("fire_bolt")) return "F";
        if (abilityId.contains("quick_jab")) return "Q";
        if (abilityId.contains("second_wind")) return "S";
        int idx = abilityId.lastIndexOf(':');
        String last = idx >= 0 ? abilityId.substring(idx + 1) : abilityId;
        return last.isEmpty() ? "?" : last.substring(0, 1).toUpperCase();
    }
}
