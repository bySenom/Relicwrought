package io.github.bysenom.relicwrought.equipment;

import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.item.ItemDataVersions;
import io.github.bysenom.relicwrought.item.model.*;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

final class PlayerEquipmentRepositoryTest {
    @TempDir
    Path tempDir;

    private final ArpgItemStackService itemService = mock(ArpgItemStackService.class);

    @Test
    void equipmentRoundtripPreservesArpgItemData() {
        UUID playerId = UUID.randomUUID();
        ItemStack ring = stack();
        FakeEquipmentStackCodec codec = new FakeEquipmentStackCodec();

        PlayerEquipmentRepository repository = new PlayerEquipmentRepository(tempDir, itemService, codec);
        repository.setStack(playerId, ArpgEquipmentSlot.RING_1, ring);

        PlayerEquipmentRepository reloaded = new PlayerEquipmentRepository(tempDir, itemService, codec);
        ItemStack loaded = reloaded.getStack(playerId, ArpgEquipmentSlot.RING_1);

        assertFalse(loaded.isEmpty());
        assertSame(ring, loaded);
        assertTrue(Files.exists(tempDir.resolve("relicwrought_equipment.json")));
    }

    @Test
    void removingSlotPersistsEmptySlot() {
        UUID playerId = UUID.randomUUID();
        ItemStack ring = stack();
        FakeEquipmentStackCodec codec = new FakeEquipmentStackCodec();
        PlayerEquipmentRepository repository = new PlayerEquipmentRepository(tempDir, itemService, codec);

        repository.setStack(playerId, ArpgEquipmentSlot.RING_2, ring);
        ItemStack removed = repository.removeStack(playerId, ArpgEquipmentSlot.RING_2);
        PlayerEquipmentRepository reloaded = new PlayerEquipmentRepository(tempDir, itemService, codec);

        assertSame(ring, removed);
        assertTrue(reloaded.getStack(playerId, ArpgEquipmentSlot.RING_2).isEmpty());
    }

    private static ItemStack stack() {
        ItemStack stack = mock(ItemStack.class);
        when(stack.isEmpty()).thenReturn(false);
        when(stack.getCount()).thenReturn(1);
        when(stack.copy()).thenReturn(stack);
        return stack;
    }

    private static final class FakeEquipmentStackCodec implements EquipmentStackCodec {
        private final Map<ItemStack, String> idsByStack = new IdentityHashMap<>();
        private final Map<String, ItemStack> stacksById = new java.util.HashMap<>();
        private int nextId = 1;

        @Override
        public JsonObject encode(ItemStack stack) {
            String id = idsByStack.computeIfAbsent(stack, ignored -> "stack-" + nextId++);
            stacksById.put(id, stack);
            JsonObject json = new JsonObject();
            json.addProperty("id", id);
            return json;
        }

        @Override
        public ItemStack decode(JsonObject json) {
            return stacksById.getOrDefault(json.get("id").getAsString(), ItemStack.EMPTY);
        }
    }
}
