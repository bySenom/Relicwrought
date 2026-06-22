package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CharacterStatSyncPayload(CharacterCombatStats stats) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CharacterStatSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Relicwrought.MOD_ID, "character_stat_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CharacterStatSyncPayload> CODEC = new StreamCodec<>() {
        @Override
        public CharacterStatSyncPayload decode(RegistryFriendlyByteBuf buf) {
            return new CharacterStatSyncPayload(new CharacterCombatStats(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble()
            ));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CharacterStatSyncPayload payload) {
            CharacterCombatStats s = payload.stats();
            buf.writeDouble(s.flatPhysicalDamage());
            buf.writeDouble(s.fireDamage());
            buf.writeDouble(s.coldDamage());
            buf.writeDouble(s.lightningDamage());
            buf.writeDouble(s.poisonDamage());
            buf.writeDouble(s.physicalDamagePercent());
            buf.writeDouble(s.elementalDamagePercent());
            buf.writeDouble(s.attackSpeedPercent());
            buf.writeDouble(s.criticalStrikeChance());
            buf.writeDouble(s.criticalStrikeMultiplier());
            buf.writeDouble(s.eliteDamageBonus());
            buf.writeDouble(s.bossDamageBonus());
            buf.writeDouble(s.armor());
            buf.writeDouble(s.maximumLife());
            buf.writeDouble(s.fireResistance());
            buf.writeDouble(s.coldResistance());
            buf.writeDouble(s.lightningResistance());
            buf.writeDouble(s.poisonResistance());
            buf.writeDouble(s.flatDamageReduction());
            buf.writeDouble(s.percentDamageReduction());
            buf.writeDouble(s.movementSpeed());
            buf.writeDouble(s.lifeRegeneration());
            buf.writeDouble(s.miningSpeedPercent());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
