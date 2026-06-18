package io.github.bysenom.relicwrought.progression;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record ClassStartingAttributes(
        String classId,
        Map<CharacterAttribute, Integer> baseAttributes
) {
    public ClassStartingAttributes {
        if (classId == null || classId.isBlank()) throw new IllegalArgumentException("classId must not be blank");
        baseAttributes = Collections.unmodifiableMap(new EnumMap<>(baseAttributes));
    }

    public int getAttribute(CharacterAttribute attribute) {
        return baseAttributes.getOrDefault(attribute, 0);
    }

    public static final Map<String, ClassStartingAttributes> DEFAULTS = buildDefaults();

    private static Map<String, ClassStartingAttributes> buildDefaults() {
        Map<String, ClassStartingAttributes> map = new java.util.LinkedHashMap<>();

        map.put("warrior", new ClassStartingAttributes("warrior", Map.of(
                CharacterAttribute.STRENGTH, 10,
                CharacterAttribute.DEXTERITY, 3,
                CharacterAttribute.INTELLIGENCE, 0,
                CharacterAttribute.VITALITY, 8
        )));

        map.put("ranger", new ClassStartingAttributes("ranger", Map.of(
                CharacterAttribute.STRENGTH, 3,
                CharacterAttribute.DEXTERITY, 10,
                CharacterAttribute.INTELLIGENCE, 2,
                CharacterAttribute.VITALITY, 5
        )));

        map.put("arcanist", new ClassStartingAttributes("arcanist", Map.of(
                CharacterAttribute.STRENGTH, 0,
                CharacterAttribute.DEXTERITY, 3,
                CharacterAttribute.INTELLIGENCE, 10,
                CharacterAttribute.VITALITY, 4
        )));

        map.put("rogue", new ClassStartingAttributes("rogue", Map.of(
                CharacterAttribute.STRENGTH, 3,
                CharacterAttribute.DEXTERITY, 9,
                CharacterAttribute.INTELLIGENCE, 3,
                CharacterAttribute.VITALITY, 5
        )));

        return Collections.unmodifiableMap(map);
    }

    public static ClassStartingAttributes forClass(String classId) {
        return DEFAULTS.getOrDefault(classId, new ClassStartingAttributes(classId, Map.of(
                CharacterAttribute.STRENGTH, 5,
                CharacterAttribute.DEXTERITY, 5,
                CharacterAttribute.INTELLIGENCE, 5,
                CharacterAttribute.VITALITY, 5
        )));
    }
}
