package io.github.bysenom.relicwrought.recipe;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.ArpgModConfig;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeManager;

public final class RecipeRemovalHandler {
    private final ArpgModConfig config;

    public RecipeRemovalHandler(ArpgModConfig config) {
        this.config = config;
    }

    public void filterRecipes(
            RecipeManager recipeManager,
            RegistryAccess registryAccess,
            ResourceManager resourceManager
    ) {
        if (!config.disableVanillaEquipmentRecipes()) return;

        Relicwrought.LOGGER.warn("Recipe removal requested but not yet implemented in this version. " +
                "Vanilla equipment recipes will NOT be disabled.");
    }
}
