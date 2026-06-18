package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.combat.damage.CombatTextEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FloatingDamageNumberPayload(CombatTextEvent event) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FloatingDamageNumberPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "floating_damage"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FloatingDamageNumberPayload> CODEC =
            StreamCodec.composite(
                    CombatTextEvent.CODEC, FloatingDamageNumberPayload::event,
                    FloatingDamageNumberPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
