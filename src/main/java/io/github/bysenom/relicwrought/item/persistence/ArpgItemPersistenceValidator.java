package io.github.bysenom.relicwrought.item.persistence;

import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.scaling.NumberSafety;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ArpgItemPersistenceValidator {
    private ArpgItemPersistenceValidator() {
    }

    public static List<String> validate(ArpgItemData data) {
        List<String> errors = new ArrayList<>();

        if (data == null) {
            errors.add("item_data_null");
            return errors;
        }

        if (data.dataVersion() <= 0) {
            errors.add("invalid_data_version:" + data.dataVersion());
        }

        if (data.itemId() == null) {
            errors.add("missing_item_id");
        }

        if (data.itemBaseId() == null) {
            errors.add("missing_item_base_id");
        }

        if (data.itemLevel() == null) {
            errors.add("missing_item_level");
        } else {
            int level = data.itemLevel().value();
            if (level < ItemLevel.MIN || level > ItemLevel.MAX) {
                errors.add("invalid_item_level:" + level);
            }
        }

        if (data.requiredCharacterLevel() < 0) {
            errors.add("invalid_required_character_level:" + data.requiredCharacterLevel());
        }

        if (data.rarity() == null) {
            errors.add("missing_rarity");
        }

        if (data.quality() < ArpgItemData.MIN_QUALITY || data.quality() > ArpgItemData.MAX_QUALITY) {
            errors.add("invalid_quality:" + data.quality());
        }

        validateAffixRolls(data.prefixes(), "prefix", errors);
        validateAffixRolls(data.suffixes(), "suffix", errors);
        validateAffixRolls(data.implicitAffixes(), "implicit", errors);

        if (data.prefixes().size() > 3) {
            errors.add("too_many_prefixes:" + data.prefixes().size());
        }
        if (data.suffixes().size() > 3) {
            errors.add("too_many_suffixes:" + data.suffixes().size());
        }

        Set<io.github.bysenom.relicwrought.item.model.DefinitionKey> seenAffixIds = new HashSet<>();
        for (var roll : data.prefixes()) {
            if (!seenAffixIds.add(roll.affixId())) {
                errors.add("duplicate_affix:" + roll.affixId());
            }
        }
        for (var roll : data.suffixes()) {
            if (!seenAffixIds.add(roll.affixId())) {
                errors.add("duplicate_affix:" + roll.affixId());
            }
        }

        return errors;
    }

    private static void validateAffixRolls(List<?> rolls, String type, List<String> errors) {
        if (rolls == null) {
            errors.add("null_" + type + "_list");
            return;
        }
        for (Object roll : rolls) {
            if (!(roll instanceof io.github.bysenom.relicwrought.item.model.AffixRoll affixRoll)) {
                errors.add("invalid_" + type + "_roll_type");
                continue;
            }
            if (affixRoll.affixId() == null) {
                errors.add(type + "_missing_affix_id");
            }
            if (affixRoll.tier() == null) {
                errors.add(type + "_missing_tier");
            }
            double nr = affixRoll.normalizedRoll();
            if (Double.isNaN(nr) || nr < 0.0D || nr > 1.0D) {
                errors.add(type + "_invalid_normalized_roll:" + nr);
            }
            try {
                NumberSafety.requireFiniteNonNegative(affixRoll.value(), type + "_value");
            } catch (IllegalArgumentException e) {
                errors.add(type + "_invalid_value:" + e.getMessage());
            }
            if (affixRoll.dataVersion() <= 0) {
                errors.add(type + "_invalid_data_version:" + affixRoll.dataVersion());
            }
        }
    }
}
