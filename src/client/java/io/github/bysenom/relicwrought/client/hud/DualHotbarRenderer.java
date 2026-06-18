package io.github.bysenom.relicwrought.client.hud;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.systems.RenderSystem;

public class DualHotbarRenderer {
    private static final Identifier ABILITY_SLOT_TEXTURE = Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "textures/gui/ability_slot.png");
    private static final Identifier ABILITY_SLOT_ACTIVE_TEXTURE = Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "textures/gui/ability_slot_active.png");
    
    public static void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null) return;
        
        HotbarMode currentMode = AbilityHotbarState.getCurrentMode();
        
        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();
        
        int itemHotbarX = scaledWidth / 2 - 91;
        int itemHotbarY = scaledHeight - 22;
        
        // Render Ability Hotbar above the item hotbar
        int abilityHotbarX = scaledWidth / 2 - 91;
        int abilityHotbarY = scaledHeight - 44; // Above the item hotbar
        
        // Dim the inactive hotbar
        if (currentMode == HotbarMode.ABILITY) {
            // Darken item hotbar area
            guiGraphics.fill(itemHotbarX, itemHotbarY, itemHotbarX + 182, itemHotbarY + 22, 0x88000000);
        } else {
            // Darken ability hotbar area
            guiGraphics.fill(abilityHotbarX, abilityHotbarY, abilityHotbarX + 182, abilityHotbarY + 22, 0x88000000);
        }
        
        // Render Ability Slots
        for (int i = 0; i < 9; i++) {
            int slotX = abilityHotbarX + i * 20 + 2;
            int slotY = abilityHotbarY + 2;
            
            // Draw slot background (using custom texture or a colored box)
            // For now, draw a semi-transparent box
            guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xAA222222);
            
            // Draw active outline if selected (Temporarily disabled due to access issues)
            // if (currentMode == HotbarMode.ABILITY && player.getInventory().selected == i) {
            //     guiGraphics.renderOutline(slotX - 1, slotY - 1, 20, 20, 0xFFFFAA00);
            // }
        }
    }
}
