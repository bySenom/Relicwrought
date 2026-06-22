package io.github.bysenom.relicwrought.client.screen;

import io.github.bysenom.relicwrought.item.ArpgItemSystems;
import io.github.bysenom.relicwrought.client.ClientArpgState;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import io.github.bysenom.relicwrought.network.EquipmentScreenClickPayload;
import io.github.bysenom.relicwrought.network.EquipmentSyncRequestPayload;
import io.github.bysenom.relicwrought.ui.RpgEquipmentScreenModel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class RpgEquipmentScreen extends Screen {
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_STEP = 20;
    private static final int SLOT_INSET = 1;
    private static final int PANEL_WIDTH = 610;
    private static final int PANEL_HEIGHT = 430;
    private static final int EQUIPMENT_ORIGIN_X = 16;
    private static final int EQUIPMENT_ORIGIN_Y = 54;
    private static final int INVENTORY_ORIGIN_Y = 314;
    private static final ArpgItemStackService CLIENT_ITEM_SERVICE = new ArpgItemStackService(List.of());

    private static RpgEquipmentScreen openScreen;

    private Component statusMessage = Component.empty();
    private int statusColor = 0xFFE8DFA8;
    private ClickSource selectedSource = ClickSource.none();
    private int sequence = 0;
    private List<EquipmentBounds> equipmentBounds = List.of();
    private List<InventoryBounds> inventoryBounds = List.of();

    public RpgEquipmentScreen() {
        super(Component.translatable("ui.relicwrought.inventory.title"));
    }

    @Override
    protected void init() {
        super.init();
        openScreen = this;
        rebuildBounds();
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
        drawPlayerModel(guiGraphics, model, panelX, panelY, mouseX, mouseY);
        drawStats(guiGraphics, model, panelX, panelY);
        drawSelectedItem(guiGraphics, model, panelX, panelY, mouseX, mouseY);
        drawInventory(guiGraphics, model, panelX, panelY, mouseX, mouseY);

        if (statusMessage != null && !statusMessage.getString().isEmpty()) {
            guiGraphics.centeredText(font, statusMessage, panelX + PANEL_WIDTH / 2, panelY + PANEL_HEIGHT - 14, statusColor);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }
        EquipmentBounds equipment = findEquipmentSlot(event.x(), event.y());
        if (equipment != null) {
            clickEquipment(equipment.slot(), event.button());
            return true;
        }
        InventoryBounds inventory = findInventorySlot(event.x(), event.y());
        if (inventory != null) {
            clickInventory(inventory.inventoryIndex(), event.button());
            return true;
        }
        selectedSource = ClickSource.none();
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void refreshFromSync() {
        rebuildBounds();
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
        guiGraphics.fill(panelX + 172, panelY + 34, panelX + 173, panelY + 238, 0xFF4B4E55);
        guiGraphics.fill(panelX + 328, panelY + 34, panelX + 329, panelY + 238, 0xFF4B4E55);
        guiGraphics.fill(panelX + 12, panelY + 246, panelX + PANEL_WIDTH - 12, panelY + 247, 0xFF4B4E55);
        guiGraphics.fill(panelX + 12, panelY + 304, panelX + PANEL_WIDTH - 12, panelY + 305, 0xFF4B4E55);
    }

    private void drawEquipmentSlots(
            GuiGraphicsExtractor guiGraphics,
            RpgEquipmentScreenModel model,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        int originX = panelX + EQUIPMENT_ORIGIN_X;
        int originY = panelY + EQUIPMENT_ORIGIN_Y;
        guiGraphics.centeredText(font, Component.translatable("ui.relicwrought.inventory.equipment_header"),
                originX + 76, panelY + 36, 0xFFE6D9B2);

        for (RpgEquipmentScreenModel.SlotView slot : model.slots()) {
            int x = originX + slot.x();
            int y = originY + slot.y();
            boolean hovered = contains(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE);
            boolean selected = selectedSource.type() == ClickSource.Type.EQUIPMENT && selectedSource.slot() == slot.slot().ordinal();
            drawEquipmentSlot(guiGraphics, slot, x, y, hovered, selected);
            if (hovered) {
                drawSlotTooltip(guiGraphics, slot, mouseX, mouseY);
            }
        }
    }

    private void drawEquipmentSlot(
            GuiGraphicsExtractor guiGraphics,
            RpgEquipmentScreenModel.SlotView slot,
            int x,
            int y,
            boolean hovered,
            boolean selected
    ) {
        int border = switch (slot.highlight()) {
            case VALID -> 0xFF71D57A;
            case INVALID -> 0xFF803B3B;
            case NEUTRAL -> slot.slot().isExtraSlot() ? 0xFF8A7141 : 0xFF575A62;
        };
        if (selected) {
            border = 0xFF8BD7FF;
        } else if (hovered) {
            border = 0xFFFFD06A;
        }
        int fill = slot.highlight() == RpgEquipmentScreenModel.HighlightState.INVALID ? 0xFF251B1F : 0xFF24272D;
        guiGraphics.fill(x - 1, y - 1, x + SLOT_SIZE + 1, y + SLOT_SIZE + 1, border);
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, fill);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + 2, 0x553A3E47);

        if (slot.occupied()) {
            guiGraphics.item(slot.stack(), x + SLOT_INSET, y + SLOT_INSET);
            guiGraphics.itemDecorations(font, slot.stack(), x + SLOT_INSET, y + SLOT_INSET);
        } else {
            int textColor = slot.highlight() == RpgEquipmentScreenModel.HighlightState.INVALID ? 0xFF8D7373 : 0xFF9C9688;
            guiGraphics.centeredText(font, slot.placeholder(), x + SLOT_SIZE / 2, y + 6, textColor);
        }
    }

    private void drawSlotTooltip(GuiGraphicsExtractor guiGraphics, RpgEquipmentScreenModel.SlotView slot, int mouseX, int mouseY) {
        if (slot.occupied()) {
            guiGraphics.setTooltipForNextFrame(font, slot.stack(), mouseX, mouseY);
            return;
        }
        List<Component> tooltip = new ArrayList<>(slot.hoverText());
        if (slot.highlight() == RpgEquipmentScreenModel.HighlightState.VALID) {
            tooltip.add(Component.translatable("ui.relicwrought.inventory.valid_slot_hint"));
        } else if (slot.highlight() == RpgEquipmentScreenModel.HighlightState.INVALID) {
            tooltip.add(Component.translatable("ui.relicwrought.inventory.invalid_slot_hint"));
        }
        guiGraphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
    }

    private void drawPlayerModel(GuiGraphicsExtractor guiGraphics, RpgEquipmentScreenModel model, int panelX, int panelY, int mouseX, int mouseY) {
        RpgEquipmentScreenModel.PlayerModelArea area = model.playerModelArea();
        int x = panelX + area.x();
        int y = panelY + area.y();
        guiGraphics.centeredText(font, Component.translatable("ui.relicwrought.inventory.model_header"),
                x + area.width() / 2, panelY + 36, 0xFFE6D9B2);
        guiGraphics.fill(x, y, x + area.width(), y + area.height(), 0x660D0F13);
        guiGraphics.outline(x, y, area.width(), area.height(), 0xFF4B4E55);
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(
                    guiGraphics,
                    x + 12,
                    y + 16,
                    x + area.width() - 12,
                    y + area.height() - 10,
                    54,
                    0.0F,
                    mouseX,
                    mouseY,
                    client.player
            );
        }
    }

    private void drawStats(GuiGraphicsExtractor guiGraphics, RpgEquipmentScreenModel model, int panelX, int panelY) {
        int x = panelX + 344;
        int y = panelY + 44;
        guiGraphics.text(font, Component.translatable("ui.relicwrought.inventory.stats_header"), x, panelY + 36, 0xFFFFF1C2);
        int lineY = y + 8;
        for (RpgEquipmentScreenModel.StatGroup group : model.statGroups()) {
            guiGraphics.text(font, group.title(), x, lineY, 0xFFE6D9B2);
            lineY += 10;
            for (RpgEquipmentScreenModel.StatLine stat : group.lines()) {
                guiGraphics.text(font, stat.label(), x + 8, lineY, 0xFFB8B3A2);
                guiGraphics.text(font, stat.value(), x + 120, lineY, 0xFFFFFFFF);
                lineY += 9;
            }
            lineY += 3;
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
        int x = panelX + 18;
        int y = panelY + 258;
        int width = PANEL_WIDTH - 36;
        guiGraphics.fill(x - 6, y - 8, x + width, y + 40, 0xAA0D0F13);
        guiGraphics.outline(x - 6, y - 8, width + 6, 48, 0xFF4B4E55);
        guiGraphics.text(font, Component.translatable("ui.relicwrought.inventory.selected_header"), x, y - 2, 0xFFFFF1C2);

        RpgEquipmentScreenModel.SelectedItemView selected = model.selectedItem();
        int slotX = x + 122;
        int slotY = y - 2;
        drawPlainSlot(guiGraphics, slotX, slotY, selected.present() ? 0xFF8BD7FF : 0xFF575A62);
        if (selected.present()) {
            guiGraphics.item(selected.stack(), slotX + SLOT_INSET, slotY + SLOT_INSET);
            guiGraphics.itemDecorations(font, selected.stack(), slotX + SLOT_INSET, slotY + SLOT_INSET);
            if (contains(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                guiGraphics.setTooltipForNextFrame(font, selected.stack(), mouseX, mouseY);
            }
        }
        guiGraphics.text(font, selected.displayName(), slotX + 28, slotY, 0xFFFFFFFF);
        Component instruction = selected.present()
                ? Component.translatable("ui.relicwrought.inventory.selected_drag_instruction")
                : Component.translatable("ui.relicwrought.inventory.selected_none_instruction");
        guiGraphics.text(font, instruction, slotX + 28, slotY + 11, 0xFFB8B3A2);
        if (selected.present() && !selected.allowedSlots().isEmpty()) {
            guiGraphics.text(font, allowedSlotsText(selected.allowedSlots()), slotX + 250, slotY + 11, 0xFFB8B3A2);
        }
    }

    private void drawInventory(
            GuiGraphicsExtractor guiGraphics,
            RpgEquipmentScreenModel model,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        int originX = panelX + (PANEL_WIDTH - 9 * SLOT_STEP) / 2;
        int originY = panelY + INVENTORY_ORIGIN_Y;
        guiGraphics.text(font, Component.translatable("ui.relicwrought.inventory.inventory_header"), originX, originY - 12, 0xFFE6D9B2);
        for (RpgEquipmentScreenModel.InventorySlotView slot : model.inventorySlots()) {
            drawInventorySlot(guiGraphics, slot, originX, originY, mouseX, mouseY);
        }
        for (RpgEquipmentScreenModel.InventorySlotView slot : model.hotbarSlots()) {
            drawInventorySlot(guiGraphics, slot, originX, originY, mouseX, mouseY);
        }
    }

    private void drawInventorySlot(
            GuiGraphicsExtractor guiGraphics,
            RpgEquipmentScreenModel.InventorySlotView slot,
            int originX,
            int originY,
            int mouseX,
            int mouseY
    ) {
        int x = originX + slot.x();
        int y = originY + slot.y();
        boolean selected = selectedSource.type() == ClickSource.Type.INVENTORY && selectedSource.slot() == slot.inventoryIndex();
        boolean hovered = contains(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE);
        int border = selected ? 0xFF8BD7FF : hovered ? 0xFFFFD06A : slot.hotbar() ? 0xFF6D7280 : 0xFF575A62;
        drawPlainSlot(guiGraphics, x, y, border);
        if (slot.occupied()) {
            guiGraphics.item(slot.stack(), x + SLOT_INSET, y + SLOT_INSET);
            guiGraphics.itemDecorations(font, slot.stack(), x + SLOT_INSET, y + SLOT_INSET);
            if (hovered) {
                guiGraphics.setTooltipForNextFrame(font, slot.stack(), mouseX, mouseY);
            }
        }
    }

    private void drawPlainSlot(GuiGraphicsExtractor guiGraphics, int x, int y, int border) {
        guiGraphics.fill(x - 1, y - 1, x + SLOT_SIZE + 1, y + SLOT_SIZE + 1, border);
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF24272D);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + 2, 0x553A3E47);
    }

    private void clickInventory(int inventoryIndex, int mouseButton) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        if (selectedSource.type() == ClickSource.Type.EQUIPMENT) {
            sendClick(
                    EquipmentScreenClickPayload.SourceType.EQUIPMENT,
                    selectedSource.slot(),
                    EquipmentScreenClickPayload.SourceType.INVENTORY,
                    inventoryIndex,
                    mouseButton,
                    EquipmentScreenClickPayload.ActionType.MOVE
            );
            selectedSource = ClickSource.none();
            return;
        }

        ItemStack stack = client.player.getInventory().getItem(inventoryIndex);
        if (stack.isEmpty()) {
            selectedSource = ClickSource.none();
            setStatus("ui.relicwrought.inventory.no_selected_item", true);
            return;
        }
        selectedSource = ClickSource.inventory(inventoryIndex);
        setStatus("ui.relicwrought.inventory.source_selected", false);
    }

    private void clickEquipment(ArpgEquipmentSlot slot, int mouseButton) {
        if (selectedSource.type() == ClickSource.Type.INVENTORY) {
            sendClick(
                    EquipmentScreenClickPayload.SourceType.INVENTORY,
                    selectedSource.slot(),
                    EquipmentScreenClickPayload.SourceType.EQUIPMENT,
                    slot.ordinal(),
                    mouseButton,
                    EquipmentScreenClickPayload.ActionType.MOVE
            );
            selectedSource = ClickSource.none();
            return;
        }
        if (selectedSource.type() == ClickSource.Type.EQUIPMENT) {
            sendClick(
                    EquipmentScreenClickPayload.SourceType.EQUIPMENT,
                    selectedSource.slot(),
                    EquipmentScreenClickPayload.SourceType.EQUIPMENT,
                    slot.ordinal(),
                    mouseButton,
                    EquipmentScreenClickPayload.ActionType.SWAP
            );
            selectedSource = ClickSource.none();
            return;
        }
        ItemStack stack = ClientArpgState.getEquipmentStack(slot);
        if (stack.isEmpty()) {
            setStatus("ui.relicwrought.inventory.no_selected_item", true);
            return;
        }
        selectedSource = ClickSource.equipment(slot.ordinal());
        setStatus("ui.relicwrought.inventory.source_selected", false);
    }

    private void sendClick(
            EquipmentScreenClickPayload.SourceType sourceType,
            int sourceSlot,
            EquipmentScreenClickPayload.SourceType targetType,
            int targetSlot,
            int mouseButton,
            EquipmentScreenClickPayload.ActionType actionType
    ) {
        setStatus("ui.relicwrought.inventory.request_sent", false);
        ClientPlayNetworking.send(new EquipmentScreenClickPayload(
                sourceType,
                sourceSlot,
                targetType,
                targetSlot,
                mouseButton,
                actionType,
                ++sequence
        ));
    }

    private void setStatus(String translationKey, boolean error) {
        statusMessage = Component.translatable(translationKey);
        statusColor = error ? 0xFFFF7777 : 0xFFE8DFA8;
    }

    private RpgEquipmentScreenModel currentModel() {
        Minecraft client = Minecraft.getInstance();
        List<ItemStack> inventory = copyInventory(client);
        ItemStack selected = selectedSource.resolve(client);
        Set<ArpgEquipmentSlot> allowedSlots = allowedSlots(selected);
        CharacterScreenModel characterModel = ClientArpgState.getCharacterScreenModel();
        return RpgEquipmentScreenModel.create(
                ClientArpgState.copyEquipmentSlots(),
                inventory,
                selected,
                allowedSlots,
                characterModel.getLevel(),
                characterModel.getTotalAttributes(),
                characterModel.getCurrentStats()
        );
    }

    private static List<ItemStack> copyInventory(Minecraft client) {
        if (client.player == null) {
            return List.of();
        }
        List<ItemStack> stacks = new ArrayList<>(36);
        for (int i = 0; i < 36; i++) {
            stacks.add(client.player.getInventory().getItem(i).copy());
        }
        return stacks;
    }

    private static Set<ArpgEquipmentSlot> allowedSlots(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !CLIENT_ITEM_SERVICE.hasArpgData(stack)) {
            return Set.of();
        }
        return CLIENT_ITEM_SERVICE.read(stack)
                .data()
                .flatMap(data -> ArpgItemSystems.bootstrapResult().itemBases().get(data.itemBaseId()))
                .map(base -> base.validSlots().isEmpty() ? Set.<ArpgEquipmentSlot>of() : EnumSet.copyOf(base.validSlots()))
                .orElse(Set.of());
    }

    private Component allowedSlotsText(Set<ArpgEquipmentSlot> slots) {
        String text = slots.stream()
                .map(slot -> Component.translatable(slot.translationKey()).getString())
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        return Component.translatable("ui.relicwrought.inventory.selected_allowed_slots", text);
    }

    private void rebuildBounds() {
        int panelX = panelX();
        int panelY = panelY();
        int equipmentOriginX = panelX + EQUIPMENT_ORIGIN_X;
        int equipmentOriginY = panelY + EQUIPMENT_ORIGIN_Y;
        List<EquipmentBounds> nextEquipmentBounds = new ArrayList<>();
        for (RpgEquipmentScreenModel.SlotView slot : currentModel().slots()) {
            nextEquipmentBounds.add(new EquipmentBounds(
                    slot.slot(),
                    equipmentOriginX + slot.x(),
                    equipmentOriginY + slot.y(),
                    SLOT_SIZE,
                    SLOT_SIZE
            ));
        }
        equipmentBounds = List.copyOf(nextEquipmentBounds);

        int inventoryOriginX = panelX + (PANEL_WIDTH - 9 * SLOT_STEP) / 2;
        int inventoryOriginY = panelY + INVENTORY_ORIGIN_Y;
        List<InventoryBounds> nextInventoryBounds = new ArrayList<>();
        for (RpgEquipmentScreenModel.InventorySlotView slot : currentModel().inventorySlots()) {
            nextInventoryBounds.add(new InventoryBounds(
                    slot.inventoryIndex(),
                    inventoryOriginX + slot.x(),
                    inventoryOriginY + slot.y(),
                    SLOT_SIZE,
                    SLOT_SIZE
            ));
        }
        for (RpgEquipmentScreenModel.InventorySlotView slot : currentModel().hotbarSlots()) {
            nextInventoryBounds.add(new InventoryBounds(
                    slot.inventoryIndex(),
                    inventoryOriginX + slot.x(),
                    inventoryOriginY + slot.y(),
                    SLOT_SIZE,
                    SLOT_SIZE
            ));
        }
        inventoryBounds = List.copyOf(nextInventoryBounds);
    }

    private EquipmentBounds findEquipmentSlot(double mouseX, double mouseY) {
        for (EquipmentBounds bounds : equipmentBounds) {
            if (contains(mouseX, mouseY, bounds.x(), bounds.y(), bounds.width(), bounds.height())) {
                return bounds;
            }
        }
        return null;
    }

    private InventoryBounds findInventorySlot(double mouseX, double mouseY) {
        for (InventoryBounds bounds : inventoryBounds) {
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

    private record EquipmentBounds(ArpgEquipmentSlot slot, int x, int y, int width, int height) {
    }

    private record InventoryBounds(int inventoryIndex, int x, int y, int width, int height) {
    }

    private record ClickSource(Type type, int slot) {
        static ClickSource none() {
            return new ClickSource(Type.NONE, -1);
        }

        static ClickSource inventory(int slot) {
            return new ClickSource(Type.INVENTORY, slot);
        }

        static ClickSource equipment(int slot) {
            return new ClickSource(Type.EQUIPMENT, slot);
        }

        ItemStack resolve(Minecraft client) {
            if (client.player == null) {
                return ItemStack.EMPTY;
            }
            if (type == Type.INVENTORY && slot >= 0 && slot < 36) {
                return client.player.getInventory().getItem(slot).copy();
            }
            if (type == Type.EQUIPMENT && slot >= 0 && slot < ArpgEquipmentSlot.values().length) {
                return ClientArpgState.getEquipmentStack(ArpgEquipmentSlot.values()[slot]);
            }
            return ItemStack.EMPTY;
        }

        enum Type {
            NONE,
            INVENTORY,
            EQUIPMENT
        }
    }
}
