package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record WeaponCooldownSyncPayload(
        int cooldownDurationTicks,
        double attackSpeed,
        boolean triggerCooldownReset
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WeaponCooldownSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "weapon_cooldown_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponCooldownSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, WeaponCooldownSyncPayload::cooldownDurationTicks,
                    ByteBufCodecs.DOUBLE, WeaponCooldownSyncPayload::attackSpeed,
                    ByteBufCodecs.BOOL, WeaponCooldownSyncPayload::triggerCooldownReset,
                    WeaponCooldownSyncPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
