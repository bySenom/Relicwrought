package io.github.bysenom.relicwrought.client.screen;

import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStats;
import io.github.bysenom.relicwrought.progression.CharacterAttribute;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class CharacterScreenModel {
    private int level = 1;
    private long currentXp = 0;
    private long xpForNextLevel = 0;
    private long totalXp = 0;
    private int unspentPoints = 0;
    private Map<CharacterAttribute, Integer> allocatedAttributes = Map.of();
    private Map<CharacterAttribute, Integer> totalAttributes = Map.of();
    
    private CharacterCombatStats currentStats = CharacterCombatStats.empty();
    
    public void updateProgression(int level, long currentXp, long xpForNextLevel, long totalXp,
                                  int unspentPoints, Map<CharacterAttribute, Integer> allocated,
                                  Map<CharacterAttribute, Integer> total) {
        this.level = level;
        this.currentXp = currentXp;
        this.xpForNextLevel = xpForNextLevel;
        this.totalXp = totalXp;
        this.unspentPoints = unspentPoints;
        this.allocatedAttributes = allocated;
        this.totalAttributes = total;
    }
    
    public void updateStats(CharacterCombatStats stats) {
        this.currentStats = stats;
    }

    public int getLevel() { return level; }
    public long getCurrentXp() { return currentXp; }
    public long getXpForNextLevel() { return xpForNextLevel; }
    public long getTotalXp() { return totalXp; }
    public int getUnspentPoints() { return unspentPoints; }
    public Map<CharacterAttribute, Integer> getAllocatedAttributes() { return allocatedAttributes; }
    public Map<CharacterAttribute, Integer> getTotalAttributes() { return totalAttributes; }
    public CharacterCombatStats getCurrentStats() { return currentStats; }
}
