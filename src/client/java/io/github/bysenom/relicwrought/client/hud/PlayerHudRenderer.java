package io.github.bysenom.relicwrought.client.hud;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.client.ClientArpgState;
import io.github.bysenom.relicwrought.ui.CharacterResourceState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;

public class PlayerHudRenderer {

    public static void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        if (!Relicwrought.config().enableRelicwroughtHud()) return;

        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null) return;

        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();

        // Render HP Bar (Replacing Hearts)
        double maxHp = ClientArpgState.getMaximumHealth();
        double currentHp = ClientArpgState.getCurrentHealth();
        double hpPercent = maxHp > 0 ? Math.max(0, Math.min(1.0, currentHp / maxHp)) : 0;

        int hpBarWidth = 80;
        int hpBarHeight = 8;
        int hpBarX = scaledWidth / 2 - 91;
        int hpBarY = scaledHeight - 39; // Above experience bar, left side

        // Background
        guiGraphics.fill(hpBarX, hpBarY, hpBarX + hpBarWidth, hpBarY + hpBarHeight, 0xFF222222);
        // Fill
        guiGraphics.fill(hpBarX, hpBarY, hpBarX + (int)(hpBarWidth * hpPercent), hpBarY + hpBarHeight, 0xFFCC0000);
        // Text
        String hpText = (int)currentHp + "/" + (int)maxHp;
        // guiGraphics.drawString(client.font, hpText, hpBarX + hpBarWidth / 2 - client.font.width(hpText) / 2, hpBarY, 0xFFFFFF);

        // Render Resource Bar (Replacing Armor)
        CharacterResourceState resource = ClientArpgState.getResourceState();
        if (resource != null && resource.type() != io.github.bysenom.relicwrought.ui.CharacterResourceType.NONE) {
            double maxRes = resource.maximumValue();
            double currentRes = resource.currentValue();
            double resPercent = maxRes > 0 ? Math.max(0, Math.min(1.0, currentRes / maxRes)) : 0;

            int resBarWidth = 80;
            int resBarHeight = 8;
            int resBarX = scaledWidth / 2 + 11;
            int resBarY = scaledHeight - 39; // Right side

            int color = switch (resource.type()) {
                case MANA -> 0xFF0055FF;
                case RAGE -> 0xFFFF5500;
                case ENERGY -> 0xFFDDDD00;
                default -> 0xFF888888;
            };

            // Background
            guiGraphics.fill(resBarX, resBarY, resBarX + resBarWidth, resBarY + resBarHeight, 0xFF222222);
            // Fill
            guiGraphics.fill(resBarX, resBarY, resBarX + (int)(resBarWidth * resPercent), resBarY + resBarHeight, color);
            // Text
            String resText = (int)currentRes + "/" + (int)maxRes;
            // guiGraphics.drawString(client.font, resText, resBarX + resBarWidth / 2 - client.font.width(resText) / 2, resBarY, 0xFFFFFF);
        }
    }
}
