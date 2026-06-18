package io.github.bysenom.relicwrought.client;

import io.github.bysenom.relicwrought.client.screen.CharacterScreenModel;
import io.github.bysenom.relicwrought.ui.CharacterResourceState;

public class ClientArpgState {
    private static final CharacterScreenModel characterScreenModel = new CharacterScreenModel();
    
    private static double currentHealth = 20.0;
    private static double maximumHealth = 20.0;
    private static CharacterResourceState resourceState = CharacterResourceState.empty();

    public static CharacterScreenModel getCharacterScreenModel() {
        return characterScreenModel;
    }
    
    public static void updateHud(double health, double maxHealth, CharacterResourceState resource) {
        currentHealth = health;
        maximumHealth = maxHealth;
        resourceState = resource;
    }

    public static double getCurrentHealth() { return currentHealth; }
    public static double getMaximumHealth() { return maximumHealth; }
    public static CharacterResourceState getResourceState() { return resourceState; }
}
