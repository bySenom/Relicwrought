package io.github.bysenom.relicwrought.client.hud;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.client.ClientArpgState;
import io.github.bysenom.relicwrought.ui.CharacterResourceState;
import io.github.bysenom.relicwrought.ui.CharacterResourceType;
import io.github.bysenom.relicwrought.ui.PlayerHudModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;

/**
 * Renders the Relicwrought HP bar and resource bar in place of vanilla hearts/armor.
 *
 * Fallback: When no ARPG health sync has been received, uses vanilla health values
 * so the bar is always visible when enabled.
 */
public class PlayerHudRenderer {

    private static boolean debugLogged = false;
    private static PlayerHudModel lastModel = PlayerHudModel.hidden(false, false);
    private static boolean renderedThisFrame = false;

    public static void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        renderedThisFrame = false;
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null) return;

        // Log once that the renderer is actually being called
        if (!debugLogged && Relicwrought.config().enableCombatDebugLogging()) {
            Relicwrought.LOGGER.info("[HUD] PlayerHudRenderer active. HP={}/{}, ResourceType={}",
                    ClientArpgState.getCurrentHealth(), ClientArpgState.getMaximumHealth(),
                    ClientArpgState.getResourceState().type());
            debugLogged = true;
        }

        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();

        PlayerHudModel model = PlayerHudModel.resolve(
                Relicwrought.config().enableRelicwroughtHud(),
                Relicwrought.config().hideVanillaHearts(),
                Relicwrought.config().hideVanillaArmor(),
                ClientArpgState.hasHudSync(),
                ClientArpgState.getCurrentHealth(),
                ClientArpgState.getMaximumHealth(),
                player.getHealth(),
                player.getMaxHealth(),
                ClientArpgState.getMaximumHealth(),
                ClientArpgState.getResourceState()
        );
        lastModel = model;
        if (!model.visible()) return;
        renderedThisFrame = true;

        int hpBarWidth = 81;
        int hpBarHeight = 9;
        int hpBarX = scaledWidth / 2 - 91;
        int hpBarY = scaledHeight - 40; // Above experience bar, left side (where hearts were)

        // Border
        guiGraphics.fill(hpBarX - 1, hpBarY - 1, hpBarX + hpBarWidth + 1, hpBarY + hpBarHeight + 1, 0xFF000000);
        // Background
        guiGraphics.fill(hpBarX, hpBarY, hpBarX + hpBarWidth, hpBarY + hpBarHeight, 0xFF331111);
        // Fill — gradient from dark red to bright red based on health
        int filledWidth = (int) (hpBarWidth * model.healthFill());
        if (filledWidth > 0) {
            guiGraphics.fill(hpBarX, hpBarY, hpBarX + filledWidth, hpBarY + hpBarHeight, 0xFFCC1111);
            // Bright highlight on top pixel row
            guiGraphics.fill(hpBarX, hpBarY, hpBarX + filledWidth, hpBarY + 1, 0xFFFF3333);
        }
        // HP text centered on bar
        if (Relicwrought.config().showHealthNumbers()) {
            String hpText = (int) model.currentHealth() + " / " + (int) model.maximumHealth();
            guiGraphics.centeredText(client.font, hpText, hpBarX + hpBarWidth / 2, hpBarY + 1, 0xFFFFFFFF);
        }

        // --- Resource Bar ---
        CharacterResourceState resource = model.resourceState();
        if (model.resourceVisible()) {
            double maxRes = resource.maximumValue();
            double currentRes = resource.currentValue();

            int resBarWidth = 81;
            int resBarHeight = 9;
            int resBarX = scaledWidth / 2 + 10;
            int resBarY = scaledHeight - 40; // Right side (where armor was)

            int fillColor = switch (resource.type()) {
                case MANA -> 0xFF2244DD;
                case RAGE -> 0xFFDD4400;
                case ENERGY -> 0xFFCCBB00;
                default -> 0xFF666666;
            };
            int highlightColor = switch (resource.type()) {
                case MANA -> 0xFF4466FF;
                case RAGE -> 0xFFFF6622;
                case ENERGY -> 0xFFEEDD22;
                default -> 0xFF888888;
            };

            // Border
            guiGraphics.fill(resBarX - 1, resBarY - 1, resBarX + resBarWidth + 1, resBarY + resBarHeight + 1, 0xFF000000);
            // Background
            guiGraphics.fill(resBarX, resBarY, resBarX + resBarWidth, resBarY + resBarHeight, 0xFF222233);
            // Fill
            int resFilled = (int) (resBarWidth * model.resourceFill());
            if (resFilled > 0) {
                guiGraphics.fill(resBarX, resBarY, resBarX + resFilled, resBarY + resBarHeight, fillColor);
                guiGraphics.fill(resBarX, resBarY, resBarX + resFilled, resBarY + 1, highlightColor);
            }
            // Text
            if (Relicwrought.config().showResourceNumbers()) {
                String resText = resource.type().name().substring(0, 3) + " " + (int) currentRes + "/" + (int) maxRes;
                guiGraphics.centeredText(client.font, resText, resBarX + resBarWidth / 2, resBarY + 1, 0xFFFFFFFF);
            }
        }
    }

    public static PlayerHudModel getLastModel() {
        return lastModel;
    }

    public static boolean wasRenderedThisFrame() {
        return renderedThisFrame;
    }
}
