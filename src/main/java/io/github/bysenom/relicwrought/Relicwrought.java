package io.github.bysenom.relicwrought;

import io.github.bysenom.relicwrought.command.ArpgItemCommand;
import io.github.bysenom.relicwrought.command.ClassCommand;
import io.github.bysenom.relicwrought.command.ProgressionCommand;
import io.github.bysenom.relicwrought.equipment.EquipmentValidationService;
import io.github.bysenom.relicwrought.equipment.PlayerEquipmentRepository;
import io.github.bysenom.relicwrought.equipment.PlayerEquipmentService;
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
import io.github.bysenom.relicwrought.network.AttributeAllocationRequest;
import io.github.bysenom.relicwrought.network.ClassSelectionNetworking;
import io.github.bysenom.relicwrought.network.EquipmentOpenPayload;
import io.github.bysenom.relicwrought.network.EquipmentSlotClickPayload;
import io.github.bysenom.relicwrought.network.EquipmentSyncPayload;
import io.github.bysenom.relicwrought.network.EquipmentSyncRequestPayload;
import io.github.bysenom.relicwrought.network.PlayerProgressionSyncPayload;
import io.github.bysenom.relicwrought.player.ClassSelectionManager;
import io.github.bysenom.relicwrought.player.PlayerProfileManager;
import io.github.bysenom.relicwrought.player.StarterKitService;
import io.github.bysenom.relicwrought.progression.*;
import io.github.bysenom.relicwrought.recipe.RecipeRemovalHandler;
import io.github.bysenom.relicwrought.ui.PlayerHudSyncService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
    private static ProgressionManager progressionManager;
    private static MobExperienceResolver mobXpResolver;
    private static io.github.bysenom.relicwrought.combat.ArpgMeleeDamageHandler meleeDamageHandler;
    private static PlayerEquipmentRepository equipmentRepository;
    private static PlayerEquipmentService equipmentService;

    public static io.github.bysenom.relicwrought.combat.ArpgMeleeDamageHandler getMeleeDamageHandler() {
        return meleeDamageHandler;
    }

    public static PlayerEquipmentRepository getEquipmentRepository() {
        return equipmentRepository;
    }

    public static boolean openEquipmentScreen(ServerPlayer player) {
        if (player == null || config == null || !config.enableRpgInventory() || equipmentService == null) {
            return false;
        }
        equipmentService.sync(player);
        ServerPlayNetworking.send(player, new EquipmentOpenPayload());
        return true;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Relicwrought loaded. Ready for combat, progression, loot and quests.");

        config = ArpgModConfig.load(FabricLoader.getInstance().getConfigDir(), LOGGER);

        ArpgItemComponents.register();
        ClassSelectionNetworking.registerC2SPayloads();
        ClassSelectionNetworking.registerS2CPayloads();
        PayloadTypeRegistry.clientboundPlay().register(PlayerProgressionSyncPayload.TYPE, PlayerProgressionSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(AttributeAllocationRequest.TYPE, AttributeAllocationRequest.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(io.github.bysenom.relicwrought.network.PlayerHudSyncPayload.TYPE, io.github.bysenom.relicwrought.network.PlayerHudSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(io.github.bysenom.relicwrought.network.FloatingDamageNumberPayload.TYPE, io.github.bysenom.relicwrought.network.FloatingDamageNumberPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(io.github.bysenom.relicwrought.network.EnemyUiSyncPayload.TYPE, io.github.bysenom.relicwrought.network.EnemyUiSyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(io.github.bysenom.relicwrought.network.AbilitySlotInputPayload.TYPE, io.github.bysenom.relicwrought.network.AbilitySlotInputPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EquipmentOpenPayload.TYPE, EquipmentOpenPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EquipmentSyncPayload.TYPE, EquipmentSyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(EquipmentSlotClickPayload.TYPE, EquipmentSlotClickPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(EquipmentSyncRequestPayload.TYPE, EquipmentSyncRequestPayload.CODEC);
        io.github.bysenom.relicwrought.network.WeaponCooldownNetworking.registerPayloads();
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

        ServerPlayNetworking.registerGlobalReceiver(AttributeAllocationRequest.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                if (progressionManager == null) return;
                CharacterAttribute attr = payload.resolveAttribute();
                if (attr == null) {
                    player.sendSystemMessage(Component.translatable("command.relicwrought.progression.invalid_attribute", payload.attributeName()));
                    return;
                }
                var result = progressionManager.allocateAttribute(player, attr, payload.amount());
                if (!result.success()) {
                    player.sendSystemMessage(Component.literal("§c" + result.errorMessage()));
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(io.github.bysenom.relicwrought.network.AbilitySlotInputPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("ui.relicwrought.ability.not_implemented"));
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(EquipmentSlotClickPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                if (equipmentService != null) {
                    equipmentService.handleSlotClick(context.player(), payload.slot());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(EquipmentSyncRequestPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                if (equipmentService != null) {
                    equipmentService.sync(context.player());
                }
            });
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (config.enableRpgInventory()) {
                ArpgItemStackService equipmentItemService = new ArpgItemStackService(List.of());
                equipmentRepository = PlayerEquipmentRepository.get(server, equipmentItemService);
                equipmentService = new PlayerEquipmentService(
                        config,
                        equipmentRepository,
                        new EquipmentValidationService(config, equipmentItemService, definitions.itemBases())
                );
                LOGGER.info("RPG equipment window initialized with {} extra slots", io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot.extraSlots().size());
            }

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

            if (config.enableCharacterProgression() && config.enableClassSelection()) {
                if (profileManager == null) {
                    profileManager = PlayerProfileManager.get(server);
                }
                progressionManager = new ProgressionManager(definitions.progressionProfiles(), profileManager);
                mobXpResolver = new MobExperienceResolver(config);

                LOGGER.info("Character progression system initialized");
            }

            if (config.disableVanillaEquipmentRecipes()) {
                var recipeManager = server.getRecipeManager();
                var registryAccess = server.registryAccess();
                var resourceManager = server.getResourceManager();
                new RecipeRemovalHandler(config).filterRecipes(recipeManager, registryAccess, resourceManager);
            }
            
            if (config.enableArpgCombat()) {
                ArpgItemStackService itemService = new ArpgItemStackService(List.of());
                meleeDamageHandler = 
                    new io.github.bysenom.relicwrought.combat.ArpgMeleeDamageHandler(config, itemService, progressionManager, equipmentRepository);
                meleeDamageHandler.register();
                LOGGER.info("Combat system initialized");
                
                net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(srv -> {
                    if (!config.enableWeaponCooldownGating()) return;
                    for (ServerPlayer player : srv.getPlayerList().getPlayers()) {
                        io.github.bysenom.relicwrought.combat.cooldown.WeaponAttackState state = meleeDamageHandler.getCooldownManager().getState(player);
                        io.github.bysenom.relicwrought.item.model.ArpgItemData weaponData = null;
                        net.minecraft.world.item.ItemStack mainHand = player.getMainHandItem();
                        if (itemService.hasArpgData(mainHand)) {
                            weaponData = itemService.read(mainHand).data().orElse(null);
                        }
                        
                        java.util.UUID weaponUuid = weaponData != null ? weaponData.itemId() : null;
                        long currentTick = srv.getTickCount();
                        
                        if (state.checkWeaponSwap(currentTick, weaponUuid)) {
                            if (config.resetCooldownOnWeaponSwitch() && weaponUuid != null) {
                                state.resetCooldown(currentTick);
                                int cooldownDuration = meleeDamageHandler.getCooldownResolver().resolveCooldownTicks(player, weaponData);
                                double aps = meleeDamageHandler.getCooldownResolver().resolveAttackSpeed(player, weaponData);
                                state.update(currentTick, cooldownDuration, aps);
                                io.github.bysenom.relicwrought.network.WeaponCooldownNetworking.sendSync(player, state, true);
                            }
                        }
                    }
                });
            }

            net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(srv -> {
                if (!config.enableRelicwroughtHud() || srv.getTickCount() % 5 != 0) {
                    return;
                }
                for (ServerPlayer player : srv.getPlayerList().getPlayers()) {
                    PlayerHudSyncService.send(player, profileManager);
                }
            });
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

            if (config.enableCharacterProgression() && progressionManager != null) {
                var player = handler.getPlayer();
                var prog = progressionManager.getProgression(player);
                PlayerProgressionSyncPayload syncPayload = new PlayerProgressionSyncPayload(
                        prog.level().value(),
                        prog.currentLevelXp(),
                        progressionManager.getXpForNextLevel(player),
                        prog.totalXp(),
                        prog.unspentAttributePoints(),
                        prog.allocatedAttributes(),
                        progressionManager.getTotalAttributes(player)
                );
                ServerPlayNetworking.send(player, syncPayload);
            }

            PlayerHudSyncService.send(handler.getPlayer(), profileManager);
            if (equipmentService != null) {
                equipmentService.sync(handler.getPlayer());
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            mobDropHandler.onLivingDeath(entity);

            if (entity instanceof ServerPlayer deadPlayer && equipmentService != null) {
                equipmentService.handleDeath(deadPlayer);
            }

            if (config.enableCharacterProgression() && progressionManager != null && mobXpResolver != null) {
                if (!entity.level().isClientSide()) {
                    var killer = findPlayerKiller(entity);
                    if (killer instanceof ServerPlayer serverKiller) {
                        long xp = mobXpResolver.resolveXp(entity, serverKiller);
                        if (xp > 0) {
                            ExperienceGrantResult result = progressionManager.grantXp(serverKiller, xp);
                            if (result.success()) {
                                if (config.showXpGainMessages() && result.xpGranted() > 0) {
                                    serverKiller.sendSystemMessage(Component.translatable("command.relicwrought.progression.xp_gain", result.xpGranted()));
                                }
                                if (config.showLevelUpMessages() && result.levelUps() > 0) {
                                    serverKiller.sendSystemMessage(Component.translatable("command.relicwrought.progression.level_up",
                                            result.levelAfter().value(), result.newAttributePoints()));
                                }
                                var prog = progressionManager.getProgression(serverKiller);
                                PlayerProgressionSyncPayload syncPayload = new PlayerProgressionSyncPayload(
                                        prog.level().value(),
                                        prog.currentLevelXp(),
                                        progressionManager.getXpForNextLevel(serverKiller),
                                        prog.totalXp(),
                                        prog.unspentAttributePoints(),
                                        prog.allocatedAttributes(),
                                        progressionManager.getTotalAttributes(serverKiller)
                                );
                                ServerPlayNetworking.send(serverKiller, syncPayload);
                            }
                        }
                    }
                }
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (profileManager != null) {
                profileManager.save();
            }
            if (equipmentRepository != null) {
                equipmentRepository.save();
            }
            equipmentRepository = null;
            equipmentService = null;
            meleeDamageHandler = null;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            ArpgItemCommand.register(dispatcher);
            ArpgItemCommand.registerLootCommands(dispatcher, dropGenerator, profileResolver, itemGenerator,
                    definitions.itemBases());
            if (selectionManager != null) {
                ClassCommand.register(dispatcher, selectionManager);
            }
            if (progressionManager != null) {
                ProgressionCommand.register(dispatcher, progressionManager, config);
                io.github.bysenom.relicwrought.command.CombatCommand.register(dispatcher, progressionManager, config);
            }
            io.github.bysenom.relicwrought.command.RelicwroughtDebugCommand.register(dispatcher);
        });

        LOGGER.info("Relicwrought initialized: mob drops={}, recipe removal={}, class selection={}",
                config.enableArpgMobDrops(), config.disableVanillaEquipmentRecipes(),
                config.enableClassSelection());
    }

    public static ArpgModConfig config() {
        return config;
    }

    private static net.minecraft.world.entity.player.Player findPlayerKiller(net.minecraft.world.entity.LivingEntity entity) {
        if (entity.getLastAttacker() instanceof net.minecraft.world.entity.player.Player player) return player;
        if (entity.getKillCredit() instanceof net.minecraft.world.entity.player.Player player) return player;
        return null;
    }
}
