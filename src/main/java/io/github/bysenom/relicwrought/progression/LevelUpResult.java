package io.github.bysenom.relicwrought.progression;

public record LevelUpResult(
        boolean leveledUp,
        CharacterLevel oldLevel,
        CharacterLevel newLevel,
        int levelsGained,
        int attributePointsGained,
        long xpRemainingInLevel,
        boolean maxLevelReached
) {
    public static LevelUpResult noChange(CharacterLevel currentLevel, long xpRemainingInLevel) {
        return new LevelUpResult(false, currentLevel, currentLevel, 0, 0, xpRemainingInLevel, currentLevel.isMax());
    }

    public static LevelUpResult leveledUp(CharacterLevel oldLevel, CharacterLevel newLevel,
                                           int levelsGained, int attributePointsGained,
                                           long xpRemainingInLevel, boolean maxLevelReached) {
        return new LevelUpResult(true, oldLevel, newLevel, levelsGained, attributePointsGained,
                xpRemainingInLevel, maxLevelReached);
    }
}
