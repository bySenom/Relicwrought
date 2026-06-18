package io.github.bysenom.relicwrought.item.affix;

import io.github.bysenom.relicwrought.item.model.AffixRoll;

import java.util.List;

public record AffixGenerationResult(
        List<AffixRoll> prefixes,
        List<AffixRoll> suffixes,
        List<String> messages,
        boolean complete
) {
    public AffixGenerationResult {
        prefixes = List.copyOf(prefixes);
        suffixes = List.copyOf(suffixes);
        messages = List.copyOf(messages);
    }
}
