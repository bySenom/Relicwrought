package io.github.bysenom.relicwrought.client.screen;

import io.github.bysenom.relicwrought.client.ClientArpgState;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.network.EquipmentSlotClickPayload;
import io.github.bysenom.relicwrought.network.EquipmentSyncRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class RpgEquipmentScreen extends Screen {
    private static RpgEquipmentScreen openScreen;
    private Component statusMessage = Component.empty();

    public RpgEquipmentScreen() {
        super(Component.translatable("ui.relicwrought.inventory.title"));
    }

    @Override
    protected void init() {
        super.init();
        openScreen = this;
        int buttonWidth = 156;
        int buttonHeight = 20;
        int gap = 8;
        int panelWidth = buttonWidth * 2 + gap;
        int startX = width / 2 - panelWidth / 2;
        int startY = 54;

        int index = 0;
        for (ArpgEquipmentSlot slot : ArpgEquipmentSlot.displayOrder()) {
            int column = index % 2;
            int row = index / 2;
            Button button = Button.builder(buttonLabel(slot), ignored -> clickSlot(slot))
                    .bounds(startX + column * (buttonWidth + gap), startY + row * 24, buttonWidth, buttonHeight)
                    .build();
            button.active = slot.isExtraSlot();
            addRenderableWidget(button);
            index++;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(guiGraphics);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.fill(width / 2 - 188, 20, width / 2 + 188, 242, 0xCC111111);
        guiGraphics.centeredText(font, title, width / 2, 28, 0xFFFFFFFF);

        Minecraft client = Minecraft.getInstance();
        ItemStack selected = client.player == null ? ItemStack.EMPTY : client.player.getInventory().getSelectedItem();
        Component selectedText = selected.isEmpty()
                ? Component.translatable("ui.relicwrought.inventory.selected_empty")
                : Component.translatable("ui.relicwrought.inventory.selected_item", selected.getHoverName());
        guiGraphics.centeredText(font, selectedText, width / 2, 42, 0xFFAAAAAA);

        if (statusMessage != null && !statusMessage.getString().isEmpty()) {
            guiGraphics.centeredText(font, statusMessage, width / 2, 226, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void refreshFromSync() {
        rebuildWidgets();
    }

    @Override
    public void onClose() {
        if (openScreen == this) {
            openScreen = null;
        }
        super.onClose();
    }

    private void clickSlot(ArpgEquipmentSlot slot) {
        if (!slot.isExtraSlot()) {
            statusMessage = Component.translatable("ui.relicwrought.inventory.vanilla_slot_read_only");
            return;
        }
        statusMessage = Component.translatable("ui.relicwrought.inventory.request_sent");
        ClientPlayNetworking.send(new EquipmentSlotClickPayload(slot));
    }

    private Component buttonLabel(ArpgEquipmentSlot slot) {
        ItemStack stack = ClientArpgState.getEquipmentStack(slot);
        Component slotName = Component.translatable(slot.translationKey());
        if (stack.isEmpty()) {
            return Component.translatable("ui.relicwrought.inventory.slot_empty", slotName);
        }
        return Component.translatable("ui.relicwrought.inventory.slot_filled", slotName, stack.getHoverName());
    }

    public static void open() {
        ClientPlayNetworking.send(new EquipmentSyncRequestPayload());
        Minecraft.getInstance().setScreenAndShow(new RpgEquipmentScreen());
    }

    public static void refreshOpenScreen() {
        if (openScreen != null) {
            openScreen.refreshFromSync();
        }
    }
}
