package io.github.bysenom.relicwrought.combat.damage;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record CombatTextEvent(
        int targetEntityId,
        UUID targetUuid,
        UUID sourcePlayerUuid,
        double totalDamage,
        boolean critical,
        String damageTypeKey,
        long sequenceId,
        long serverGameTime
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, CombatTextEvent> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CombatTextEvent::targetEntityId,
            net.minecraft.core.UUIDUtil.STREAM_CODEC, CombatTextEvent::targetUuid,
            net.minecraft.core.UUIDUtil.STREAM_CODEC, CombatTextEvent::sourcePlayerUuid,
            ByteBufCodecs.DOUBLE, CombatTextEvent::totalDamage,
            ByteBufCodecs.BOOL, CombatTextEvent::critical,
            ByteBufCodecs.STRING_UTF8, CombatTextEvent::damageTypeKey,
            ByteBufCodecs.VAR_LONG, CombatTextEvent::sequenceId,
            ByteBufCodecs.VAR_LONG, CombatTextEvent::serverGameTime,
            CombatTextEvent::new
    );
}
