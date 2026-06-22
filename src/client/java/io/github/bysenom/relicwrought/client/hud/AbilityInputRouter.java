package io.github.bysenom.relicwrought.client.hud;

import io.github.bysenom.relicwrought.network.AbilitySlotInputPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class AbilityInputRouter {
    private static long sequenceIdCounter = 0;

    public static boolean handleHotbarKey(int slotIndex) {
        if (AbilityHotbarState.getCurrentMode() == HotbarMode.ABILITY) {
            // We are in ability mode, send payload and return true to cancel vanilla item swap
            long sequenceId = sequenceIdCounter++;
            ClientPlayNetworking.send(new AbilitySlotInputPayload(slotIndex, true, sequenceId));
            return true;
        }
        return false;
    }

    public static boolean shouldBlockHotbarScroll(HotbarMode mode, boolean isScreenOpen, boolean isPlayerPresent) {
        if (!isPlayerPresent) return false;
        if (isScreenOpen) return false;
        return mode == HotbarMode.ABILITY;
    }
}
