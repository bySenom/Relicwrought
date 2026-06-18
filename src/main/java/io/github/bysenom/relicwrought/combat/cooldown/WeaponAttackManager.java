package io.github.bysenom.relicwrought.combat.cooldown;

import net.minecraft.world.entity.player.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WeaponAttackManager {
    private final Map<UUID, WeaponAttackState> states = new ConcurrentHashMap<>();

    public WeaponAttackState getState(Player player) {
        return states.computeIfAbsent(player.getUUID(), k -> new WeaponAttackState());
    }

    public void removeState(Player player) {
        states.remove(player.getUUID());
    }
    
    public void clear() {
        states.clear();
    }
}
