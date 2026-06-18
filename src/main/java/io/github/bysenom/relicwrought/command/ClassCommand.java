package io.github.bysenom.relicwrought.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.network.ClassSelectionResponse;
import io.github.bysenom.relicwrought.player.ClassSelectionManager;
import io.github.bysenom.relicwrought.player.PlayerArpgProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public final class ClassCommand {
    private static final SimpleCommandExceptionType ERROR_NO_PLAYER = new SimpleCommandExceptionType(
            Component.translatable("command.relicwrought.class.error.no_player")
    );
    private static final SimpleCommandExceptionType ERROR_ALREADY_SELECTED = new SimpleCommandExceptionType(
            Component.translatable("command.relicwrought.class.error.already_selected")
    );

    private ClassCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, ClassSelectionManager selectionManager) {
        dispatcher.register(Commands.literal("class")
                .then(Commands.literal("choose")
                        .then(Commands.argument("class", StringArgumentType.word())
                                .executes(ctx -> chooseClass(ctx.getSource(), selectionManager,
                                        StringArgumentType.getString(ctx, "class")))
                        )
                )
                .then(Commands.literal("inspect")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(ctx -> inspectSelf(ctx.getSource(), selectionManager))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .executes(ctx -> inspectPlayer(ctx.getSource(), selectionManager,
                                        StringArgumentType.getString(ctx, "player")))
                        )
                )
                .then(Commands.literal("reset")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(ctx -> resetPlayer(ctx.getSource(), selectionManager,
                                        StringArgumentType.getString(ctx, "player")))
                        )
                )
                .then(Commands.literal("grantkit")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(ctx -> grantKit(ctx.getSource(), selectionManager,
                                        StringArgumentType.getString(ctx, "player")))
                        )
                )
        );
    }

    private static int chooseClass(CommandSourceStack source, ClassSelectionManager manager, String classId) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            if (manager.hasSelectedClass(player)) {
                source.sendFailure(Component.translatable("command.relicwrought.class.error.already_selected"));
                return 0;
            }
            var result = manager.selectClass(player, classId);
            if (result.success()) {
                source.sendSuccess(() -> Component.translatable("command.relicwrought.class.selected", classId), true);
                var response = new ClassSelectionResponse(true, "Class selected: " + classId, classId);
                ServerPlayNetworking.send(player, response);
            } else {
                source.sendFailure(Component.literal(result.message()));
            }
            return result.success() ? 1 : 0;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int inspectSelf(CommandSourceStack source, ClassSelectionManager manager) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            PlayerArpgProfile profile = manager.getProfile(player);
            source.sendSuccess(() -> Component.literal("=== Player Class Profile ==="), false);
            source.sendSuccess(() -> Component.literal("Class selected: " + profile.classSelected()), false);
            source.sendSuccess(() -> Component.literal("Class ID: " + (profile.classId().isEmpty() ? "none" : profile.classId())), false);
            source.sendSuccess(() -> Component.literal("Kit granted: " + profile.starterKitGranted()), false);
            source.sendSuccess(() -> Component.literal("Kit ID: " + (profile.starterKitId().isEmpty() ? "none" : profile.starterKitId())), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int inspectPlayer(CommandSourceStack source, ClassSelectionManager manager, String playerName) {
        var server = source.getServer();
        var player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            source.sendFailure(Component.translatable("command.relicwrought.class.error.player_not_found", playerName));
            return 0;
        }
        PlayerArpgProfile profile = manager.getProfile(player);
        source.sendSuccess(() -> Component.literal("=== Class Profile for " + playerName + " ==="), false);
        source.sendSuccess(() -> Component.literal("Class selected: " + profile.classSelected()), false);
        source.sendSuccess(() -> Component.literal("Class ID: " + (profile.classId().isEmpty() ? "none" : profile.classId())), false);
        source.sendSuccess(() -> Component.literal("Kit granted: " + profile.starterKitGranted()), false);
        source.sendSuccess(() -> Component.literal("Kit ID: " + (profile.starterKitId().isEmpty() ? "none" : profile.starterKitId())), false);
        return 1;
    }

    private static int resetPlayer(CommandSourceStack source, ClassSelectionManager manager, String playerName) {
        var server = source.getServer();
        var player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            source.sendFailure(Component.translatable("command.relicwrought.class.error.player_not_found", playerName));
            return 0;
        }
        manager.resetPlayer(player);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.class.reset", playerName), true);
        return 1;
    }

    private static int grantKit(CommandSourceStack source, ClassSelectionManager manager, String playerName) {
        var server = source.getServer();
        var player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            source.sendFailure(Component.translatable("command.relicwrought.class.error.player_not_found", playerName));
            return 0;
        }
        var profile = manager.getProfile(player);
        if (!profile.classSelected() || profile.classId().isEmpty()) {
            source.sendFailure(Component.literal("Player has not selected a class yet"));
            return 0;
        }
        var result = manager.selectClass(player, profile.classId());
        if (result.success()) {
            source.sendSuccess(() -> Component.translatable("command.relicwrought.class.kit_granted", playerName), true);
        } else {
            source.sendFailure(Component.literal(result.message()));
        }
        return result.success() ? 1 : 0;
    }
}
