package io.github.bysenom.relicwrought.client;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.client.screen.ClassSelectionClientState;
import io.github.bysenom.relicwrought.client.tooltip.ArpgItemTooltipAppender;
import io.github.bysenom.relicwrought.network.ClassSelectionResponse;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class RelicwroughtClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Relicwrought.LOGGER.info("Relicwrought client systems ready.");
        ArpgItemTooltipAppender.register();
        ClassSelectionClientState.register();
    }
}
