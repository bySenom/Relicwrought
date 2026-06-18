package io.github.bysenom.relicwrought.progression;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record CharacterProgression(
        CharacterLevel level,
        long currentLevelXp,
        long totalXp,
        int unspentAttributePoints,
        Map<CharacterAttribute, Integer> allocatedAttributes
) {
    public CharacterProgression {
        if (currentLevelXp < 0) throw new IllegalArgumentException("Current level XP must not be negative: " + currentLevelXp);
        if (totalXp < 0) throw new IllegalArgumentException("Total XP must not be negative: " + totalXp);
        if (unspentAttributePoints < 0) throw new IllegalArgumentException("Unspent attribute points must not be negative: " + unspentAttributePoints);
        if (allocatedAttributes == null) throw new IllegalArgumentException("Allocated attributes must not be null");
        EnumMap<CharacterAttribute, Integer> map = new EnumMap<>(CharacterAttribute.class);
        map.putAll(allocatedAttributes);
        allocatedAttributes = Collections.unmodifiableMap(map);
    }

    public static CharacterProgression createDefault() {
        Map<CharacterAttribute, Integer> attrs = new EnumMap<>(CharacterAttribute.class);
        for (var attr : CharacterAttribute.values()) {
            attrs.put(attr, 0);
        }
        return new CharacterProgression(CharacterLevel.of(1), 0L, 0L, 0, attrs);
    }

    public long totalAllocatedAttributePoints() {
        return allocatedAttributes.values().stream().mapToLong(Integer::longValue).sum();
    }
}
