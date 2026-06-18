package io.github.bysenom.relicwrought.combat.cooldown;

public class WeaponAttackState {
    private long lastAcceptedAttackTick;
    private int cooldownDurationTicks;
    private double currentAttackSpeed;
    private java.util.UUID lastWeaponUuid;

    public WeaponAttackState() {
        this.lastAcceptedAttackTick = 0;
        this.cooldownDurationTicks = 0;
        this.currentAttackSpeed = 1.0;
        this.lastWeaponUuid = null;
    }

    public void update(long currentTick, int newCooldownDuration, double newAttackSpeed) {
        this.cooldownDurationTicks = newCooldownDuration;
        this.currentAttackSpeed = newAttackSpeed;
    }

    public void recordAttack(long currentTick, int cooldownDuration, double attackSpeed) {
        this.lastAcceptedAttackTick = currentTick;
        this.cooldownDurationTicks = cooldownDuration;
        this.currentAttackSpeed = attackSpeed;
    }

    public void resetCooldown(long currentTick) {
        this.lastAcceptedAttackTick = currentTick;
    }
    
    public boolean checkWeaponSwap(long currentTick, java.util.UUID newWeaponUuid) {
        if (newWeaponUuid == null && lastWeaponUuid == null) return false;
        if (newWeaponUuid != null && newWeaponUuid.equals(lastWeaponUuid)) return false;
        
        this.lastWeaponUuid = newWeaponUuid;
        return true;
    }
    
    public void setLastAcceptedAttackTick(long tick) {
        this.lastAcceptedAttackTick = tick;
    }

    public double getProgress(long currentTick) {
        if (cooldownDurationTicks <= 0) return 1.0;
        long elapsed = currentTick - lastAcceptedAttackTick;
        if (elapsed < 0) elapsed = 0; // Guard against time skips or mismatched ticks
        double progress = (double) elapsed / cooldownDurationTicks;
        return Math.min(progress, 1.0);
    }

    public boolean isReady(long currentTick) {
        if (cooldownDurationTicks <= 0) return true;
        return (currentTick - lastAcceptedAttackTick) >= cooldownDurationTicks;
    }

    public long getLastAcceptedAttackTick() { return lastAcceptedAttackTick; }
    public int getCooldownDurationTicks() { return cooldownDurationTicks; }
    public double getCurrentAttackSpeed() { return currentAttackSpeed; }
}
