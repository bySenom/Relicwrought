package io.github.bysenom.relicwrought.client.hud;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.client.ClientArpgState;
import io.github.bysenom.relicwrought.client.combattext.FloatingDamageNumberManager;
import io.github.bysenom.relicwrought.client.enemy.EnemyNameplateRenderer;
import io.github.bysenom.relicwrought.client.enemy.EnemyUiTracker;
import io.github.bysenom.relicwrought.ui.CharacterResourceState;
import io.github.bysenom.relicwrought.ui.PlayerHudModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

public final class UiDebugOverlayRenderer {
    private UiDebugOverlayRenderer() {
    }

    public static void render(GuiGraphicsExtractor guiGraphics) {
        ArpgModConfig config = Relicwrought.config();
        if (!config.enableUiDebugOverlay()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }

        PlayerHudModel hudModel = PlayerHudRenderer.getLastModel();
        CharacterResourceState resource = hudModel.resourceState();
        long gameTime = client.level.getGameTime();
        List<String> lines = new ArrayList<>();
        lines.add("RW HUD: " + onOff(config.enableRelicwroughtHud()));
        lines.add("Health renderer: " + onOff(PlayerHudRenderer.wasRenderedThisFrame()));
        lines.add("Health values: " + (int) hudModel.currentHealth() + " / " + (int) hudModel.maximumHealth());
        lines.add("Vanilla hearts hidden: " + hudModel.vanillaHeartsHidden());
        lines.add("Vanilla armor hidden: " + hudModel.vanillaArmorHidden());
        lines.add("Resource renderer: " + onOff(hudModel.resourceVisible()));
        lines.add("Resource: " + resource.type() + " " + (int) resource.currentValue() + " / " + (int) resource.maximumValue());
        lines.add("Enemy snapshots: " + EnemyUiTracker.getSnapshotCount(gameTime));
        lines.add("Enemy renderer: " + onOff(EnemyNameplateRenderer.wasRenderedThisFrame()));
        lines.add("Damage numbers active: " + FloatingDamageNumberManager.getActiveCount());
        lines.add("Last damage payload: " + yesNo(FloatingDamageNumberManager.hasRecentPayload(gameTime)));
        lines.add("Hotbar mode: " + AbilityHotbarState.getCurrentMode());

        int x = 4;
        int y = 4;
        int lineHeight = 10;
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, client.font.width(line));
        }
        guiGraphics.fill(x - 2, y - 2, x + width + 4, y + lines.size() * lineHeight + 1, 0xAA000000);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineWidth = client.font.width(line);
            guiGraphics.centeredText(client.font, line, x + lineWidth / 2, y + i * lineHeight, 0xFFFFFFFF);
        }
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }
}
