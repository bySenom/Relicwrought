package io.github.bysenom.relicwrought.client;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.client.hud.AbilityHotbarState;
import io.github.bysenom.relicwrought.client.hud.AbilityInputRouter;
import io.github.bysenom.relicwrought.client.hud.HotbarMode;
import io.github.bysenom.relicwrought.client.screen.RpgEquipmentScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;

public class KeyBindingRegistry {
    private static boolean cKeyPressed = false;
    private static boolean rKeyPressed = false;
    private static boolean oKeyPressed = false;

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            long window = client.getWindow().handle();
            boolean cPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS;
            if (cPressed && !cKeyPressed && client.canInterruptScreen()) {
                RpgEquipmentScreen.open();
            }
            cKeyPressed = cPressed;

            boolean rPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
            if (rPressed && !rKeyPressed && client.canInterruptScreen()) {
                if (Relicwrought.config().enableAbilityHotbar()) {
                    AbilityHotbarState.toggleMode();
                }
            }
            rKeyPressed = rPressed;

            if (AbilityHotbarState.getCurrentMode() == HotbarMode.ABILITY) {
                for (int i = 0; i < 9; i++) {
                    while (client.options.keyHotbarSlots[i].consumeClick()) {
                        AbilityInputRouter.handleHotbarKey(i);
                    }
                }
            }

            boolean oPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_O) == GLFW.GLFW_PRESS;
            if (oPressed && !oKeyPressed && client.canInterruptScreen()) {
                RpgEquipmentScreen.open();
            }
            oKeyPressed = oPressed;
        });
    }
}
