package io.github.bysenom.relicwrought.ui;

public enum EnemyClassification {
    NORMAL("ui.relicwrought.enemy.normal"),
    ELITE("ui.relicwrought.enemy.elite"),
    BOSS("ui.relicwrought.enemy.boss"),
    WORLD_BOSS("ui.relicwrought.enemy.world_boss");

    private final String translationKey;

    EnemyClassification(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
