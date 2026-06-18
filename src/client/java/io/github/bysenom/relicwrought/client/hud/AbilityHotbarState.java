package io.github.bysenom.relicwrought.client.hud;

public class AbilityHotbarState {
    private static HotbarMode currentMode = HotbarMode.ITEM;

    public static HotbarMode getCurrentMode() {
        return currentMode;
    }

    public static void toggleMode() {
        currentMode = currentMode.toggle();
    }

    public static void setMode(HotbarMode mode) {
        currentMode = mode;
    }
}
