package io.github.bysenom.relicwrought.client.enemy;

import io.github.bysenom.relicwrought.ui.EnemyUiSnapshot;

public class EnemyUiTracker {
    private static EnemyUiSnapshot currentTarget = null;
    private static long lastUpdateTime = 0;

    public static void updateSnapshot(EnemyUiSnapshot snapshot, long gameTime) {
        currentTarget = snapshot;
        lastUpdateTime = gameTime;
    }

    public static EnemyUiSnapshot getCurrentTarget() {
        return currentTarget;
    }

    public static boolean hasValidTarget(long currentGameTime) {
        // Expire after 5 seconds (100 ticks)
        return currentTarget != null && (currentGameTime - lastUpdateTime < 100);
    }

    public static int getSnapshotCount(long currentGameTime) {
        return hasValidTarget(currentGameTime) ? 1 : 0;
    }
}
