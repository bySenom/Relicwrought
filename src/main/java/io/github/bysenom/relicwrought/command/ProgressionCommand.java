package io.github.bysenom.relicwrought.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.player.PlayerArpgProfile;
import io.github.bysenom.relicwrought.progression.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Map;

public final class ProgressionCommand {
    private ProgressionCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, ProgressionManager manager, ArpgModConfig config) {
        dispatcher.register(Commands.literal("relicwrought")
                .then(Commands.literal("stats")
                        .executes(ctx -> statsSelf(ctx.getSource(), manager))
                )
                .then(Commands.literal("xp")
                        .then(Commands.literal("give")
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) && config.allowAdminLevelCommands())
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> xpGive(ctx.getSource(), manager, config,
                                                        StringArgumentType.getString(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "amount")))
                                        )
                                )
                        )
                )
                .then(Commands.literal("level")
                        .then(Commands.literal("inspect")
                                .executes(ctx -> levelInspectSelf(ctx.getSource(), manager))
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) && config.allowAdminLevelCommands())
                                        .executes(ctx -> levelInspectPlayer(ctx.getSource(), manager,
                                                StringArgumentType.getString(ctx, "player")))
                                )
                        )
                        .then(Commands.literal("set")
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) && config.allowAdminLevelCommands())
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .then(Commands.argument("level", IntegerArgumentType.integer(CharacterLevel.MIN, CharacterLevel.MAX))
                                                .executes(ctx -> levelSet(ctx.getSource(), manager,
                                                        StringArgumentType.getString(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "level")))
                                        )
                                )
                        )
                )
                .then(Commands.literal("attribute")
                        .then(Commands.literal("add")
                                .then(Commands.argument("attribute", StringArgumentType.word())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                                .executes(ctx -> attributeAdd(ctx.getSource(), manager,
                                                        StringArgumentType.getString(ctx, "attribute"),
                                                        IntegerArgumentType.getInteger(ctx, "amount")))
                                        )
                                )
                        )
                )
        );
    }

    private static int statsSelf(CommandSourceStack source, ProgressionManager manager) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            CharacterProgression prog = manager.getProgression(player);
            Map<CharacterAttribute, Integer> totalAttrs = manager.getTotalAttributes(player);

            source.sendSuccess(() -> Component.literal("=== Character Stats ==="), false);
            source.sendSuccess(() -> Component.translatable("command.relicwrought.progression.level", prog.level().value()), false);
            source.sendSuccess(() -> Component.translatable("command.relicwrought.progression.xp",
                    prog.currentLevelXp(), manager.getXpForNextLevel(player)), false);
            source.sendSuccess(() -> Component.translatable("command.relicwrought.progression.total_xp", prog.totalXp()), false);
            source.sendSuccess(() -> Component.translatable("command.relicwrought.progression.attribute_points", prog.unspentAttributePoints()), false);

            PlayerArpgProfile profile = manager.getProfile(player);
            ClassStartingAttributes base = ClassStartingAttributes.forClass(profile.classId());

            for (var attr : CharacterAttribute.values()) {
                int baseVal = base.getAttribute(attr);
                int allocated = prog.allocatedAttributes().getOrDefault(attr, 0);
                int total = totalAttrs.getOrDefault(attr, 0);
                source.sendSuccess(() -> Component.translatable(
                        "command.relicwrought.progression.attribute_line",
                        Component.translatable("stat.relicwrought." + attr.name().toLowerCase()),
                        baseVal, allocated, total
                ), false);
            }

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int xpGive(CommandSourceStack source, ProgressionManager manager, ArpgModConfig config, String playerName, int amount) {
        var server = source.getServer();
        var player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            source.sendFailure(Component.translatable("command.relicwrought.class.error.player_not_found", playerName));
            return 0;
        }

        if (!config.enableCharacterProgression()) {
            source.sendFailure(Component.translatable("command.relicwrought.progression.disabled"));
            return 0;
        }

        ExperienceGrantResult result = manager.grantXp(player, amount);
        if (result.success()) {
            source.sendSuccess(() -> Component.translatable("command.relicwrought.progression.xp_granted",
                    amount, playerName, result.levelAfter().value()), true);
            if (result.levelUps() > 0) {
                player.sendSystemMessage(Component.translatable("command.relicwrought.progression.level_up",
                        result.levelAfter().value(), result.newAttributePoints()));
            }
        } else {
            source.sendFailure(Component.literal(result.warnings().isEmpty() ? "Failed" : result.warnings().get(0)));
        }
        return result.success() ? 1 : 0;
    }

    private static int levelInspectSelf(CommandSourceStack source, ProgressionManager manager) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            CharacterProgression prog = manager.getProgression(player);
            source.sendSuccess(() -> Component.translatable("command.relicwrought.progression.level", prog.level().value()), false);
            source.sendSuccess(() -> Component.translatable("command.relicwrought.progression.xp",
                    prog.currentLevelXp(), manager.getXpForNextLevel(player)), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int levelInspectPlayer(CommandSourceStack source, ProgressionManager manager, String playerName) {
        var server = source.getServer();
        var player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            source.sendFailure(Component.translatable("command.relicwrought.class.error.player_not_found", playerName));
            return 0;
        }
        CharacterProgression prog = manager.getProgression(player);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.progression.level", prog.level().value()), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.progression.xp",
                prog.currentLevelXp(), manager.getXpForNextLevel(player)), false);
        return 1;
    }

    private static int levelSet(CommandSourceStack source, ProgressionManager manager, String playerName, int newLevel) {
        var server = source.getServer();
        var player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            source.sendFailure(Component.translatable("command.relicwrought.class.error.player_not_found", playerName));
            return 0;
        }

        CharacterLevel clampedLevel = CharacterLevel.clamp(newLevel);
        ExperienceCurve curve = manager.curve();
        long totalXpForLevel = curve.totalXpForLevel(clampedLevel.value());
        long currentLevelXp = 0;

        int unspentPoints = (clampedLevel.value() - 1) * ExperienceRewardService.ATTRIBUTE_POINTS_PER_LEVEL;

        var prog = manager.getProgression(player);
        var updatedProg = new CharacterProgression(clampedLevel, currentLevelXp, totalXpForLevel, unspentPoints, prog.allocatedAttributes());
        var profile = manager.getProfile(player);
        var updatedProfile = PlayerArpgProfile.fromCharacterProgression(profile, updatedProg);
        manager.saveProfileDirect(player, updatedProfile);

        source.sendSuccess(() -> Component.translatable("command.relicwrought.progression.level_set", playerName, clampedLevel.value()), true);
        return 1;
    }

    private static int attributeAdd(CommandSourceStack source, ProgressionManager manager, String attrName, int amount) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            CharacterAttribute attribute;
            try {
                attribute = CharacterAttribute.valueOf(attrName.toUpperCase());
            } catch (IllegalArgumentException e) {
                source.sendFailure(Component.translatable("command.relicwrought.progression.invalid_attribute", attrName));
                return 0;
            }

            AttributeAllocationResult result = manager.allocateAttribute(player, attribute, amount);
            if (result.success()) {
                source.sendSuccess(() -> Component.translatable("command.relicwrought.progression.attribute_added",
                        amount, Component.translatable("stat.relicwrought." + attribute.name().toLowerCase()),
                        result.totalAllocated(), result.remainingPoints()), false);
            } else {
                source.sendFailure(Component.literal(result.errorMessage()));
            }
            return result.success() ? 1 : 0;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
