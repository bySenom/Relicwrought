package io.github.bysenom.relicwrought.client.screen;

import io.github.bysenom.relicwrought.progression.CharacterAttribute;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class CharacterScreen extends Screen {
    private final CharacterScreenModel model;

    public CharacterScreen(CharacterScreenModel model) {
        super(Component.translatable("ui.relicwrought.character.title"));
        this.model = model;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        
        int x = this.width / 2 - 100;
        int y = 20;
        
        // guiGraphics.drawString(this.font, this.title, x, y, 0xFFFFFF);
        y += 20;
        
        // guiGraphics.drawString(this.font, Component.literal("Level: " + model.getLevel()), x, y, 0xAAAAAA);
        y += 15;
        // guiGraphics.drawString(this.font, Component.literal("XP: " + model.getCurrentXp() + " / " + model.getXpForNextLevel()), x, y, 0xAAAAAA);
        y += 25;
        
        // guiGraphics.drawString(this.font, Component.literal("Attributes:"), x, y, 0xFFFFAA);
        y += 15;
        
        for (Map.Entry<CharacterAttribute, Integer> entry : model.getTotalAttributes().entrySet()) {
            // guiGraphics.drawString(this.font, Component.literal(entry.getKey().name() + ": " + entry.getValue()), x + 10, y, 0xFFFFFF);
            y += 15;
        }
        
        y += 10;
        // guiGraphics.drawString(this.font, Component.literal("Combat Stats:"), x, y, 0xFFFFAA);
        y += 15;
        
        var stats = model.getCurrentStats();
        // guiGraphics.drawString(this.font, Component.literal("Max Life: " + stats.maximumLife()), x + 10, y, 0xFFFFFF);
        y += 15;
        // guiGraphics.drawString(this.font, Component.literal("Armor: " + stats.armor()), x + 10, y, 0xFFFFFF);
        y += 15;
        // guiGraphics.drawString(this.font, Component.literal("Crit Chance: " + (stats.criticalStrikeChance() * 100) + "%"), x + 10, y, 0xFFFFFF);
        y += 15;
        // guiGraphics.drawString(this.font, Component.literal("Crit Multi: " + (stats.criticalStrikeMultiplier() * 100) + "%"), x + 10, y, 0xFFFFFF);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
