package io.github.bysenom.relicwrought.ui;

public record EnemyNameplateModel(
        boolean visible,
        String title,
        String healthText,
        double healthFill,
        boolean healthBarVisible,
        boolean healthNumbersVisible
) {
    public static EnemyNameplateModel hidden() {
        return new EnemyNameplateModel(false, "", "", 0.0, false, false);
    }

    public static EnemyNameplateModel fromSnapshot(
            EnemyUiSnapshot snapshot,
            boolean nameplatesEnabled,
            boolean showHealthBar,
            boolean showHealthNumbers
    ) {
        if (!nameplatesEnabled || snapshot == null || !snapshot.hostile()) {
            return hidden();
        }
        if (!isFinitePositive(snapshot.maximumHealth()) || !isFinite(snapshot.currentHealth()) || snapshot.currentHealth() <= 0.0) {
            return hidden();
        }
        String name = snapshot.displayName() == null || snapshot.displayName().isBlank()
                ? "Enemy"
                : snapshot.displayName();
        String levelText = snapshot.level() > 0 ? String.valueOf(snapshot.level()) : "?";
        double current = clamp(snapshot.currentHealth(), 0.0, snapshot.maximumHealth());
        double fill = clamp01(current / snapshot.maximumHealth());
        return new EnemyNameplateModel(
                true,
                "Lv. " + levelText + " " + name,
                (int) current + " / " + (int) snapshot.maximumHealth(),
                fill,
                showHealthBar,
                showHealthNumbers
        );
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private static boolean isFinitePositive(double value) {
        return isFinite(value) && value > 0.0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp01(double value) {
        if (!isFinite(value)) {
            return 0.0;
        }
        return clamp(value, 0.0, 1.0);
    }
}
