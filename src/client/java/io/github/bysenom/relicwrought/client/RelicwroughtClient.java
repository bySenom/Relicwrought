package io.github.bysenom.relicwrought.client;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.client.tooltip.ArpgItemTooltipAppender;
import net.fabricmc.api.ClientModInitializer;

public final class RelicwroughtClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Relicwrought.LOGGER.info("Relicwrought client systems ready.");
        ArpgItemTooltipAppender.register();
    }
}
