package io.github.bysenom.relicwrought.ui;

public record CharacterResourceState(
        CharacterResourceType type,
        double currentValue,
        double maximumValue,
        int dataVersion
) {
    public static final int CURRENT_VERSION = 1;

    public CharacterResourceState {
        if (type == null) type = CharacterResourceType.NONE;
        if (maximumValue < 0) maximumValue = 0;
        if (Double.isNaN(maximumValue) || Double.isInfinite(maximumValue)) maximumValue = 0;
        
        if (currentValue < 0) currentValue = 0;
        if (Double.isNaN(currentValue) || Double.isInfinite(currentValue)) currentValue = 0;
        
        if (currentValue > maximumValue && type != CharacterResourceType.NONE) {
            currentValue = maximumValue;
        }
    }

    public static CharacterResourceState empty() {
        return new CharacterResourceState(CharacterResourceType.NONE, 0, 0, CURRENT_VERSION);
    }
    
    public static CharacterResourceState full(CharacterResourceType type, double maximum) {
        return new CharacterResourceState(type, maximum, maximum, CURRENT_VERSION);
    }
    
    public static CharacterResourceState emptyWithMax(CharacterResourceType type, double maximum) {
        return new CharacterResourceState(type, 0, maximum, CURRENT_VERSION);
    }
    
    public CharacterResourceState clamp() {
        return new CharacterResourceState(type, currentValue, maximumValue, dataVersion);
    }
}
