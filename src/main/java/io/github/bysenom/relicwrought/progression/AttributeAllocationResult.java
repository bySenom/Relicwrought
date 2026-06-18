package io.github.bysenom.relicwrought.progression;

public record AttributeAllocationResult(
        boolean success,
        CharacterAttribute attribute,
        int amountAllocated,
        int remainingPoints,
        int totalAllocated,
        String errorMessage
) {
    public static AttributeAllocationResult success(CharacterAttribute attribute, int amountAllocated,
                                                     int remainingPoints, int totalAllocated) {
        return new AttributeAllocationResult(true, attribute, amountAllocated, remainingPoints, totalAllocated, null);
    }

    public static AttributeAllocationResult failure(String errorMessage) {
        return new AttributeAllocationResult(false, null, 0, 0, 0, errorMessage);
    }
}
