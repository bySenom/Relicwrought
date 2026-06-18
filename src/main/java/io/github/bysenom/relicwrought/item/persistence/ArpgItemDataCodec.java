package io.github.bysenom.relicwrought.item.persistence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bysenom.relicwrought.item.model.AffixComponentRoll;
import io.github.bysenom.relicwrought.item.model.AffixOperation;
import io.github.bysenom.relicwrought.item.model.AffixRoll;
import io.github.bysenom.relicwrought.item.model.AffixScope;
import io.github.bysenom.relicwrought.item.model.AffixTier;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.model.Rarity;

import java.util.List;
import java.util.UUID;

public final class ArpgItemDataCodec {
    private ArpgItemDataCodec() {
    }

    static final Codec<DefinitionKey> DEFINITION_KEY_CODEC = Codec.STRING.xmap(
            value -> DefinitionKey.parse(value, "relicwrought"),
            DefinitionKey::toString
    );

    static final Codec<AffixTier> AFFIX_TIER_CODEC = Codec.STRING.xmap(
            AffixTier::valueOf,
            AffixTier::name
    );

    static final Codec<AffixScope> AFFIX_SCOPE_CODEC = Codec.STRING.xmap(
            AffixScope::valueOf,
            AffixScope::name
    );

    static final Codec<AffixOperation> AFFIX_OPERATION_CODEC = Codec.STRING.xmap(
            value -> AffixOperation.parse(value),
            AffixOperation::name
    );

    static final Codec<Rarity> RARITY_CODEC = Codec.STRING.xmap(
            value -> Rarity.valueOf(value.toUpperCase()),
            Rarity::name
    );

    static final Codec<ItemLevel> ITEM_LEVEL_CODEC = Codec.INT.xmap(
            ItemLevel::of,
            ItemLevel::value
    );

    static final Codec<AffixComponentRoll> AFFIX_COMPONENT_ROLL_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("stat").forGetter(AffixComponentRoll::stat),
                    AFFIX_SCOPE_CODEC.fieldOf("scope").forGetter(AffixComponentRoll::scope),
                    AFFIX_OPERATION_CODEC.fieldOf("operation").forGetter(AffixComponentRoll::operation),
                    Codec.DOUBLE.fieldOf("normalized_roll").forGetter(AffixComponentRoll::normalizedRoll),
                    Codec.DOUBLE.fieldOf("value").forGetter(AffixComponentRoll::value)
            ).apply(instance, AffixComponentRoll::new)
    );

    static final Codec<List<AffixComponentRoll>> AFFIX_COMPONENT_ROLL_LIST_CODEC = Codec.list(AFFIX_COMPONENT_ROLL_CODEC);

    static final Codec<AffixRoll> AFFIX_ROLL_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    DEFINITION_KEY_CODEC.fieldOf("affix_id").forGetter(AffixRoll::affixId),
                    AFFIX_TIER_CODEC.fieldOf("tier").forGetter(AffixRoll::tier),
                    Codec.DOUBLE.fieldOf("normalized_roll").forGetter(AffixRoll::normalizedRoll),
                    Codec.DOUBLE.fieldOf("value").forGetter(AffixRoll::value),
                    AFFIX_COMPONENT_ROLL_LIST_CODEC.optionalFieldOf("component_rolls", List.of()).forGetter(AffixRoll::componentRolls),
                    Codec.INT.fieldOf("data_version").forGetter(AffixRoll::dataVersion)
            ).apply(instance, AffixRoll::new)
    );

    static final Codec<List<AffixRoll>> AFFIX_ROLL_LIST_CODEC = Codec.list(AFFIX_ROLL_CODEC);

    public static final Codec<ArpgItemData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("data_version").forGetter(ArpgItemData::dataVersion),
                    Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("item_id").forGetter(ArpgItemData::itemId),
                    DEFINITION_KEY_CODEC.fieldOf("item_base_id").forGetter(ArpgItemData::itemBaseId),
                    ITEM_LEVEL_CODEC.fieldOf("item_level").forGetter(ArpgItemData::itemLevel),
                    Codec.INT.optionalFieldOf("required_character_level", 0).forGetter(ArpgItemData::requiredCharacterLevel),
                    RARITY_CODEC.fieldOf("rarity").forGetter(ArpgItemData::rarity),
                    Codec.INT.fieldOf("quality").forGetter(ArpgItemData::quality),
                    Codec.LONG.fieldOf("seed").forGetter(ArpgItemData::seed),
                    Codec.BOOL.optionalFieldOf("starter_item", false).forGetter(ArpgItemData::starterItem),
                    AFFIX_ROLL_LIST_CODEC.optionalFieldOf("implicit_affixes", List.of()).forGetter(ArpgItemData::implicitAffixes),
                    AFFIX_ROLL_LIST_CODEC.optionalFieldOf("prefixes", List.of()).forGetter(ArpgItemData::prefixes),
                    AFFIX_ROLL_LIST_CODEC.optionalFieldOf("suffixes", List.of()).forGetter(ArpgItemData::suffixes)
            ).apply(instance, ArpgItemData::new)
    );
}
