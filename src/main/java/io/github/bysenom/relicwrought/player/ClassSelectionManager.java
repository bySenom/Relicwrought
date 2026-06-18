package io.github.bysenom.relicwrought.player;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class ClassSelectionManager {
    private final DataRegistry<ClassDefinition> classes;
    private final DataRegistry<StarterKitDefinition> starterKits;
    private final PlayerProfileManager profileManager;
    private final StarterKitService kitService;

    public ClassSelectionManager(
            DataRegistry<ClassDefinition> classes,
            DataRegistry<StarterKitDefinition> starterKits,
            PlayerProfileManager profileManager,
            StarterKitService kitService
    ) {
        this.classes = classes;
        this.starterKits = starterKits;
        this.profileManager = profileManager;
        this.kitService = kitService;
    }

    public SelectionResult selectClass(ServerPlayer player, String classId) {
        UUID uuid = player.getUUID();

        ClassDefinition classDef = resolveClass(classId);
        if (classDef == null) {
            return SelectionResult.failure("Class not found: " + classId);
        }
        if (!classDef.enabled()) {
            return SelectionResult.failure("Class is disabled: " + classId);
        }

        if (profileManager.hasSelectedClass(uuid)) {
            return SelectionResult.failure("Player has already selected a class");
        }

        StarterKitDefinition kit = resolveKit(classDef.starterKitId());
        if (kit == null) {
            return SelectionResult.failure("Starter kit not found for class: " + classDef.starterKitId());
        }

        Relicwrought.LOGGER.info("Player {} selecting class {} with kit {}",
                player.getName().getString(), classId, kit.id());

        long now = System.currentTimeMillis();
        PlayerArpgProfile updatedProfile = profileManager.getProfile(uuid)
                .withClassSelected(classDef.id().toString(), now);
        profileManager.saveProfile(uuid, updatedProfile);

        StarterKitService.KitGrantResult grantResult = kitService.grantKit(player, kit);

        if (!grantResult.completeSuccess()) {
            Relicwrought.LOGGER.warn("Kit grant partial for {}: inserted={}, dropped={}, failed={}",
                    uuid, grantResult.inserted(), grantResult.dropped(), grantResult.failed());
        }

        return SelectionResult.success(grantResult);
    }

    public boolean hasSelectedClass(ServerPlayer player) {
        return profileManager.hasSelectedClass(player.getUUID());
    }

    public PlayerArpgProfile getProfile(ServerPlayer player) {
        return profileManager.getProfile(player.getUUID());
    }

    public void resetPlayer(ServerPlayer player) {
        profileManager.saveProfile(player.getUUID(), PlayerArpgProfile.empty());
    }

    private ClassDefinition resolveClass(String classId) {
        var key = io.github.bysenom.relicwrought.item.model.DefinitionKey.parse(classId, Relicwrought.MOD_ID);
        return classes.get(key).orElse(null);
    }

    private StarterKitDefinition resolveKit(String kitId) {
        var key = io.github.bysenom.relicwrought.item.model.DefinitionKey.parse(kitId, Relicwrought.MOD_ID);
        return starterKits.get(key).orElse(null);
    }

    public record SelectionResult(
            boolean success,
            String message,
            StarterKitService.KitGrantResult grantResult
    ) {
        public static SelectionResult success(StarterKitService.KitGrantResult grantResult) {
            return new SelectionResult(true, "Class selected successfully", grantResult);
        }

        public static SelectionResult failure(String message) {
            return new SelectionResult(false, message, null);
        }
    }
}
