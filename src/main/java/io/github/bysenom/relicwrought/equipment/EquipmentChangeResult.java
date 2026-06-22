package io.github.bysenom.relicwrought.equipment;

public record EquipmentChangeResult(boolean success, String translationKey) {
    public static EquipmentChangeResult success(String translationKey) {
        return new EquipmentChangeResult(true, translationKey);
    }

    public static EquipmentChangeResult failure(String translationKey) {
        return new EquipmentChangeResult(false, translationKey);
    }
}
