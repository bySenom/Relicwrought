package io.github.bysenom.relicwrought.item.affix;

import io.github.bysenom.relicwrought.item.model.AffixRoll;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ItemLevel;

import java.util.List;

public record AffixGenerationRequest(
        ItemBaseDefinition itemBase,
        ItemLevel itemLevel,
        AffixSlotLimits slotLimits,
        long seed,
        AffixGenerationPolicy policy,
        List<AffixRoll> existingAffixes
) {
    public AffixGenerationRequest(
            ItemBaseDefinition itemBase,
            ItemLevel itemLevel,
            AffixSlotLimits slotLimits,
            long seed,
            AffixGenerationPolicy policy
    ) {
        this(itemBase, itemLevel, slotLimits, seed, policy, List.of());
    }

    public AffixGenerationRequest {
        if (itemBase == null) {
            throw new IllegalArgumentException("Item base must not be null");
        }
        if (itemLevel == null) {
            throw new IllegalArgumentException("Item level must not be null");
        }
        if (slotLimits == null) {
            throw new IllegalArgumentException("Affix slot limits must not be null");
        }
        if (policy == null) {
            throw new IllegalArgumentException("Affix generation policy must not be null");
        }
        existingAffixes = List.copyOf(existingAffixes);
    }
}
