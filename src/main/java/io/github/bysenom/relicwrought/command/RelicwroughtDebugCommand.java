package io.github.bysenom.relicwrought.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.combat.damage.CombatTextEvent;
import io.github.bysenom.relicwrought.network.FloatingDamageNumberPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class RelicwroughtDebugCommand {
    private RelicwroughtDebugCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("relicwrought")
                .then(Commands.literal("equipment")
                        .then(Commands.literal("open")
                                .executes(context -> openEquipment(context.getSource()))))
                .then(Commands.literal("debug")
                        .then(Commands.literal("damage_number")
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01, 1_000_000_000.0))
                                        .executes(context -> sendDamageNumber(
                                                context.getSource(),
                                                DoubleArgumentType.getDouble(context, "amount")
                                        ))))));
    }

    private static int openEquipment(CommandSourceStack source) {
        if (!source.isPlayer()) {
            source.sendFailure(Component.translatable("command.relicwrought.equipment.error.not_player"));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        if (player == null || !Relicwrought.openEquipmentScreen(player)) {
            source.sendFailure(Component.translatable("command.relicwrought.equipment.open_failed"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.relicwrought.equipment.opened"), false);
        return 1;
    }

    private static int sendDamageNumber(CommandSourceStack source, double amount) {
        if (!source.isPlayer()) {
            source.sendFailure(Component.translatable("command.relicwrought.debug.error.not_player"));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        if (player == null || Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0.0) {
            source.sendFailure(Component.translatable("command.relicwrought.debug.damage_number.invalid"));
            return 0;
        }

        CombatTextEvent event = new CombatTextEvent(
                player.getId(),
                player.getUUID(),
                player.getUUID(),
                amount,
                false,
                "physical",
                player.level().getRandom().nextLong(),
                player.level().getGameTime()
        );
        ServerPlayNetworking.send(player, new FloatingDamageNumberPayload(event));
        source.sendSuccess(() -> Component.translatable("command.relicwrought.debug.damage_number.sent", amount), false);
        return 1;
    }
}
