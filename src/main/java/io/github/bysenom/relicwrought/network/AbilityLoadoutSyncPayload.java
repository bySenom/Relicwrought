package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record AbilityLoadoutSyncPayload(List<AbilitySlotSyncEntry> slots) implements CustomPacketPayload {
    public static final int SLOT_COUNT = 9;

    public static final CustomPacketPayload.Type<AbilityLoadoutSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "ability_loadout_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityLoadoutSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AbilityLoadoutSyncPayload decode(RegistryFriendlyByteBuf buf) {
            int count = buf.readVarInt();
            List<AbilitySlotSyncEntry> slots = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                int slotIndex = buf.readVarInt();
                boolean hasAbility = buf.readBoolean();
                Identifier id = hasAbility ? buf.readIdentifier() : null;
                slots.add(new AbilitySlotSyncEntry(slotIndex, Optional.ofNullable(id)));
            }
            return new AbilityLoadoutSyncPayload(normalize(slots));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, AbilityLoadoutSyncPayload payload) {
            List<AbilitySlotSyncEntry> safe = normalize(payload == null ? List.of() : payload.slots());
            buf.writeVarInt(safe.size());
            for (AbilitySlotSyncEntry slot : safe) {
                buf.writeVarInt(Math.max(0, Math.min(8, slot.slotIndex())));
                Optional<Identifier> opt = slot.abilityId() == null ? Optional.empty() : slot.abilityId();
                if (opt.isPresent() && opt.get() != null) {
                    buf.writeBoolean(true);
                    buf.writeIdentifier(opt.get());
                } else {
                    buf.writeBoolean(false);
                }
            }
        }
    };

    public AbilityLoadoutSyncPayload {
        if (slots == null) {
            throw new IllegalArgumentException("slots must not be null");
        }
    }

    private static List<AbilitySlotSyncEntry> normalize(List<AbilitySlotSyncEntry> input) {
        List<AbilitySlotSyncEntry> result = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            result.add(new AbilitySlotSyncEntry(i, Optional.empty()));
        }
        if (input != null) {
            for (AbilitySlotSyncEntry entry : input) {
                if (entry == null) continue;
                int index = entry.slotIndex();
                if (index < 0 || index >= SLOT_COUNT) continue;
                Optional<Identifier> id = entry.abilityId();
                if (id == null || id.isEmpty()) {
                    result.set(index, new AbilitySlotSyncEntry(index, Optional.empty()));
                } else {
                    result.set(index, new AbilitySlotSyncEntry(index, Optional.of(id.get())));
                }
            }
        }
        return result;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
