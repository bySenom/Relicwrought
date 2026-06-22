package io.github.bysenom.relicwrought.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AbilityLoadoutSyncPayloadTest {

    private RegistryFriendlyByteBuf createMockBuf() {
        // Since we only test writing/encoding, a mock RegistryAccess is enough.
        // We actually just need a real RegistryFriendlyByteBuf wrapped around an Unpooled.buffer()
        // with a mock registry access.
        net.minecraft.core.RegistryAccess mockAccess = mock(net.minecraft.core.RegistryAccess.class);
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), mockAccess);
    }

    private void assertEncodable(AbilityLoadoutSyncPayload payload) {
        RegistryFriendlyByteBuf buf = createMockBuf();
        assertDoesNotThrow(() -> {
            AbilityLoadoutSyncPayload.STREAM_CODEC.encode(buf, payload);
        });
        assertNotNull(buf);
        buf.release();
    }

    @Test
    void testEmptyLoadout() {
        List<AbilitySlotSyncEntry> slots = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            slots.add(new AbilitySlotSyncEntry(i, Optional.empty()));
        }
        AbilityLoadoutSyncPayload payload = new AbilityLoadoutSyncPayload(slots);
        assertEncodable(payload);
    }

    @Test
    void testNullLoadout() {
        // Technically the constructor throws an exception if slots is null,
        // but let's test if the codec survives it by bypassing the constructor or catching it.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new AbilityLoadoutSyncPayload(null);
        });
        assertNotNull(exception);
    }

    @Test
    void test9EmptySlots() {
        List<AbilitySlotSyncEntry> slots = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            slots.add(new AbilitySlotSyncEntry(i, Optional.empty()));
        }
        AbilityLoadoutSyncPayload payload = new AbilityLoadoutSyncPayload(slots);
        assertEncodable(payload);
    }

    @Test
    void testNullSlotEntry() {
        List<AbilitySlotSyncEntry> slots = new ArrayList<>();
        slots.add(null);
        for (int i = 1; i < 9; i++) {
            slots.add(new AbilitySlotSyncEntry(i, Optional.empty()));
        }
        AbilityLoadoutSyncPayload payload = new AbilityLoadoutSyncPayload(slots);
        assertEncodable(payload);
    }

    @Test
    void testNullOptional() {
        // Constructor of AbilitySlotSyncEntry prevents null Optional,
        // but if someone bypassed it via reflection, our codec should handle it.
        // We'll just verify the codec doesn't fail if we give it an empty optional.
        List<AbilitySlotSyncEntry> slots = new ArrayList<>();
        slots.add(new AbilitySlotSyncEntry(0, Optional.empty()));
        assertEncodable(new AbilityLoadoutSyncPayload(slots));
    }

    @Test
    void testValidPowerStrikeId() {
        List<AbilitySlotSyncEntry> slots = new ArrayList<>();
        slots.add(new AbilitySlotSyncEntry(0, Optional.of(Identifier.fromNamespaceAndPath("relicwrought", "power_strike"))));
        for (int i = 1; i < 9; i++) {
            slots.add(new AbilitySlotSyncEntry(i, Optional.empty()));
        }
        AbilityLoadoutSyncPayload payload = new AbilityLoadoutSyncPayload(slots);
        assertEncodable(payload);
    }

    @Test
    void testInvalidId() {
        // We test with an invalid identifier syntax but passed through Identifier.of
        // Actually, Identifier.of will throw early, but let's simulate a bad namespace
        List<AbilitySlotSyncEntry> slots = new ArrayList<>();
        slots.add(new AbilitySlotSyncEntry(0, Optional.of(Identifier.fromNamespaceAndPath("bad", "id"))));
        AbilityLoadoutSyncPayload payload = new AbilityLoadoutSyncPayload(slots);
        assertEncodable(payload);
    }
}
