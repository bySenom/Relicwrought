package io.github.bysenom.relicwrought.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record AbilitySlotSyncEntry(int slotIndex, Optional<Identifier> abilityId) {
    public AbilitySlotSyncEntry {
        if (slotIndex < 0 || slotIndex > 8) {
            throw new IllegalArgumentException("slotIndex must be 0-8, got " + slotIndex);
        }
        if (abilityId == null) {
            throw new IllegalArgumentException("abilityId must not be null (use Optional.empty())");
        }
    }

    public static final StreamCodec<ByteBuf, AbilitySlotSyncEntry> CODEC = new StreamCodec<>() {
        @Override
        public AbilitySlotSyncEntry decode(ByteBuf buffer) {
            int idx = ByteBufCodecs.VAR_INT.decode(buffer);
            boolean hasAbility = buffer.readBoolean();
            Identifier id = hasAbility ? Identifier.STREAM_CODEC.decode(buffer) : null;
            return new AbilitySlotSyncEntry(idx, Optional.ofNullable(id));
        }

        @Override
        public void encode(ByteBuf buffer, AbilitySlotSyncEntry entry) {
            ByteBufCodecs.VAR_INT.encode(buffer, entry.slotIndex());
            Optional<Identifier> opt = entry.abilityId();
            buffer.writeBoolean(opt != null && opt.isPresent());
            if (opt != null && opt.isPresent()) {
                Identifier.STREAM_CODEC.encode(buffer, opt.get());
            }
        }
    };
}
