package io.github.bysenom.relicwrought.progression;

import java.util.List;

public record ExperienceGrantResult(
        long xpBefore,
        long xpGranted,
        long xpAfter,
        CharacterLevel levelBefore,
        CharacterLevel levelAfter,
        int levelUps,
        int newAttributePoints,
        boolean maxLevelReached,
        List<String> warnings,
        ExperienceGrantError error
) {
    public ExperienceGrantResult {
        warnings = List.copyOf(warnings);
    }

    public boolean success() {
        return error == null || error == ExperienceGrantError.NONE;
    }

    public static ExperienceGrantResult success(long xpBefore, long xpGranted, long xpAfter,
                                                  CharacterLevel levelBefore, CharacterLevel levelAfter,
                                                  int levelUps, int newAttributePoints, boolean maxLevelReached) {
        return new ExperienceGrantResult(xpBefore, xpGranted, xpAfter, levelBefore, levelAfter,
                levelUps, newAttributePoints, maxLevelReached, List.of(), ExperienceGrantError.NONE);
    }

    public static ExperienceGrantResult failure(ExperienceGrantError error, String warning) {
        return new ExperienceGrantResult(0, 0, 0, CharacterLevel.of(1), CharacterLevel.of(1),
                0, 0, false, List.of(warning), error);
    }

    public enum ExperienceGrantError {
        NONE,
        PROGRESSION_DISABLED,
        INVALID_XP_AMOUNT,
        MAX_LEVEL_REACHED,
        UNKNOWN_PROGRESSION_PROFILE,
        PLAYER_PROFILE_MISSING,
        PROFILE_WRITE_FAILED,
        UNSUPPORTED_PROFILE_VERSION,
        NO_VALID_KILLER,
        NOT_A_HOSTILE_ENTITY
    }
}
