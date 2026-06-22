package io.github.bysenom.relicwrought.client.screen;

import io.github.bysenom.relicwrought.client.ClientArpgState;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.network.EquipmentSlotClickPayload;
import io.github.bysenom.relicwrought.network.EquipmentSyncRequestPayload;
import io.github.bysenom.relicwrought.ui.RpgEquipmentScreenModel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class RpgEquipmentScreen extends Screen {
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_INSET = 1;
    private static final int PANEL_WIDTH = 456;
    private static final int PANEL_HEIGHT = 270;
    private static final int LEFT_PANEL_WIDTH = 198;
    private static RpgEquipmentScreen openScreen;

    private Component statusMessage = Component.empty();
    private int statusColor = 0xFFE8DFA8;
    private List<SlotBounds> slotBounds = List.of();

    public RpgEquipmentScreen() {
        super(Component.translatable("ui.relicwrought.inventory.title"));
    }

    @Override
    protected void init() {
        super.init();
        openScreen = this;
        rebuildSlotBounds();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(guiGraphics);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        RpgEquipmentScreenModel model = currentModel();
        int panelX = panelX();
        int panelY = panelY();

        drawPanel(guiGraphics, panelX, panelY);
        drawEquipmentSlots(guiGraphics, model, panelX, panelY, mouseX, mouseY);
        drawStats(guiGraphics, model, panelX, panelY);
        drawSelectedItem(guiGraphics, model, panelX, panelY, mouseX, mouseY);
        drawHotbarPreview(guiGraphics, panelX, panelY);

        if (statusMessage != null && !statusMessage.getString().isEmpty()) {
            guiGraphics.centeredText(font, statusMessage, panelX + PANEL_WIDTH / 2, panelY + PANEL_HEIGHT - 16, statusColor);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }
        SlotBounds clicked = findSlot(event.x(), event.y());
        if (clicked == null) {
            return super.mouseClicked(event, doubleClick);
        }
        clickSlot(clicked.slot());
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void refreshFromSync() {
        rebuildSlotBounds();
    }

    @Override
    public void onClose() {
        if (openScreen == this) {
            openScreen = null;
        }
        super.onClose();
    }

    private void drawPanel(GuiGraphicsExtractor guiGraphics, int panelX, int panelY) {
        guiGraphics.fill(panelX - 2, panelY - 2, panelX + PANEL_WIDTH + 2, panelY + PANEL_HEIGHT + 2, 0xEE050608);
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xDD14171C);
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 26, 0xEE20242C);
        guiGraphics.outline(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xFFB08A45);
        guiGraphics.centeredText(font, Component.translatable("ui.relicwrought.inventory.title_full"), panelX + PANEL_WIDTH / 2, panelY + 9, 0xFFFFF1C2);
        guiGraphics.fill(panelX + LEFT_PANEL_WIDTH, panelY + 32, panelX + LEFT_PANEL_WIDTH + 1, panelY + PANEL_HEIGHT - 34, 0xFF4B4E55);
    }

    private void drawEquipmentSlots(
            GuiGraphicsExtractor guiGraphics,
            RpgEquipmentScreenModel model,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        int originX = panelX + 16;
        int originY = panelY + 42;
        guiGraphics.centeredText(font, Component.translatable("ui.relicwrought.inventory.equipment_header"),
                originX + 92, panelY + 32, 0xFFE6D9B2);

        for (RpgEquipmentScreenModel.SlotView slot : model.slots()) {
            int x = originX + slot.x();
            int y = originY + slot.y();
            boolean hovered = contains(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE);
            drawSlot(guiGraphics, slot, x, y, hovered);
            if (hovered) {
                drawSlotTooltip(guiGraphics, slot, mouseX, mouseY);
            }
        }
    }

    private void drawSlot(GuiGraphicsExtractor guiGraphics, RpgEquipmentScreenModel.SlotView slot, int x, int y, boolean hovered) {
        int border = hovered ? 0xFFFFD06A : slot.interactive() ? 0xFF8A7141 : 0xFF575A62;
        int fill = slot.interactive() ? 0xFF24272D : 0xFF1A1C20;
        guiGraphics.fill(x - 1, y - 1, x + SLOT_SIZE + 1, y + SLOT_SIZE + 1, border);
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, fill);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + 2, 0x553A3E47);

        if (slot.occupied()) {
            guiGraphics.item(slot.stack(), x + SLOT_INSET, y + SLOT_INSET);
            guiGraphics.itemDecorations(font, slot.stack(), x + SLOT_INSET, y + SLOT_INSET);
        } else {
            guiGraphics.centeredText(font, slot.placeholder(), x + SLOT_SIZE / 2, y + 6, slot.interactive() ? 0xFF9C9688 : 0xFF686B72);
        }
    }

    private void drawSlotTooltip(GuiGraphicsExtractor guiGraphics, RpgEquipmentScreenModel.SlotView slot, int mouseX, int mouseY) {
        if (slot.occupied()) {
            guiGraphics.setTooltipForNextFrame(font, slot.stack(), mouseX, mouseY);
            return;
        }
        guiGraphics.setComponentTooltipForNextFrame(font, slot.hoverText(), mouseX, mouseY);
    }

    private void drawStats(GuiGraphicsExtractor guiGraphics, RpgEquipmentScreenModel model, int panelX, int panelY) {
        int x = panelX + LEFT_PANEL_WIDTH + 18;
        int y = panelY + 42;
        guiGraphics.text(font, Component.translatable("ui.relicwrought.inventory.stats_header"), x, y - 10, 0xFFFFF1C2);
        int lineY = y + 8;
        for (RpgEquipmentScreenModel.StatLine stat : model.stats()) {
            guiGraphics.text(font, stat.label(), x, lineY, 0xFFB8B3A2);
            guiGraphics.text(font, stat.value(), x + 104, lineY, 0xFFFFFFFF);
            lineY += 11;
        }
    }

    private void drawSelectedItem(
            GuiGraphicsExtractor guiGraphics,
            RpgEquipmentScreenModel model,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        int x = panelX + LEFT_PANEL_WIDTH + 18;
        int y = panelY + 182;
        int width = PANEL_WIDTH - LEFT_PANEL_WIDTH - 34;
        guiGraphics.fill(x - 6, y - 10, x + width, y + 54, 0xAA0D0F13);
        guiGraphics.outline(x - 6, y - 10, width + 6, 64, 0xFF4B4E55);
        guiGraphics.text(font, Component.translatable("ui.relicwrought.inventory.selected_header"), x, y - 4, 0xFFFFF1C2);

        RpgEquipmentScreenModel.SelectedItemView selected = model.selectedItem();
        int slotX = x;
        int slotY = y + 13;
        guiGraphics.fill(slotX - 1, slotY - 1, slotX + SLOT_SIZE + 1, slotY + SLOT_SIZE + 1, 0xFF8A7141);
        guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0xFF24272D);
        if (selected.present()) {
            guiGraphics.item(selected.stack(), slotX + SLOT_INSET, slotY + SLOT_INSET);
            guiGraphics.itemDecorations(font, selected.stack(), slotX + SLOT_INSET, slotY + SLOT_INSET);
            if (contains(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                guiGraphics.setTooltipForNextFrame(font, selected.stack(), mouseX, mouseY);
            }
        }
        guiGraphics.text(font, selected.displayName(), slotX + 30, slotY + 1, 0xFFFFFFFF);
        guiGraphics.text(font, Component.translatable("ui.relicwrought.inventory.equip_instruction"), slotX + 30, slotY + 13, 0xFFB8B3A2);
    }

    private void drawHotbarPreview(GuiGraphicsExtractor guiGraphics, int panelX, int panelY) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        int startX = panelX + 36;
        int y = panelY + PANEL_HEIGHT - 34;
        guiGraphics.text(font, Component.translatable("ui.relicwrought.inventory.hotbar_hint"), startX, y - 13, 0xFFB8B3A2);
        int selectedSlot = client.player.getInventory().getSelectedSlot();
        for (int i = 0; i < 9; i++) {
            int x = startX + i * 22;
            int border = i == selectedSlot ? 0xFFFFD06A : 0xFF575A62;
            guiGraphics.fill(x - 1, y - 1, x + SLOT_SIZE + 1, y + SLOT_SIZE + 1, border);
            guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF24272D);
            ItemStack stack = client.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                guiGraphics.item(stack, x + SLOT_INSET, y + SLOT_INSET);
                guiGraphics.itemDecorations(font, stack, x + SLOT_INSET, y + SLOT_INSET);
            }
        }
    }

    private void clickSlot(ArpgEquipmentSlot slot) {
        Minecraft client = Minecraft.getInstance();
        ItemStack selected = client.player == null ? ItemStack.EMPTY : client.player.getInventory().getSelectedItem();
        RpgEquipmentScreenModel.InteractionPreview preview = currentModel().previewClick(slot, selected);
        if (!preview.allowed()) {
            setStatus(preview.messageKey(), true);
            return;
        }
        setStatus(preview.messageKey(), false);
        ClientPlayNetworking.send(new EquipmentSlotClickPayload(slot));
    }

    private void setStatus(String translationKey, boolean error) {
        statusMessage = Component.translatable(translationKey);
        statusColor = error ? 0xFFFF7777 : 0xFFE8DFA8;
    }

    private RpgEquipmentScreenModel currentModel() {
        Minecraft client = Minecraft.getInstance();
        ItemStack selected = client.player == null ? ItemStack.EMPTY : client.player.getInventory().getSelectedItem();
        CharacterScreenModel characterModel = ClientArpgState.getCharacterScreenModel();
        return RpgEquipmentScreenModel.create(
                ClientArpgState.copyEquipmentSlots(),
                selected,
                characterModel.getLevel(),
                characterModel.getTotalAttributes(),
                characterModel.getCurrentStats()
        );
    }

    private void rebuildSlotBounds() {
        int originX = panelX() + 16;
        int originY = panelY() + 42;
        List<SlotBounds> bounds = new ArrayList<>();
        for (RpgEquipmentScreenModel.SlotView slot : currentModel().slots()) {
            bounds.add(new SlotBounds(
                    slot.slot(),
                    originX + slot.x(),
                    originY + slot.y(),
                    SLOT_SIZE,
                    SLOT_SIZE
            ));
        }
        slotBounds = List.copyOf(bounds);
    }

    private SlotBounds findSlot(double mouseX, double mouseY) {
        for (SlotBounds bounds : slotBounds) {
            if (contains(mouseX, mouseY, bounds.x(), bounds.y(), bounds.width(), bounds.height())) {
                return bounds;
            }
        }
        return null;
    }

    private int panelX() {
        return Math.max(8, width / 2 - PANEL_WIDTH / 2);
    }

    private int panelY() {
        return Math.max(8, height / 2 - PANEL_HEIGHT / 2);
    }

    private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
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

    private record SlotBounds(ArpgEquipmentSlot slot, int x, int y, int width, int height) {
    }
}
