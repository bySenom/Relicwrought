package io.github.bysenom.relicwrought.item.model;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum ArpgEquipmentSlot {
    HEAD(false, EquipmentSlot.HEAD),
    NECK(true, null),
    SHOULDERS(true, null),
    CLOAK(true, null),
    CHEST(false, EquipmentSlot.CHEST),
    BELT(true, null),
    LEGS(false, EquipmentSlot.LEGS),
    FEET(false, EquipmentSlot.FEET),
    RING_1(true, null),
    RING_2(true, null),
    TRINKET_1(true, null),
    TRINKET_2(true, null),
    MAIN_HAND(false, EquipmentSlot.MAINHAND),
    OFF_HAND(false, EquipmentSlot.OFFHAND);

    private final boolean extraSlot;
    private final EquipmentSlot vanillaSlot;

    ArpgEquipmentSlot(boolean extraSlot, EquipmentSlot vanillaSlot) {
        this.extraSlot = extraSlot;
        this.vanillaSlot = vanillaSlot;
    }

    public boolean isExtraSlot() {
        return extraSlot;
    }

    public boolean isVanillaMappedSlot() {
        return vanillaSlot != null;
    }

    public Optional<EquipmentSlot> vanillaSlot() {
        return Optional.ofNullable(vanillaSlot);
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "ui.relicwrought.equipment." + serializedName();
    }

    public static ArpgEquipmentSlot parseSerialized(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Equipment slot must not be blank");
        }
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    public static List<ArpgEquipmentSlot> displayOrder() {
        return List.of(values());
    }

    public static List<ArpgEquipmentSlot> extraSlots() {
        return Arrays.stream(values()).filter(ArpgEquipmentSlot::isExtraSlot).toList();
    }
}
