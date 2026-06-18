package io.github.bysenom.relicwrought.combat.damage;

public enum DamageType {
    PHYSICAL,
    FIRE,
    COLD,
    LIGHTNING,
    POISON,
    ARCANE,
    CHAOS,
    HOLY,
    SHADOW;

    public boolean isElemental() {
        return this == FIRE || this == COLD || this == LIGHTNING || this == POISON || this == ARCANE || this == HOLY || this == SHADOW;
    }
}
