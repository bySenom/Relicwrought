package io.github.bysenom.relicwrought.item.persistence;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public final class ArpgItemComponents {
    private ArpgItemComponents() {
    }

    private static final Codec<ArpgItemComponent> CODEC = ArpgItemDataCodec.CODEC.xmap(
            ArpgItemComponent::new,
            ArpgItemComponent::data
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, ArpgItemComponent> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);

    public static final DataComponentType<ArpgItemComponent> ARPG_ITEM_DATA = DataComponentType.<ArpgItemComponent>builder()
            .persistent(CODEC)
            .networkSynchronized(STREAM_CODEC)
            .cacheEncoding()
            .build();

    public static void register() {
        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath("relicwrought", "arpg_item_data"),
                ARPG_ITEM_DATA
        );
    }
}
