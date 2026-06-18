package io.github.bysenom.relicwrought.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerProfileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "relicwrought_profiles.json";

    private final Map<UUID, PlayerArpgProfile> profiles = new ConcurrentHashMap<>();
    private final Path savePath;
    private boolean dirty = false;

    public PlayerProfileManager(Path worldSaveDir) {
        this.savePath = worldSaveDir.resolve(FILE_NAME);
        load();
    }

    public static PlayerProfileManager get(MinecraftServer server) {
        Path worldDir = server.getWorldPath(LevelResource.ROOT);
        return new PlayerProfileManager(worldDir);
    }

    public PlayerArpgProfile getProfile(UUID playerUuid) {
        return profiles.getOrDefault(playerUuid, PlayerArpgProfile.empty());
    }

    public void saveProfile(UUID playerUuid, PlayerArpgProfile profile) {
        profiles.put(playerUuid, profile);
        dirty = true;
        save();
    }

    public boolean hasSelectedClass(UUID playerUuid) {
        return getProfile(playerUuid).classSelected();
    }

    public boolean hasReceivedKit(UUID playerUuid) {
        return getProfile(playerUuid).starterKitGranted();
    }

    private void load() {
        if (Files.exists(savePath)) {
            try {
                String content = Files.readString(savePath);
                Map<String, PlayerProfileData> raw = GSON.fromJson(content,
                        new TypeToken<Map<String, PlayerProfileData>>() {}.getType());
                if (raw != null) {
                    for (var entry : raw.entrySet()) {
                        UUID uuid = UUID.fromString(entry.getKey());
                        PlayerProfileData data = entry.getValue();
                        profiles.put(uuid, new PlayerArpgProfile(
                                data.dataVersion, data.classSelected, data.classId,
                                data.starterKitGranted, data.starterKitId,
                                data.starterKitVersion, data.selectionTimestamp
                        ));
                    }
                }
            } catch (Exception e) {
                Relicwrought.LOGGER.warn("Failed to load player profiles: {}", e.getMessage());
            }
        }
    }

    public void save() {
        if (!dirty) return;
        try {
            Map<String, PlayerProfileData> raw = new java.util.LinkedHashMap<>();
            for (var entry : profiles.entrySet()) {
                PlayerArpgProfile p = entry.getValue();
                raw.put(entry.getKey().toString(), new PlayerProfileData(
                        p.dataVersion(), p.classSelected(), p.classId(),
                        p.starterKitGranted(), p.starterKitId(),
                        p.starterKitVersion(), p.selectionTimestamp()
                ));
            }
            Files.createDirectories(savePath.getParent());
            Files.writeString(savePath, GSON.toJson(raw));
            dirty = false;
        } catch (IOException e) {
            Relicwrought.LOGGER.warn("Failed to save player profiles: {}", e.getMessage());
        }
    }

    public void markDirty() {
        dirty = true;
    }

    private record PlayerProfileData(
            int dataVersion, boolean classSelected, String classId,
            boolean starterKitGranted, String starterKitId,
            int starterKitVersion, long selectionTimestamp
    ) {}
}
