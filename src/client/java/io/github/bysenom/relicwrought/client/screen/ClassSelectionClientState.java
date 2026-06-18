package io.github.bysenom.relicwrought.client.screen;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.network.ClassSelectionRequest;
import io.github.bysenom.relicwrought.network.ClassSelectionResponse;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClassSelectionClientState {
    private static boolean classSelected = false;
    private static boolean awaitingSelection = false;

    private ClassSelectionClientState() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ClassSelectionResponse.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if ("prompt".equals(payload.message())) {
                    if (!classSelected) {
                        ClassSelectionScreen.open();
                    }
                } else if (payload.success()) {
                    classSelected = true;
                    awaitingSelection = false;
                    Relicwrought.LOGGER.info("Class selection confirmed: {}", payload.classId());
                    context.client().setScreenAndShow(null);
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.sendSystemMessage(
                                Component.translatable("class.relicwrought.selection_confirmed", payload.classId()));
                    }
                } else {
                    Relicwrought.LOGGER.warn("Class selection failed: {}", payload.message());
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.sendSystemMessage(
                                Component.literal("§c" + payload.message()));
                    }
                }
            });
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            classSelected = false;
            awaitingSelection = false;
        });
    }

    public static void requestClassSelection() {
        if (!classSelected && !awaitingSelection) {
            awaitingSelection = true;
            ClassSelectionScreen.open();
        }
    }

    public static boolean hasSelectedClass() {
        return classSelected;
    }

    public static boolean isAwaitingSelection() {
        return awaitingSelection;
    }
}
