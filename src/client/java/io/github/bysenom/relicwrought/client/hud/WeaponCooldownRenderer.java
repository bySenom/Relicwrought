package io.github.bysenom.relicwrought.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class WeaponCooldownRenderer {

    public static void render(GuiGraphicsExtractor guiGraphics, float tickCounter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ArpgModConfig config = ArpgModConfig.defaults();
        if (!config.enableWeaponCooldownGating()) return;

        WeaponCooldownHudModel model = new WeaponCooldownHudModel(mc, ClientWeaponCooldownState.getState(), config);
        if (!model.visible) return;

        renderCooldownBar(guiGraphics, mc, model, config);
    }

    private static void renderCooldownBar(GuiGraphicsExtractor guiGraphics, Minecraft mc, WeaponCooldownHudModel model, ArpgModConfig config) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int width = config.weaponCooldownWidth();
        int height = config.weaponCooldownHeight();
        
        // Default position: Hotbar center
        int x = (screenWidth - width) / 2 + config.weaponCooldownOffsetX();
        int y = screenHeight - 22 - height + config.weaponCooldownOffsetY(); // Above hotbar

        // Background
        guiGraphics.fill(x, y, x + width, y + height, 0x88000000);

        // Progress bar
        int progressWidth = (int) (width * model.progress);
        int color = model.ready ? 0xFF00FF00 : 0xFFFFAA00; // Green when ready, orange when charging
        
        if (model.progress > 0) {
            guiGraphics.fill(x, y, x + progressWidth, y + height, color);
        }
        
        // Ready flash
        if (model.ready && config.weaponCooldownShowReadyFlash()) {
            // A simple flash effect based on game time could be added here
            // But for now, the solid green is clear enough
        }
        
        // Percentage text
        if (config.weaponCooldownShowPercentage()) {
            String text = (int)(model.progress * 100) + "%";
            guiGraphics.centeredText(mc.font, text, x + width / 2, y - 10, 0xFFFFFFFF);
        }
    }
}
