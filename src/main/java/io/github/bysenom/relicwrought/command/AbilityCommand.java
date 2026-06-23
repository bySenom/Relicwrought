package io.github.bysenom.relicwrought.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.ability.AbilityDefinition;
import io.github.bysenom.relicwrought.ability.AbilityRegistry;
import io.github.bysenom.relicwrought.ability.PlayerAbilityCooldowns;
import io.github.bysenom.relicwrought.ability.PlayerAbilityLoadout;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Map;

public final class AbilityCommand {
    private AbilityCommand() {}

    /** Registers ability commands. Registry is resolved at execution time so this can be called before SERVER_STARTED. */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ability")
                .then(Commands.literal("list")
                        .executes(ctx -> listAbilities(ctx.getSource())))
                .then(Commands.literal("grant")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.argument("ability_id", StringArgumentType.word())
                                .executes(ctx -> grantAbility(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "ability_id"), -1))
                                .then(Commands.argument("slot", IntegerArgumentType.integer(1, 9))
                                        .executes(ctx -> grantAbility(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "ability_id"),
                                                IntegerArgumentType.getInteger(ctx, "slot"))))))
                .then(Commands.literal("clear")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(ctx -> clearAll(ctx.getSource()))
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, 9))
                                .executes(ctx -> clearSlot(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "slot")))))
                .then(Commands.literal("cooldowns")
                        .executes(ctx -> showCooldowns(ctx.getSource())))
        );
    }

    private static int listAbilities(CommandSourceStack source) {
        AbilityRegistry registry = Relicwrought.getAbilityRegistry();
        if (registry == null) {
            source.sendFailure(Component.literal("Ability system not loaded"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("=== Loaded Abilities (" + registry.size() + ") ==="), false);
        for (AbilityDefinition def : registry.all()) {
            source.sendSuccess(() -> Component.literal("  " + def.id() + " [" + def.effectType() + "] class="
                    + def.allowedClasses() + " cost=" + def.resourceCost() + " " + def.resourceType()
                    + " cd=" + def.cooldownTicks() + "t"), false);
        }
        return 1;
    }

    private static int grantAbility(CommandSourceStack source, String abilityId, int slot) {
        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("Player only"));
            return 0;
        }
        AbilityRegistry registry = Relicwrought.getAbilityRegistry();
        if (registry == null) {
            source.sendFailure(Component.literal("Ability system not loaded"));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        DefinitionKey key = DefinitionKey.parse(abilityId, Relicwrought.MOD_ID);
        if (registry.get(key).isEmpty()) {
            source.sendFailure(Component.literal("Ability not found: " + abilityId));
            return 0;
        }
        PlayerAbilityLoadout loadout = Relicwrought.getLoadout(player);
        if (slot >= 1 && slot <= 9) {
            loadout.setSlot(slot - 1, abilityId);
            source.sendSuccess(() -> Component.literal("Set slot " + slot + " to " + abilityId), true);
        } else {
            int emptySlot = findEmptySlot(loadout);
            if (emptySlot < 0) {
                source.sendFailure(Component.literal("All slots full, specify a slot (1-9)"));
                return 0;
            }
            int slotNum = emptySlot + 1;
            loadout.setSlot(emptySlot, abilityId);
            source.sendSuccess(() -> Component.literal("Set slot " + slotNum + " to " + abilityId), true);
        }
        Relicwrought.syncAbilityLoadout(player);
        return 1;
    }

    private static int clearAll(CommandSourceStack source) {
        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("Player only"));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        PlayerAbilityLoadout loadout = Relicwrought.getLoadout(player);
        for (int i = 0; i < 9; i++) {
            loadout.clearSlot(i);
        }
        Relicwrought.syncAbilityLoadout(player);
        source.sendSuccess(() -> Component.literal("Cleared all ability slots"), true);
        return 1;
    }

    private static int clearSlot(CommandSourceStack source, int slot) {
        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("Player only"));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        PlayerAbilityLoadout loadout = Relicwrought.getLoadout(player);
        loadout.clearSlot(slot - 1);
        Relicwrought.syncAbilityLoadout(player);
        source.sendSuccess(() -> Component.literal("Cleared slot " + slot), true);
        return 1;
    }

    private static int showCooldowns(CommandSourceStack source) {
        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("Player only"));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        PlayerAbilityCooldowns cds = Relicwrought.getCooldowns(player);
        Map<String, Integer> active = cds.getActiveCooldowns();
        if (active.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No active cooldowns"), false);
        } else {
            source.sendSuccess(() -> Component.literal("=== Active Cooldowns ==="), false);
            for (var entry : active.entrySet()) {
                source.sendSuccess(() -> Component.literal("  " + entry.getKey() + ": " + entry.getValue() + "t"), false);
            }
        }
        return 1;
    }

    private static int findEmptySlot(PlayerAbilityLoadout loadout) {
        for (int i = 0; i < 9; i++) {
            if (loadout.getAbilityId(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
