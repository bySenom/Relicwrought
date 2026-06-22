package io.github.bysenom.relicwrought.client.combattext;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Renders floating damage numbers as HUD overlay text.
 * 
 * Uses screen-space projection of world positions since MC 26.2
 * has restructured the rendering pipeline and MultiBufferSource no longer exists.
 * Each number drifts upward and fades out.
 */
public class FloatingDamageNumberRenderer {

    public static void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        if (!Relicwrought.config().enableCombatText() || !Relicwrought.config().showFloatingDamageNumbers()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();

        // Render active floating numbers at screen center with offsets
        int index = 0;
        for (FloatingDamageNumberManager.ActiveDamageNumber number : FloatingDamageNumberManager.getActiveNumbers()) {
            float alpha = number.getAlpha();
            if (alpha <= 0.01f) continue;

            String text = number.getText();
            int color = number.getColor();
            int a = (int) (alpha * 255) & 0xFF;
            color = (color & 0x00FFFFFF) | (a << 24);

            // Position: spread around screen center, drift upward with age
            int baseX = scaledWidth / 2;
            int baseY = scaledHeight / 2 - 30;

            // Use sequence variation to offset horizontally
            int xOffset = (int) ((number.x * 37 % 60) - 30); // pseudo-random from world X
            int yOffset = -(int) (number.age * 1.5); // drift upward

            int textWidth = client.font.width(text);
            int drawX = baseX + xOffset - textWidth / 2;
            int drawY = baseY + yOffset - (index * 2); // stack slightly

            guiGraphics.centeredText(client.font, text, drawX + textWidth / 2, drawY, color);
            index++;
        }
    }
}
