package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.progression.CharacterAttribute;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public record PlayerProgressionSyncPayload(
        int characterLevel,
        long currentLevelXp,
        long xpForNextLevel,
        long totalXp,
        int unspentAttributePoints,
        Map<CharacterAttribute, Integer> allocatedAttributes,
        Map<CharacterAttribute, Integer> totalAttributes
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerProgressionSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "progression_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerProgressionSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, PlayerProgressionSyncPayload::characterLevel,
                    ByteBufCodecs.VAR_LONG, PlayerProgressionSyncPayload::currentLevelXp,
                    ByteBufCodecs.VAR_LONG, PlayerProgressionSyncPayload::xpForNextLevel,
                    ByteBufCodecs.VAR_LONG, PlayerProgressionSyncPayload::totalXp,
                    ByteBufCodecs.INT, PlayerProgressionSyncPayload::unspentAttributePoints,
                    ByteBufCodecs.map(
                            LinkedHashMap::new,
                            ByteBufCodecs.STRING_UTF8.map(CharacterAttribute::valueOf, CharacterAttribute::name),
                            ByteBufCodecs.INT,
                            CharacterAttribute.values().length
                    ), PlayerProgressionSyncPayload::allocatedAttributes,
                    ByteBufCodecs.map(
                            LinkedHashMap::new,
                            ByteBufCodecs.STRING_UTF8.map(CharacterAttribute::valueOf, CharacterAttribute::name),
                            ByteBufCodecs.INT,
                            CharacterAttribute.values().length
                    ), PlayerProgressionSyncPayload::totalAttributes,
                    PlayerProgressionSyncPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
