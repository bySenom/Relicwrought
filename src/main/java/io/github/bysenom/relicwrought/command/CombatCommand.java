package io.github.bysenom.relicwrought.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.combat.stats.AttributeCombatResolver;
import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStatResolver;
import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStats;
import io.github.bysenom.relicwrought.combat.stats.EquippedItemStatResolver;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import io.github.bysenom.relicwrought.progression.CharacterProgression;
import io.github.bysenom.relicwrought.progression.ProgressionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class CombatCommand {
    private CombatCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, ProgressionManager progressionManager, ArpgModConfig config) {
        dispatcher.register(Commands.literal("relicwrought")
                .then(Commands.literal("combat")
                        .then(Commands.literal("stats")
                                .executes(context -> showStats(context.getSource(), progressionManager, config)))
                )
        );
    }

    private static int showStats(CommandSourceStack source, ProgressionManager progressionManager, ArpgModConfig config) {
        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("This command must be executed by a player"));
            return 0;
        }

        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        CharacterProgression progression = progressionManager.getProgression(player);
        CharacterCombatStats baseStats = progression != null 
                ? AttributeCombatResolver.resolve(progression.allocatedAttributes(), config)
                : CharacterCombatStats.empty();

        ArpgItemStackService itemService = new ArpgItemStackService(List.of());
        EquippedItemStatResolver equippedResolver = new EquippedItemStatResolver(itemService);
        CharacterCombatStats equippedStats = equippedResolver.collectGlobalStats(player);

        CharacterCombatStats totalStats = CharacterCombatStatResolver.combine(baseStats, equippedStats);

        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.header"), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.physical", totalStats.physicalDamagePercent() * 100), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.elemental", totalStats.elementalDamagePercent() * 100), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.attack_speed", totalStats.attackSpeedPercent() * 100), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.crit_chance", (config.baseCriticalChance() + totalStats.criticalStrikeChance()) * 100), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.crit_multi", totalStats.criticalStrikeMultiplier() * 100), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.armor", totalStats.armor()), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.fire_res", totalStats.fireResistance() * 100), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.cold_res", totalStats.coldResistance() * 100), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.lightning_res", totalStats.lightningResistance() * 100), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.poison_res", totalStats.poisonResistance() * 100), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.movement", totalStats.movementSpeed() * 100), false);
        source.sendSuccess(() -> Component.translatable("command.relicwrought.combat.stats.life", totalStats.maximumLife()), false);

        return 1;
    }
}
