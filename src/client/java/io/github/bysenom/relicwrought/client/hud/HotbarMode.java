package io.github.bysenom.relicwrought.client.hud;

public enum HotbarMode {
    ITEM,
    ABILITY;

    public HotbarMode toggle() {
        return this == ITEM ? ABILITY : ITEM;
    }
}
