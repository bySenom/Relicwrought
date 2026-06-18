package io.github.bysenom.relicwrought.item.model;

public record AffixComponentDefinition(String stat, AffixScope scope, AffixOperation operation) {
    public AffixComponentDefinition {
        if (stat == null || stat.isBlank()) {
            throw new IllegalArgumentException("Affix component stat must not be blank");
        }
    }
}
