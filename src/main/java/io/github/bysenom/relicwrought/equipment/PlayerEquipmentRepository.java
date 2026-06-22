package io.github.bysenom.relicwrought.equipment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerEquipmentRepository {
    public static final int CURRENT_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "relicwrought_equipment.json";

    private final Map<UUID, PlayerEquipmentData> equipmentByPlayer = new ConcurrentHashMap<>();
    private final Path savePath;
    private final ArpgItemStackService itemService;
    private final EquipmentStackCodec stackCodec;
    private boolean dirty = false;

    public PlayerEquipmentRepository(Path worldSaveDir, ArpgItemStackService itemService) {
        this(worldSaveDir, itemService, new ArpgEquipmentStackCodec(itemService));
    }

    public PlayerEquipmentRepository(Path worldSaveDir, ArpgItemStackService itemService, EquipmentStackCodec stackCodec) {
        this.savePath = worldSaveDir.resolve(FILE_NAME);
        this.itemService = itemService;
        this.stackCodec = stackCodec;
        load();
    }

    public static PlayerEquipmentRepository get(MinecraftServer server, ArpgItemStackService itemService) {
        return new PlayerEquipmentRepository(server.getWorldPath(LevelResource.ROOT), itemService);
    }

    public PlayerEquipmentData get(UUID playerUuid) {
        return equipmentByPlayer.getOrDefault(playerUuid, PlayerEquipmentData.empty());
    }

    public ItemStack getStack(UUID playerUuid, ArpgEquipmentSlot slot) {
        return get(playerUuid).get(slot);
    }

    public List<ArpgItemData> getArpgItems(UUID playerUuid) {
        return get(playerUuid).copySlots().values().stream()
                .filter(itemService::hasArpgData)
                .flatMap(stack -> itemService.read(stack).data().stream())
                .toList();
    }

    public void setStack(UUID playerUuid, ArpgEquipmentSlot slot, ItemStack stack) {
        PlayerEquipmentData next = get(playerUuid).withSlot(slot, stack);
        equipmentByPlayer.put(playerUuid, next);
        dirty = true;
        save();
    }

    public ItemStack removeStack(UUID playerUuid, ArpgEquipmentSlot slot) {
        ItemStack removed = get(playerUuid).get(slot);
        PlayerEquipmentData next = get(playerUuid).withoutSlot(slot);
        equipmentByPlayer.put(playerUuid, next);
        dirty = true;
        save();
        return removed;
    }

    public void clear(UUID playerUuid) {
        equipmentByPlayer.put(playerUuid, PlayerEquipmentData.empty());
        dirty = true;
        save();
    }

    public void save() {
        if (!dirty) {
            return;
        }
        try {
            Files.createDirectories(savePath.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("dataVersion", CURRENT_VERSION);
            JsonObject players = new JsonObject();
            for (var entry : equipmentByPlayer.entrySet()) {
                JsonObject player = new JsonObject();
                player.addProperty("dataVersion", CURRENT_VERSION);
                JsonObject slots = new JsonObject();
                for (var slotEntry : entry.getValue().copySlots().entrySet()) {
                    JsonObject stored = stackCodec.encode(slotEntry.getValue());
                    if (stored != null) {
                        slots.add(slotEntry.getKey().serializedName(), stored);
                    }
                }
                player.add("slots", slots);
                players.add(entry.getKey().toString(), player);
            }
            root.add("players", players);
            Files.writeString(savePath, GSON.toJson(root));
            dirty = false;
        } catch (IOException exception) {
            Relicwrought.LOGGER.warn("Failed to save Relicwrought equipment: {}", exception.getMessage());
        }
    }

    private void load() {
        if (!Files.exists(savePath)) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(savePath)).getAsJsonObject();
            JsonObject players = root.has("players") && root.get("players").isJsonObject()
                    ? root.getAsJsonObject("players")
                    : new JsonObject();
            for (var playerEntry : players.entrySet()) {
                UUID playerUuid = UUID.fromString(playerEntry.getKey());
                JsonObject player = playerEntry.getValue().getAsJsonObject();
                JsonObject slots = player.has("slots") && player.get("slots").isJsonObject()
                        ? player.getAsJsonObject("slots")
                        : new JsonObject();
                EnumMap<ArpgEquipmentSlot, ItemStack> loadedSlots = new EnumMap<>(ArpgEquipmentSlot.class);
                for (var slotEntry : slots.entrySet()) {
                    ArpgEquipmentSlot slot = ArpgEquipmentSlot.parseSerialized(slotEntry.getKey());
                    if (!slot.isExtraSlot()) {
                        continue;
                    }
                    ItemStack stack = stackCodec.decode(slotEntry.getValue().getAsJsonObject());
                    if (!stack.isEmpty()) {
                        loadedSlots.put(slot, stack);
                    }
                }
                equipmentByPlayer.put(playerUuid, PlayerEquipmentData.empty());
                PlayerEquipmentData data = PlayerEquipmentData.empty();
                for (var slotEntry : loadedSlots.entrySet()) {
                    data = data.withSlot(slotEntry.getKey(), slotEntry.getValue());
                }
                equipmentByPlayer.put(playerUuid, data);
            }
        } catch (Exception exception) {
            Relicwrought.LOGGER.warn("Failed to load Relicwrought equipment: {}", exception.getMessage());
        }
    }

}
