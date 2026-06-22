package io.github.bysenom.relicwrought.client.enemy;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side HUD overlay renderer for enemy nameplates.
 * Renders the nameplate of the entity the player is looking at (crosshair target)
 * as a screen-space overlay near the top of the screen.
 * 
 * This is a documented client-side fallback using vanilla health values.
 * The server remains authoritative for gameplay data.
 */
public class EnemyNameplateRenderer {

    private static final double MAX_RANGE = 24.0;

    /**
     * Called from HudMixin during HUD extraction phase.
     */
    public static void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        if (!Relicwrought.config().enableEnemyNameplates()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        // Find the entity the player is looking at
        LivingEntity target = findLookedAtEntity(client);
        if (target == null) return;

        float currentHealth = target.getHealth();
        float maxHealth = target.getMaxHealth();
        if (maxHealth <= 0) return;
        float healthPercent = Math.max(0, Math.min(1.0f, currentHealth / maxHealth));

        String name = target.getName().getString();
        String hpText = (int) currentHealth + " / " + (int) maxHealth;

        int scaledWidth = client.getWindow().getGuiScaledWidth();

        // Render at top center of screen
        int plateWidth = 120;
        int plateHeight = 20;
        int plateX = (scaledWidth - plateWidth) / 2;
        int plateY = 4;

        // Background
        guiGraphics.fill(plateX - 1, plateY - 1, plateX + plateWidth + 1, plateY + plateHeight + 1, 0xCC000000);
        guiGraphics.fill(plateX, plateY, plateX + plateWidth, plateY + plateHeight, 0xAA111111);

        // Name text (centered)
        guiGraphics.centeredText(client.font, name, plateX + plateWidth / 2, plateY + 2, 0xFFFFFFFF);

        // HP bar
        int barX = plateX + 4;
        int barY = plateY + 12;
        int barWidth = plateWidth - 8;
        int barHeight = 5;

        // Bar background
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        // Bar fill
        int filledWidth = (int) (barWidth * healthPercent);
        if (filledWidth > 0) {
            int barColor = healthPercent > 0.5f ? 0xFF22CC22 : healthPercent > 0.25f ? 0xFFCCAA00 : 0xFFCC2222;
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + barHeight, barColor);
            // Highlight
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + 1, (barColor & 0x00FFFFFF) | 0xFF000000 | 0x00333333);
        }

        // HP text (right-aligned below bar)
        // Use a smaller approach: render directly on the bar
        guiGraphics.centeredText(client.font, hpText, plateX + plateWidth / 2, barY + barHeight + 1, 0xFFAAAAAA);
    }

    private static LivingEntity findLookedAtEntity(Minecraft client) {
        // Use the crosshair target
        if (client.crosshairPickEntity instanceof LivingEntity living) {
            if (living instanceof Player) return null;
            if (living instanceof ArmorStand) return null;
            if (!living.isAlive()) return null;
            double dist = client.player.distanceTo(living);
            if (dist > MAX_RANGE) return null;
            return living;
        }
        return null;
    }
}
