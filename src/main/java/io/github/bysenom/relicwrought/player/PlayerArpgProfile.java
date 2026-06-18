package io.github.bysenom.relicwrought.player;

import io.github.bysenom.relicwrought.progression.CharacterAttribute;
import io.github.bysenom.relicwrought.progression.CharacterLevel;
import io.github.bysenom.relicwrought.progression.CharacterProgression;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record PlayerArpgProfile(
        int dataVersion,
        boolean classSelected,
        String classId,
        boolean starterKitGranted,
        String starterKitId,
        int starterKitVersion,
        long selectionTimestamp,
        int characterLevel,
        long currentLevelXp,
        long totalXp,
        int unspentAttributePoints,
        Map<CharacterAttribute, Integer> allocatedAttributes
) {
    public static final int CURRENT_VERSION = 2;

    public static PlayerArpgProfile empty() {
        return new PlayerArpgProfile(CURRENT_VERSION, false, "", false, "", 0, 0,
                CharacterLevel.MIN, 0L, 0L, 0, emptyAttributes());
    }

    public PlayerArpgProfile withClassSelected(String classId, long timestamp) {
        return new PlayerArpgProfile(
                CURRENT_VERSION, true, classId,
                starterKitGranted, starterKitId, starterKitVersion, timestamp,
                characterLevel, currentLevelXp, totalXp, unspentAttributePoints, allocatedAttributes
        );
    }

    public PlayerArpgProfile withKitGranted(String kitId, int kitVersion) {
        return new PlayerArpgProfile(
                CURRENT_VERSION, classSelected, classId,
                true, kitId, kitVersion, selectionTimestamp,
                characterLevel, currentLevelXp, totalXp, unspentAttributePoints, allocatedAttributes
        );
    }

    public PlayerArpgProfile withProgression(int level, long levelXp, long totalXp, int unspentPoints,
                                              Map<CharacterAttribute, Integer> allocated) {
        return new PlayerArpgProfile(
                CURRENT_VERSION, classSelected, classId,
                starterKitGranted, starterKitId, starterKitVersion, selectionTimestamp,
                level, levelXp, totalXp, unspentPoints, allocated
        );
    }

    public CharacterProgression toCharacterProgression() {
        return new CharacterProgression(
                CharacterLevel.clamp(characterLevel),
                Math.max(0, currentLevelXp),
                Math.max(0, totalXp),
                Math.max(0, unspentAttributePoints),
                allocatedAttributes != null ? allocatedAttributes : emptyAttributes()
        );
    }

    public static PlayerArpgProfile fromCharacterProgression(PlayerArpgProfile base, CharacterProgression prog) {
        Map<CharacterAttribute, Integer> attrs = new EnumMap<>(CharacterAttribute.class);
        if (prog.allocatedAttributes() != null) {
            attrs.putAll(prog.allocatedAttributes());
        }
        return new PlayerArpgProfile(
                CURRENT_VERSION, base.classSelected(), base.classId(),
                base.starterKitGranted(), base.starterKitId(), base.starterKitVersion(),
                base.selectionTimestamp(),
                prog.level().value(), prog.currentLevelXp(), prog.totalXp(),
                prog.unspentAttributePoints(), attrs
        );
    }

    public static Map<CharacterAttribute, Integer> emptyAttributes() {
        Map<CharacterAttribute, Integer> map = new EnumMap<>(CharacterAttribute.class);
        for (var attr : CharacterAttribute.values()) {
            map.put(attr, 0);
        }
        return Collections.unmodifiableMap(map);
    }

    public static PlayerArpgProfile legacyV1ToV2(PlayerArpgProfile v1) {
        return new PlayerArpgProfile(
                CURRENT_VERSION,
                v1.classSelected(), v1.classId(),
                v1.starterKitGranted(), v1.starterKitId(), v1.starterKitVersion(),
                v1.selectionTimestamp(),
                CharacterLevel.MIN, 0L, 0L, 0, emptyAttributes()
        );
    }
}
