package io.github.bysenom.relicwrought.network;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.player.ClassSelectionManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ClassSelectionNetworking {
    private static final ClassSelectionResponse PROMPT_RESPONSE = new ClassSelectionResponse(false, "prompt", "");

    private ClassSelectionNetworking() {}

    public static void registerC2SPayloads() {
        PayloadTypeRegistry.serverboundPlay().register(ClassSelectionRequest.TYPE, ClassSelectionRequest.STREAM_CODEC);
    }

    public static void registerS2CPayloads() {
        PayloadTypeRegistry.clientboundPlay().register(ClassSelectionResponse.TYPE, ClassSelectionResponse.STREAM_CODEC);
    }

    public static void registerServerHandler(ClassSelectionManager selectionManager) {
        ServerPlayNetworking.registerGlobalReceiver(ClassSelectionRequest.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var result = selectionManager.selectClass(player, payload.classId());

                if (result.success()) {
                    // Initialize ability loadout for this class and sync to client
                    var profile = selectionManager.getProfile(player);
                    var loadout = io.github.bysenom.relicwrought.ability.PlayerAbilityLoadout.defaultsForClass(profile.classId());
                    // Force overwrite any cached empty loadout
                    Relicwrought.setLoadout(player, loadout);
                    Relicwrought.syncAbilityLoadout(player);

                    var response = new ClassSelectionResponse(true,
                            "Class selected: " + payload.classId(), payload.classId());
                    ServerPlayNetworking.send(player, response);
                } else {
                    var response = new ClassSelectionResponse(false, result.message(), payload.classId());
                    ServerPlayNetworking.send(player, response);
                    player.sendSystemMessage(Component.literal("§c" + result.message()));
                }
            });
        });
    }

    public static void sendClassSelectionPrompt(ServerPlayer player) {
        ServerPlayNetworking.send(player, PROMPT_RESPONSE);
    }
}
