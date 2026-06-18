package io.github.bysenom.relicwrought;

import io.github.bysenom.relicwrought.command.ArpgItemCommand;
import io.github.bysenom.relicwrought.command.ClassCommand;
import io.github.bysenom.relicwrought.item.ArpgItemSystems;
import io.github.bysenom.relicwrought.item.affix.AffixGenerator;
import io.github.bysenom.relicwrought.item.generation.ArpgItemGenerator;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemComponents;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import io.github.bysenom.relicwrought.item.registry.DefinitionLoadResult;
import io.github.bysenom.relicwrought.item.scaling.ItemStatScaler;
import io.github.bysenom.relicwrought.loot.ArpgDropGenerator;
import io.github.bysenom.relicwrought.loot.ArpgMobDropHandler;
import io.github.bysenom.relicwrought.loot.LootProfileResolver;
import io.github.bysenom.relicwrought.network.ClassSelectionNetworking;
import io.github.bysenom.relicwrought.player.ClassSelectionManager;
import io.github.bysenom.relicwrought.player.PlayerProfileManager;
import io.github.bysenom.relicwrought.player.StarterKitService;
import io.github.bysenom.relicwrought.recipe.RecipeRemovalHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class Relicwrought implements ModInitializer {
    public static final String MOD_ID = "relicwrought";
    public static final Logger LOGGER = LoggerFactory.getLogger("Relicwrought");

    private static ArpgModConfig config;
    private static PlayerProfileManager profileManager;
    private static ClassSelectionManager selectionManager;

    @Override
    public void onInitialize() {
        LOGGER.info("Relicwrought loaded. Ready for combat, progression, loot and quests.");

        config = ArpgModConfig.load(FabricLoader.getInstance().getConfigDir(), LOGGER);

        ArpgItemComponents.register();
        ClassSelectionNetworking.registerC2SPayloads();
        ClassSelectionNetworking.registerS2CPayloads();
        ArpgItemSystems.initialize();

        DefinitionLoadResult definitions = ArpgItemSystems.bootstrapResult();
        AffixGenerator affixGenerator = new AffixGenerator(definitions.affixes(), definitions.affixGroups());
        ArpgItemStackService stackService = new ArpgItemStackService(List.of());
        ItemStatScaler statScaler = new ItemStatScaler(definitions.scalingProfiles());
        ArpgItemGenerator itemGenerator = new ArpgItemGenerator(
                definitions.itemBases(), definitions.rarities(),
                affixGenerator, statScaler, stackService
        );

        ArpgDropGenerator dropGenerator = new ArpgDropGenerator(
                itemGenerator, definitions.lootProfiles(),
                definitions.itemBases(), definitions.rarities(), config
        );
        LootProfileResolver profileResolver = new LootProfileResolver(definitions.lootProfiles());
        ArpgMobDropHandler mobDropHandler = new ArpgMobDropHandler(dropGenerator, profileResolver, config);

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) ->
                mobDropHandler.onLivingDeath(entity)
        );

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (config.enableClassSelection()) {
                profileManager = PlayerProfileManager.get(server);
                StarterKitService kitService = new StarterKitService(itemGenerator);
                selectionManager = new ClassSelectionManager(
                        definitions.classes(), definitions.starterKits(),
                        profileManager, kitService
                );
                ClassSelectionNetworking.registerServerHandler(selectionManager);
                LOGGER.info("Class selection system initialized with {} classes, {} starter kits",
                        definitions.classes().size(), definitions.starterKits().size());
            }

            if (config.disableVanillaEquipmentRecipes()) {
                var recipeManager = server.getRecipeManager();
                var registryAccess = server.registryAccess();
                var resourceManager = server.getResourceManager();
                new RecipeRemovalHandler(config).filterRecipes(recipeManager, registryAccess, resourceManager);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (config.enableClassSelection() && selectionManager != null) {
                var player = handler.getPlayer();
                if (!selectionManager.hasSelectedClass(player)) {
                    if (config.showClassScreenOnFirstJoin()) {
                        ClassSelectionNetworking.sendClassSelectionPrompt(player);
                    }
                }
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (profileManager != null) {
                profileManager.save();
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            ArpgItemCommand.register(dispatcher);
            ArpgItemCommand.registerLootCommands(dispatcher, dropGenerator, profileResolver, itemGenerator,
                    definitions.itemBases());
            if (selectionManager != null) {
                ClassCommand.register(dispatcher, selectionManager);
            }
        });

        LOGGER.info("Relicwrought initialized: mob drops={}, recipe removal={}, class selection={}",
                config.enableArpgMobDrops(), config.disableVanillaEquipmentRecipes(),
                config.enableClassSelection());
    }

    public static ArpgModConfig config() {
        return config;
    }
}
