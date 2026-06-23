package io.github.bysenom.relicwrought.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.progression.CharacterAttribute;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
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
                Map<String, Object> rawMap = GSON.fromJson(content,
                        new TypeToken<Map<String, Object>>() {}.getType());
                if (rawMap != null) {
                    for (var entry : rawMap.entrySet()) {
                        UUID uuid = UUID.fromString(entry.getKey());
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) entry.getValue();
                        PlayerArpgProfile profile = deserializeProfile(data);
                        profiles.put(uuid, profile);
                    }
                }
            } catch (Exception e) {
                Relicwrought.LOGGER.warn("Failed to load player profiles: {}", e.getMessage());
            }
        }
    }

    private PlayerArpgProfile deserializeProfile(Map<String, Object> data) {
        int dataVersion = getInt(data, "dataVersion", 0);

        if (dataVersion == 0) {
            return PlayerArpgProfile.empty();
        }

        boolean classSelected = getBool(data, "classSelected", false);
        String classId = getStr(data, "classId", "");
        boolean starterKitGranted = getBool(data, "starterKitGranted", false);
        String starterKitId = getStr(data, "starterKitId", "");
        int starterKitVersion = getInt(data, "starterKitVersion", 0);
        long selectionTimestamp = getLong(data, "selectionTimestamp", 0L);

        if (dataVersion < 2) {
            Relicwrought.LOGGER.info("Migrating player profile v{} -> v{}", dataVersion, 2);
            PlayerArpgProfile v1 = new PlayerArpgProfile(
                    dataVersion, classSelected, classId, starterKitGranted,
                    starterKitId, starterKitVersion, selectionTimestamp,
                    io.github.bysenom.relicwrought.progression.CharacterLevel.MIN,
                    0L, 0L, 0, PlayerArpgProfile.emptyAttributes(), 0.0, 100.0, "NONE"
            );
            return PlayerArpgProfile.legacyV2ToV3(PlayerArpgProfile.legacyV1ToV2(v1));
        }

        int characterLevel = getInt(data, "characterLevel", io.github.bysenom.relicwrought.progression.CharacterLevel.MIN);
        long currentLevelXp = getLong(data, "currentLevelXp", 0L);
        long totalXp = getLong(data, "totalXp", 0L);
        int unspentAttributePoints = getInt(data, "unspentAttributePoints", 0);

        Map<CharacterAttribute, Integer> allocated = PlayerArpgProfile.emptyAttributes();
        if (data.containsKey("allocatedAttributes")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rawAttrs = (Map<String, Object>) data.get("allocatedAttributes");
            Map<CharacterAttribute, Integer> attrs = new EnumMap<>(CharacterAttribute.class);
            for (var attrEntry : rawAttrs.entrySet()) {
                try {
                    CharacterAttribute attr = CharacterAttribute.valueOf(attrEntry.getKey().toUpperCase());
                    attrs.put(attr, ((Number) attrEntry.getValue()).intValue());
                } catch (IllegalArgumentException e) {
                    Relicwrought.LOGGER.warn("Unknown attribute key: {}", attrEntry.getKey());
                }
            }
            for (var attr : CharacterAttribute.values()) {
                attrs.putIfAbsent(attr, 0);
            }
            allocated = Collections.unmodifiableMap(attrs);
        }
        
        if (dataVersion < 3) {
            Relicwrought.LOGGER.info("Migrating player profile v{} -> v{}", dataVersion, 3);
            PlayerArpgProfile v2 = new PlayerArpgProfile(
                    dataVersion, classSelected, classId, starterKitGranted,
                    starterKitId, starterKitVersion, selectionTimestamp,
                    characterLevel, currentLevelXp, totalXp, unspentAttributePoints, allocated, 0.0, 100.0, "NONE"
            );
            return PlayerArpgProfile.legacyV2ToV3(v2);
        }
        
        double currentResourceValue = getDouble(data, "currentResourceValue", 0.0);
        double maxResourceValue = getDouble(data, "maxResourceValue", 100.0);
        String resourceType = getStr(data, "resourceType", "NONE");

        return new PlayerArpgProfile(
                PlayerArpgProfile.CURRENT_VERSION,
                classSelected, classId, starterKitGranted,
                starterKitId, starterKitVersion, selectionTimestamp,
                characterLevel, currentLevelXp, totalXp,
                unspentAttributePoints, allocated, currentResourceValue, maxResourceValue, resourceType
        );
    }

    public void save() {
        if (!dirty) return;
        try {
            Map<String, Object> raw = new LinkedHashMap<>();
            for (var entry : profiles.entrySet()) {
                PlayerArpgProfile p = entry.getValue();
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("dataVersion", p.dataVersion());
                data.put("classSelected", p.classSelected());
                data.put("classId", p.classId());
                data.put("starterKitGranted", p.starterKitGranted());
                data.put("starterKitId", p.starterKitId());
                data.put("starterKitVersion", p.starterKitVersion());
                data.put("selectionTimestamp", p.selectionTimestamp());
                data.put("characterLevel", p.characterLevel());
                data.put("currentLevelXp", p.currentLevelXp());
                data.put("totalXp", p.totalXp());
                data.put("unspentAttributePoints", p.unspentAttributePoints());

                Map<String, Integer> attrs = new LinkedHashMap<>();
                if (p.allocatedAttributes() != null) {
                    for (var attrEntry : p.allocatedAttributes().entrySet()) {
                        attrs.put(attrEntry.getKey().name().toLowerCase(), attrEntry.getValue());
                    }
                }
                data.put("allocatedAttributes", attrs);
                data.put("currentResourceValue", p.currentResourceValue());
                data.put("maxResourceValue", p.maxResourceValue());
                data.put("resourceType", p.resourceType());

                raw.put(entry.getKey().toString(), data);
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

    private static int getInt(Map<String, Object> map, String key, int def) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.intValue();
        return def;
    }

    private static long getLong(Map<String, Object> map, String key, long def) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.longValue();
        return def;
    }
    
    private static double getDouble(Map<String, Object> map, String key, double def) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.doubleValue();
        return def;
    }

    private static boolean getBool(Map<String, Object> map, String key, boolean def) {
        Object v = map.get(key);
        if (v instanceof Boolean b) return b;
        return def;
    }

    private static String getStr(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        if (v instanceof String s) return s;
        return def;
    }
}
