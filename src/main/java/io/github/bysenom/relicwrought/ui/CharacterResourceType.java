package io.github.bysenom.relicwrought.ui;

public enum CharacterResourceType {
    NONE("ui.relicwrought.none"),
    MANA("ui.relicwrought.mana"),
    RAGE("ui.relicwrought.rage"),
    ENERGY("ui.relicwrought.energy");

    private final String translationKey;

    CharacterResourceType(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
