package io.github.bysenom.relicwrought.progression;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ExperienceRewardService {
    public static final int ATTRIBUTE_POINTS_PER_LEVEL = 1;

    private final ExperienceCurve curve;
    private final int maxLevel;

    public ExperienceRewardService(ExperienceCurve curve) {
        this(curve, CharacterLevel.MAX);
    }

    public ExperienceRewardService(ExperienceCurve curve, int maxLevel) {
        if (curve == null) throw new IllegalArgumentException("curve must not be null");
        if (maxLevel < CharacterLevel.MIN || maxLevel > CharacterLevel.MAX) {
            throw new IllegalArgumentException("maxLevel must be between " + CharacterLevel.MIN + " and " + CharacterLevel.MAX);
        }
        this.curve = curve;
        this.maxLevel = maxLevel;
    }

    public ExperienceGrantResult grantXp(CharacterProgression progression, long rawXp) {
        if (progression == null) {
            return ExperienceGrantResult.failure(ExperienceGrantResult.ExperienceGrantError.PLAYER_PROFILE_MISSING, "Progression is null");
        }
        if (rawXp < 0) {
            return ExperienceGrantResult.failure(ExperienceGrantResult.ExperienceGrantError.INVALID_XP_AMOUNT, "XP amount must not be negative: " + rawXp);
        }
        if (rawXp == 0) {
            return ExperienceGrantResult.success(
                    progression.totalXp(), 0, progression.totalXp(),
                    progression.level(), progression.level(), 0, 0, progression.level().isMax()
            );
        }

        long xpBefore = progression.totalXp();
        CharacterLevel levelBefore = progression.level();

        if (levelBefore.value() >= maxLevel) {
            return ExperienceGrantResult.failure(ExperienceGrantResult.ExperienceGrantError.MAX_LEVEL_REACHED, "Player is already at max level");
        }

        long newTotalXp = xpBefore + rawXp;
        if (newTotalXp < xpBefore) {
            newTotalXp = Long.MAX_VALUE;
        }

        LevelUpResult levelUpResult = processLevelUps(levelBefore, newTotalXp, progression.currentLevelXp());
        CharacterLevel levelAfter = levelUpResult.newLevel();
        int levelUps = levelUpResult.levelsGained();
        int newAttributePoints = levelUps * ATTRIBUTE_POINTS_PER_LEVEL;
        boolean maxLevelReached = levelAfter.value() >= maxLevel;

        return ExperienceGrantResult.success(
                xpBefore, rawXp, newTotalXp,
                levelBefore, levelAfter, levelUps,
                newAttributePoints, maxLevelReached
        );
    }

    public LevelUpResult processLevelUps(CharacterLevel currentLevel, long newTotalXp, long currentLevelXp) {
        if (currentLevel.isMax() || currentLevel.value() >= maxLevel) {
            return LevelUpResult.noChange(currentLevel, currentLevelXp);
        }

        int levelsGained = 0;
        int attributePoints = 0;
        CharacterLevel workingLevel = currentLevel;

        long xpRemaining = newTotalXp - curve.totalXpForLevel(workingLevel.value());

        while (xpRemaining >= 0 && workingLevel.value() < maxLevel) {
            long xpForNext = curve.xpFromPreviousToCurrent(workingLevel.value() + 1);
            if (xpRemaining < xpForNext) break;

            xpRemaining -= xpForNext;
            workingLevel = workingLevel.next();
            levelsGained++;
            attributePoints += ATTRIBUTE_POINTS_PER_LEVEL;

            if (workingLevel.isMax() || workingLevel.value() >= maxLevel) {
                break;
            }
        }

        if (levelsGained == 0) {
            return LevelUpResult.noChange(currentLevel, newTotalXp - curve.totalXpForLevel(currentLevel.value()));
        }

        return LevelUpResult.leveledUp(currentLevel, workingLevel, levelsGained, attributePoints, xpRemaining, workingLevel.isMax());
    }

    public CharacterProgression applyGrant(CharacterProgression progression, ExperienceGrantResult grantResult) {
        if (!grantResult.success()) return progression;

        CharacterLevel newLevel = grantResult.levelAfter();
        long newTotalXp = grantResult.xpAfter();
        long currentLevelXp = newTotalXp - curve.totalXpForLevel(newLevel.value());
        if (currentLevelXp < 0) currentLevelXp = 0;

        int newUnspentPoints = progression.unspentAttributePoints() + grantResult.newAttributePoints();

        Map<CharacterAttribute, Integer> allocated = new EnumMap<>(progression.allocatedAttributes());

        return new CharacterProgression(newLevel, currentLevelXp, newTotalXp, newUnspentPoints, allocated);
    }

    public AttributeAllocationResult allocateAttribute(CharacterProgression progression, CharacterAttribute attribute, int amount) {
        if (progression == null) {
            return AttributeAllocationResult.failure("Progression is null");
        }
        if (attribute == null) {
            return AttributeAllocationResult.failure("Attribute must not be null");
        }
        if (amount <= 0) {
            return AttributeAllocationResult.failure("Allocation amount must be positive: " + amount);
        }
        if (progression.unspentAttributePoints() < amount) {
            return AttributeAllocationResult.failure("Insufficient attribute points: have " + progression.unspentAttributePoints() + ", need " + amount);
        }

        int currentAllocated = progression.allocatedAttributes().getOrDefault(attribute, 0);
        int newAllocated = currentAllocated + amount;

        if (newAllocated < currentAllocated) {
            return AttributeAllocationResult.failure("Integer overflow for attribute allocation");
        }

        return AttributeAllocationResult.success(attribute, amount,
                progression.unspentAttributePoints() - amount, newAllocated);
    }

    public ExperienceCurve curve() { return curve; }
    public int maxLevel() { return maxLevel; }
}
