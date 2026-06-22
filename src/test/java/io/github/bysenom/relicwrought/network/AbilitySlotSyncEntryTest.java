package io.github.bysenom.relicwrought.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

final class AbilitySlotSyncEntryTest {
    @Test
    void entryRoundtripWithId() {
        AbilitySlotSyncEntry original = new AbilitySlotSyncEntry(3, Optional.of(Identifier.parse("relicwrought:power_strike")));
        ByteBuf buffer = Unpooled.buffer();
        AbilitySlotSyncEntry.CODEC.encode(buffer, original);
        AbilitySlotSyncEntry decoded = AbilitySlotSyncEntry.CODEC.decode(buffer);
        assertEquals(3, decoded.slotIndex());
        assertTrue(decoded.abilityId().isPresent());
        assertEquals("relicwrought:power_strike", decoded.abilityId().get().toString());
    }

    @Test
    void entryRoundtripEmptyId() {
        AbilitySlotSyncEntry original = new AbilitySlotSyncEntry(7, Optional.empty());
        ByteBuf buffer = Unpooled.buffer();
        AbilitySlotSyncEntry.CODEC.encode(buffer, original);
        AbilitySlotSyncEntry decoded = AbilitySlotSyncEntry.CODEC.decode(buffer);
        assertEquals(7, decoded.slotIndex());
        assertTrue(decoded.abilityId().isEmpty());
    }

    @Test
    void entryRejectsNegativeSlotIndex() {
        assertThrows(IllegalArgumentException.class, () -> new AbilitySlotSyncEntry(-1, Optional.empty()));
    }

    @Test
    void entryRejectsTooLargeSlotIndex() {
        assertThrows(IllegalArgumentException.class, () -> new AbilitySlotSyncEntry(9, Optional.empty()));
    }

    @Test
    void entryRejectsNullOptional() {
        assertThrows(IllegalArgumentException.class, () -> new AbilitySlotSyncEntry(0, null));
    }

    @Test
    void payloadRoundtripFullLoadout() {
        List<AbilitySlotSyncEntry> slots = new ArrayList<>(9);
        slots.add(new AbilitySlotSyncEntry(0, Optional.of(Identifier.parse("relicwrought:power_strike"))));
        slots.add(new AbilitySlotSyncEntry(1, Optional.of(Identifier.parse("relicwrought:fire_bolt"))));
        slots.add(new AbilitySlotSyncEntry(2, Optional.empty()));
        slots.add(new AbilitySlotSyncEntry(3, Optional.of(Identifier.parse("relicwrought:quick_jab"))));
        slots.add(new AbilitySlotSyncEntry(4, Optional.empty()));
        slots.add(new AbilitySlotSyncEntry(5, Optional.of(Identifier.parse("relicwrought:second_wind"))));
        slots.add(new AbilitySlotSyncEntry(6, Optional.empty()));
        slots.add(new AbilitySlotSyncEntry(7, Optional.empty()));
        slots.add(new AbilitySlotSyncEntry(8, Optional.empty()));
        AbilityLoadoutSyncPayload original = new AbilityLoadoutSyncPayload(slots);

        ByteBuf raw = Unpooled.buffer();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(raw, RegistryAccess.EMPTY);
        AbilityLoadoutSyncPayload.STREAM_CODEC.encode(buf, original);

        RegistryFriendlyByteBuf buf2 = new RegistryFriendlyByteBuf(raw, null);
        AbilityLoadoutSyncPayload decoded = AbilityLoadoutSyncPayload.STREAM_CODEC.decode(buf2);

        assertEquals(9, decoded.slots().size());
        assertEquals("relicwrought:power_strike", decoded.slots().get(0).abilityId().get().toString());
        assertEquals("relicwrought:fire_bolt", decoded.slots().get(1).abilityId().get().toString());
        assertTrue(decoded.slots().get(2).abilityId().isEmpty());
        assertEquals("relicwrought:quick_jab", decoded.slots().get(3).abilityId().get().toString());
        assertTrue(decoded.slots().get(4).abilityId().isEmpty());
        assertEquals("relicwrought:second_wind", decoded.slots().get(5).abilityId().get().toString());
        assertTrue(decoded.slots().get(6).abilityId().isEmpty());
        assertTrue(decoded.slots().get(7).abilityId().isEmpty());
        assertTrue(decoded.slots().get(8).abilityId().isEmpty());
    }

    @Test
    void payloadRoundtripEmptyLoadout() {
        List<AbilitySlotSyncEntry> slots = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) slots.add(new AbilitySlotSyncEntry(i, Optional.empty()));
        AbilityLoadoutSyncPayload original = new AbilityLoadoutSyncPayload(slots);

        ByteBuf raw = Unpooled.buffer();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(raw, RegistryAccess.EMPTY);
        AbilityLoadoutSyncPayload.STREAM_CODEC.encode(buf, original);

        RegistryFriendlyByteBuf buf2 = new RegistryFriendlyByteBuf(raw, null);
        AbilityLoadoutSyncPayload decoded = AbilityLoadoutSyncPayload.STREAM_CODEC.decode(buf2);

        for (int i = 0; i < 9; i++) {
            assertTrue(decoded.slots().get(i).abilityId().isEmpty());
            assertEquals(i, decoded.slots().get(i).slotIndex());
        }
    }

    @Test
    void payloadNormalizesToExactly9Slots() {
        List<AbilitySlotSyncEntry> tooFew = new ArrayList<>(2);
        tooFew.add(new AbilitySlotSyncEntry(0, Optional.of(Identifier.parse("relicwrought:power_strike"))));
        tooFew.add(new AbilitySlotSyncEntry(4, Optional.empty()));
        AbilityLoadoutSyncPayload original = new AbilityLoadoutSyncPayload(tooFew);

        ByteBuf raw = Unpooled.buffer();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(raw, RegistryAccess.EMPTY);
        AbilityLoadoutSyncPayload.STREAM_CODEC.encode(buf, original);

        RegistryFriendlyByteBuf buf2 = new RegistryFriendlyByteBuf(raw, null);
        AbilityLoadoutSyncPayload decoded = AbilityLoadoutSyncPayload.STREAM_CODEC.decode(buf2);

        assertEquals(9, decoded.slots().size());
        assertEquals("relicwrought:power_strike", decoded.slots().get(0).abilityId().get().toString());
        assertTrue(decoded.slots().get(4).abilityId().isEmpty());
    }

    @Test
    void payloadNullInputBecomesEmpty() {
        ByteBuf raw = Unpooled.buffer();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(raw, RegistryAccess.EMPTY);
        AbilityLoadoutSyncPayload.STREAM_CODEC.encode(buf, null);

        RegistryFriendlyByteBuf buf2 = new RegistryFriendlyByteBuf(raw, null);
        AbilityLoadoutSyncPayload decoded = AbilityLoadoutSyncPayload.STREAM_CODEC.decode(buf2);

        assertEquals(9, decoded.slots().size());
        for (int i = 0; i < 9; i++) {
            assertTrue(decoded.slots().get(i).abilityId().isEmpty());
        }
    }

    @Test
    void payloadRejectsNullSlotsList() {
        assertThrows(IllegalArgumentException.class, () -> new AbilityLoadoutSyncPayload(null));
    }
}
