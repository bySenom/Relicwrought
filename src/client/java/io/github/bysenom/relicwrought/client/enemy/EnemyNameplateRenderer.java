package io.github.bysenom.relicwrought.client.enemy;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.ui.EnemyClassification;
import io.github.bysenom.relicwrought.ui.EnemyNameplateModel;
import io.github.bysenom.relicwrought.ui.EnemyUiSnapshot;
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
    private static EnemyNameplateModel lastModel = EnemyNameplateModel.hidden();
    private static boolean renderedThisFrame = false;

    /**
     * Called from HudMixin during HUD extraction phase.
     */
    public static void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        renderedThisFrame = false;
        if (!Relicwrought.config().enableEnemyNameplates()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        EnemyUiSnapshot snapshot = resolveSnapshot(client);
        EnemyNameplateModel model = EnemyNameplateModel.fromSnapshot(
                snapshot,
                Relicwrought.config().enableEnemyNameplates(),
                Relicwrought.config().showEnemyHealthBars(),
                Relicwrought.config().showEnemyHealthNumbers()
        );
        lastModel = model;
        if (!model.visible()) return;
        renderedThisFrame = true;

        int scaledWidth = client.getWindow().getGuiScaledWidth();

        // Render at top center of screen
        int plateWidth = 132;
        int plateHeight = model.healthNumbersVisible() ? 28 : 20;
        int plateX = (scaledWidth - plateWidth) / 2;
        int plateY = 4;

        // Background
        guiGraphics.fill(plateX - 1, plateY - 1, plateX + plateWidth + 1, plateY + plateHeight + 1, 0xCC000000);
        guiGraphics.fill(plateX, plateY, plateX + plateWidth, plateY + plateHeight, 0xAA111111);

        // Name text (centered)
        guiGraphics.centeredText(client.font, model.title(), plateX + plateWidth / 2, plateY + 2, 0xFFFFFFFF);

        if (model.healthBarVisible()) {
            int barX = plateX + 4;
            int barY = plateY + 12;
            int barWidth = plateWidth - 8;
            int barHeight = 5;

            // Bar background
            guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
            // Bar fill
            int filledWidth = (int) (barWidth * model.healthFill());
            if (filledWidth > 0) {
                int barColor = model.healthFill() > 0.5f ? 0xFF22CC22 : model.healthFill() > 0.25f ? 0xFFCCAA00 : 0xFFCC2222;
                guiGraphics.fill(barX, barY, barX + filledWidth, barY + barHeight, barColor);
                guiGraphics.fill(barX, barY, barX + filledWidth, barY + 1, brighten(barColor));
            }

            if (model.healthNumbersVisible()) {
                guiGraphics.centeredText(client.font, model.healthText(), plateX + plateWidth / 2, barY + barHeight + 1, 0xFFAAAAAA);
            }
        }
    }

    public static EnemyNameplateModel getLastModel() {
        return lastModel;
    }

    public static boolean wasRenderedThisFrame() {
        return renderedThisFrame;
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

    private static EnemyUiSnapshot resolveSnapshot(Minecraft client) {
        long gameTime = client.level.getGameTime();
        if (EnemyUiTracker.hasValidTarget(gameTime)) {
            return EnemyUiTracker.getCurrentTarget();
        }
        LivingEntity target = findLookedAtEntity(client);
        if (target == null) {
            return null;
        }
        EnemyClassification classification = target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                || target instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                ? EnemyClassification.BOSS
                : EnemyClassification.NORMAL;
        return new EnemyUiSnapshot(
                target.getId(),
                target.getUUID(),
                target.getName().getString(),
                classification,
                0,
                target.getHealth(),
                target.getMaxHealth(),
                true,
                classification == EnemyClassification.BOSS,
                0
        );
    }

    private static int brighten(int color) {
        int r = Math.min(255, ((color >> 16) & 0xFF) + 35);
        int g = Math.min(255, ((color >> 8) & 0xFF) + 35);
        int b = Math.min(255, (color & 0xFF) + 35);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
