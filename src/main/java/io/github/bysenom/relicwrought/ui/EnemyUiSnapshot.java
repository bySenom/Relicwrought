package io.github.bysenom.relicwrought.ui;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record EnemyUiSnapshot(
        int entityId,
        UUID entityUuid,
        String displayName,
        EnemyClassification classification,
        int level,
        double currentHealth,
        double maximumHealth,
        boolean hostile,
        boolean boss,
        int dataVersion
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, EnemyUiSnapshot> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, EnemyUiSnapshot::entityId,
            net.minecraft.core.UUIDUtil.STREAM_CODEC, EnemyUiSnapshot::entityUuid,
            ByteBufCodecs.STRING_UTF8, EnemyUiSnapshot::displayName,
            ByteBufCodecs.idMapper(i -> EnemyClassification.values()[i], Enum::ordinal), EnemyUiSnapshot::classification,
            ByteBufCodecs.INT, EnemyUiSnapshot::level,
            ByteBufCodecs.DOUBLE, EnemyUiSnapshot::currentHealth,
            ByteBufCodecs.DOUBLE, EnemyUiSnapshot::maximumHealth,
            ByteBufCodecs.BOOL, EnemyUiSnapshot::hostile,
            ByteBufCodecs.BOOL, EnemyUiSnapshot::boss,
            ByteBufCodecs.INT, EnemyUiSnapshot::dataVersion,
            EnemyUiSnapshot::new
    );
}
