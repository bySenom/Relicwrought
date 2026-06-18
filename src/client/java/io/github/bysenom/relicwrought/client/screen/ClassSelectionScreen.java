package io.github.bysenom.relicwrought.client.screen;

import io.github.bysenom.relicwrought.network.ClassSelectionRequest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ClassSelectionScreen extends Screen {
    private static final List<ClassOption> CLASSES = List.of(
            new ClassOption("warrior", "class.relicwrought.warrior", "class.relicwrought.warrior.description"),
            new ClassOption("ranger", "class.relicwrought.ranger", "class.relicwrought.ranger.description"),
            new ClassOption("arcanist", "class.relicwrought.arcanist", "class.relicwrought.arcanist.description"),
            new ClassOption("rogue", "class.relicwrought.rogue", "class.relicwrought.rogue.description")
    );

    private String selectedClassId = null;
    private Component statusMessage = Component.empty();
    private boolean selectionSent = false;

    public ClassSelectionScreen() {
        super(Component.translatable("screen.relicwrought.class_selection.title"));
    }

    @Override
    protected void init() {
        super.init();
        LinearLayout layout = LinearLayout.vertical().spacing(8);
        layout.defaultCellSetting().alignHorizontallyCenter();

        layout.addChild(new StringWidget(0, 0, 200, 20,
                Component.translatable("screen.relicwrought.class_selection.title"), font));

        for (ClassOption option : CLASSES) {
            Button classBtn = Button.builder(
                    Component.translatable(option.displayKey()),
                    btn -> selectClass(option.id())
            ).size(200, 20).build();

            if (selectionSent) {
                classBtn.active = false;
            }
            layout.addChild(classBtn);
        }

        Button confirmBtn = Button.builder(
                Component.translatable("screen.relicwrought.class_selection.confirm"),
                btn -> confirmSelection()
        ).size(200, 20).build();

        if (selectedClassId == null || selectionSent) {
            confirmBtn.active = false;
        }
        layout.addChild(confirmBtn);

        layout.arrangeElements();
        FrameLayout.centerInRectangle(layout, 0, 0, width, height);
        layout.visitWidgets(this::addRenderableWidget);
    }

    private void selectClass(String classId) {
        if (selectionSent) return;
        this.selectedClassId = classId;
        this.statusMessage = Component.translatable("screen.relicwrought.class_selection.selected",
                Component.translatable(getClassDisplayKey(classId)));
        rebuildWidgets();
    }

    private void confirmSelection() {
        if (selectedClassId == null || selectionSent) return;
        selectionSent = true;
        ClientPlayNetworking.send(new ClassSelectionRequest(selectedClassId));
        this.statusMessage = Component.translatable("screen.relicwrought.class_selection.selection_sent");
        rebuildWidgets();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(guiGraphics);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        if (statusMessage != null && !statusMessage.getString().isEmpty()) {
            guiGraphics.centeredText(font, statusMessage, width / 2, height / 2 + 60, 0xFFFFFF);
        }

        if (selectedClassId != null) {
            String descKey = getClassDescriptionKey(selectedClassId);
            Component desc = Component.translatable(descKey);
            guiGraphics.centeredText(font, desc, width / 2, height / 2 + 80, 0xAAAAAA);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private String getClassDisplayKey(String classId) {
        return "class.relicwrought." + classId;
    }

    private String getClassDescriptionKey(String classId) {
        return "class.relicwrought." + classId + ".description";
    }

    public static void open() {
        Minecraft.getInstance().setScreenAndShow(new ClassSelectionScreen());
    }

    private record ClassOption(String id, String displayKey, String descriptionKey) {}
}
