package io.github.bysenom.relicwrought.loot;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ItemLevelResolverTest {
    private static final DefinitionKey PROFILE_ID = DefinitionKey.parse("test_profile", Relicwrought.MOD_ID);

    private static LootProfileDefinition profileWithLevel(LootItemLevelConfig levelConfig) {
        return new LootProfileDefinition(
                PROFILE_ID, LootSourceType.NORMAL_MOB, 1.0, 1, 1,
                Set.of(), Set.of(), Set.of(), Map.of(),
                levelConfig, Set.of(), Map.of(), true, 1
        );
    }

    @Test
    void fixedLevelReturnsMinimum() {
        var profile = profileWithLevel(new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.FIXED, 100, 100, 0));
        var context = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 100.0, 20.0, 10.0, true, 0);
        var result = ItemLevelResolver.resolve(profile, context, null, new SplittableRandom(42));
        assertEquals(100, result.value());
    }

    @Test
    void randomLevelWithinRange() {
        var profile = profileWithLevel(new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.RANDOM, 50, 100, 0));
        var context = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 100.0, 20.0, 10.0, true, 0);
        for (long seed = 0; seed < 100; seed++) {
            var result = ItemLevelResolver.resolve(profile, context, null, new SplittableRandom(seed));
            assertTrue(result.value() >= 50 && result.value() <= 100,
                    "Level " + result.value() + " out of range [50,100] for seed " + seed);
        }
    }

    @Test
    void sourceScaledLevelUsesContextStrength() {
        var profile = profileWithLevel(new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 0));
        var context = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 100.0, 20.0, 10.0, true, 0);
        double strength = context.computeBaseStrength();
        int expected = 1 + (int) Math.round(Math.min(1.0, strength / 200.0) * (500 - 1));
        var result = ItemLevelResolver.resolve(profile, context, null, new SplittableRandom(42));
        assertEquals(expected, result.value());
    }

    @Test
    void sourceScaledStrongEntityGetsHigherLevel() {
        var profile = profileWithLevel(new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 500, 0));
        var weak = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 10.0, 2.0, 1.0, true, 0);
        var strong = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 200.0, 50.0, 30.0, true, 0);
        int weakLevel = ItemLevelResolver.resolve(profile, weak, null, new SplittableRandom(42)).value();
        int strongLevel = ItemLevelResolver.resolve(profile, strong, null, new SplittableRandom(42)).value();
        assertTrue(strongLevel >= weakLevel,
                "Strong entity level " + strongLevel + " should be >= weak entity level " + weakLevel);
    }

    @Test
    void dimensionBoundsAffectSourceScaledMinimum() {
        var profile = profileWithLevel(new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 950, 0));
        var overworld = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 10.0, 0.0, 0.0, true, 0);
        var nether = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:nether", "test",
                null, 10.0, 0.0, 0.0, true, 0);
        var end = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:the_end", "test",
                null, 10.0, 0.0, 0.0, true, 0);
        assertTrue(ItemLevelResolver.resolve(profile, overworld, null, new SplittableRandom(42)).value() <= 500);
        assertTrue(ItemLevelResolver.resolve(profile, nether, null, new SplittableRandom(42)).value() >= 250);
        assertTrue(ItemLevelResolver.resolve(profile, end, null, new SplittableRandom(42)).value() >= 500);
    }

    @Test
    void overrideBonusApplied() {
        var profile = profileWithLevel(new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.FIXED, 100, 100, 0));
        var override = new EntityLootOverride(null, 20, 0.0, 0);
        var context = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 100.0, 20.0, 10.0, true, 0);
        var result = ItemLevelResolver.resolve(profile, context, override, new SplittableRandom(42));
        assertEquals(100, result.value());
    }

    @Test
    void varianceProducesDifferentResults() {
        var profile = profileWithLevel(new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.FIXED, 100, 100, 10));
        var context = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 100.0, 0.0, 0.0, true, 0);
        int result1 = ItemLevelResolver.resolve(profile, context, null, new SplittableRandom(1)).value();
        int result2 = ItemLevelResolver.resolve(profile, context, null, new SplittableRandom(2)).value();
        assertEquals(100, result1);
        assertTrue(result2 >= 90 && result2 <= 100,
                "Level " + result2 + " out of expected range [90,100]");
    }

    @Test
    void resultNeverExceeds950() {
        var profile = profileWithLevel(new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.SOURCE_SCALED, 1, 950, 100));
        var context = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 1000.0, 200.0, 100.0, true, 0);
        for (long seed = 0; seed < 50; seed++) {
            var result = ItemLevelResolver.resolve(profile, context, null, new SplittableRandom(seed));
            assertTrue(result.value() <= 950,
                    "Level " + result.value() + " exceeds 950 for seed " + seed);
        }
    }

    @Test
    void resultNeverBelow1() {
        var profile = profileWithLevel(new LootItemLevelConfig(
                LootItemLevelConfig.LootItemLevelType.FIXED, 1, 1, 100));
        var context = new LootContextData(
                LootSourceType.NORMAL_MOB, "minecraft:overworld", "test",
                null, 0.0, 0.0, 0.0, true, 0);
        for (long seed = 0; seed < 50; seed++) {
            var result = ItemLevelResolver.resolve(profile, context, null, new SplittableRandom(seed));
            assertTrue(result.value() >= 1,
                    "Level " + result.value() + " is below 1 for seed " + seed);
        }
    }
}
