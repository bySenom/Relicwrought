package io.github.bysenom.relicwrought.combat.animation;

public enum AnimationProfile {
    LIGHT(0.8),
    MEDIUM(1.0),
    HEAVY(1.2);

    private final double swingDurationRatio;

    AnimationProfile(double swingDurationRatio) {
        this.swingDurationRatio = swingDurationRatio;
    }

    public double getSwingDurationRatio() {
        return swingDurationRatio;
    }
}
