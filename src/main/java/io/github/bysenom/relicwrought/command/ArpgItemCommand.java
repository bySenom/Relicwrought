package io.github.bysenom.relicwrought.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.ArpgItemSystems;
import io.github.bysenom.relicwrought.item.affix.AffixGenerator;
import io.github.bysenom.relicwrought.item.format.ArpgItemDisplayModel;
import io.github.bysenom.relicwrought.item.format.ArpgItemDisplayModel.DisplayLine;
import io.github.bysenom.relicwrought.item.generation.ArpgItemGenerator;
import io.github.bysenom.relicwrought.item.generation.GenerationErrorCode;
import io.github.bysenom.relicwrought.item.generation.ItemGenerationRequest;
import io.github.bysenom.relicwrought.item.generation.ItemGenerationResult;
import io.github.bysenom.relicwrought.item.generation.RarityDefinition;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemBaseDefinition;
import io.github.bysenom.relicwrought.item.model.ItemCategory;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemPersistenceValidator;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadResult;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadStatus;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemWriteResult;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;
import io.github.bysenom.relicwrought.item.registry.DefinitionLoadResult;
import io.github.bysenom.relicwrought.item.scaling.ItemStatScaler;
import io.github.bysenom.relicwrought.item.scaling.ScalingProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import io.github.bysenom.relicwrought.loot.ArpgDropGenerator;
import io.github.bysenom.relicwrought.loot.LootContextData;
import io.github.bysenom.relicwrought.loot.LootDropResult;
import io.github.bysenom.relicwrought.loot.LootProfileDefinition;
import io.github.bysenom.relicwrought.loot.LootProfileResolver;
import io.github.bysenom.relicwrought.loot.LootSourceType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.SplittableRandom;

public final class ArpgItemCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_PLAYER = new SimpleCommandExceptionType(
            Component.translatable("command.relicwrought.arpgitem.error.not_player")
    );
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_BASE = new DynamicCommandExceptionType(
            id -> Component.translatable("command.relicwrought.arpgitem.error.unknown_base", id)
    );
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_RARITY = new DynamicCommandExceptionType(
            id -> Component.translatable("command.relicwrought.arpgitem.error.unknown_rarity", id)
    );
    private static final SimpleCommandExceptionType ERROR_NO_ITEM = new SimpleCommandExceptionType(
            Component.translatable("command.relicwrought.arpgitem.error.no_item")
    );
    private static final SimpleCommandExceptionType ERROR_NOT_ARPG = new SimpleCommandExceptionType(
            Component.translatable("command.relicwrought.arpgitem.error.not_arpg_item")
    );

    private static ArpgItemGenerator generator;
    private static ArpgItemStackService stackService;
    private static DataRegistry<ItemBaseDefinition> itemBases;
    private static DataRegistry<RarityDefinition> rarities;
    private static DataRegistry<ScalingProfile> scalingProfiles;

    private ArpgItemCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        DefinitionLoadResult result = ArpgItemSystems.bootstrapResult();
        itemBases = result.itemBases();
        rarities = result.rarities();
        scalingProfiles = result.scalingProfiles();
        stackService = new ArpgItemStackService(List.of());
        generator = new ArpgItemGenerator(
                result.itemBases(),
                result.rarities(),
                new AffixGenerator(result.affixes(), result.affixGroups()),
                new ItemStatScaler(result.scalingProfiles()),
                stackService
        );

        dispatcher.register(Commands.literal("arpgitem")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("generate")
                        .then(Commands.argument("item_base", StringArgumentType.word())
                                .then(Commands.argument("item_level", IntegerArgumentType.integer(1, 950))
                                        .executes(ctx -> generate(ctx, StringArgumentType.getString(ctx, "item_base"), IntegerArgumentType.getInteger(ctx, "item_level"), null, null, null))
                                        .then(Commands.argument("rarity", StringArgumentType.word())
                                                .executes(ctx -> generate(ctx, StringArgumentType.getString(ctx, "item_base"), IntegerArgumentType.getInteger(ctx, "item_level"), StringArgumentType.getString(ctx, "rarity"), null, null))
                                                .then(Commands.argument("quality", IntegerArgumentType.integer(0, 20))
                                                        .executes(ctx -> generate(ctx, StringArgumentType.getString(ctx, "item_base"), IntegerArgumentType.getInteger(ctx, "item_level"), StringArgumentType.getString(ctx, "rarity"), IntegerArgumentType.getInteger(ctx, "quality"), null))
                                                        .then(Commands.argument("seed", LongArgumentType.longArg())
                                                                .executes(ctx -> generate(ctx, StringArgumentType.getString(ctx, "item_base"), IntegerArgumentType.getInteger(ctx, "item_level"), StringArgumentType.getString(ctx, "rarity"), IntegerArgumentType.getInteger(ctx, "quality"), LongArgumentType.getLong(ctx, "seed")))
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("random")
                        .then(Commands.argument("item_level", IntegerArgumentType.integer(1, 950))
                                .executes(ctx -> random(ctx, IntegerArgumentType.getInteger(ctx, "item_level"), null, null, null))
                                .then(Commands.argument("category", StringArgumentType.word())
                                        .executes(ctx -> random(ctx, IntegerArgumentType.getInteger(ctx, "item_level"), StringArgumentType.getString(ctx, "category"), null, null))
                                        .then(Commands.argument("rarity", StringArgumentType.word())
                                                .executes(ctx -> random(ctx, IntegerArgumentType.getInteger(ctx, "item_level"), StringArgumentType.getString(ctx, "category"), StringArgumentType.getString(ctx, "rarity"), null))
                                                .then(Commands.argument("seed", LongArgumentType.longArg())
                                                        .executes(ctx -> random(ctx, IntegerArgumentType.getInteger(ctx, "item_level"), StringArgumentType.getString(ctx, "category"), StringArgumentType.getString(ctx, "rarity"), LongArgumentType.getLong(ctx, "seed")))
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("inspect")
                        .executes(ctx -> inspect(ctx, false))
                        .then(Commands.literal("full")
                                .executes(ctx -> inspect(ctx, true))
                        )
                )
                .then(Commands.literal("validate")
                        .executes(ArpgItemCommand::validate)
                )
                .then(Commands.literal("remove")
                        .executes(ArpgItemCommand::remove)
                )
                .then(Commands.literal("help")
                        .executes(ArpgItemCommand::help)
                )
        );
    }

    private static int generate(CommandContext<CommandSourceStack> ctx, String baseId, int level, String rarityId, Integer quality, Long seed) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        long finalSeed = seed != null ? seed : new Random().nextLong();
        if (finalSeed == 0) finalSeed = 1;

        DefinitionKey baseKey = DefinitionKey.parse(baseId, Relicwrought.MOD_ID);
        if (itemBases.get(baseKey).isEmpty()) {
            throw ERROR_UNKNOWN_BASE.create(baseId);
        }

        DefinitionKey rarityKey = null;
        if (rarityId != null) {
            rarityKey = DefinitionKey.parse(rarityId, Relicwrought.MOD_ID);
            if (rarities.get(rarityKey).isEmpty()) {
                throw ERROR_UNKNOWN_RARITY.create(rarityId);
            }
        }

        ItemLevel itemLevel = ItemLevel.of(level);
        ItemGenerationRequest request = new ItemGenerationRequest(
                baseKey, itemLevel, finalSeed, rarityKey, quality,
                null, null, null, null,
                null, true, null
        );

        ItemGenerationResult result = generator.generate(request);
        if (!result.success()) {
            ctx.getSource().sendFailure(Component.translatable("command.relicwrought.arpgitem.generation_failed",
                    result.errorCode().name()));
            return 0;
        }

        ItemStack stack = result.itemStack();
        ItemStack deliveryStack = stack.copy();
        Component displayName = stack.getHoverName().copy();
        if (!player.getInventory().add(deliveryStack)) {
            ItemEntity entity = player.drop(stack.copy(), false);
            if (entity != null) {
                entity.setNoPickUpDelay();
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("command.relicwrought.arpgitem.dropped", displayName), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("command.relicwrought.arpgitem.given", displayName), true);
        }
        return 1;
    }

    private static int random(CommandContext<CommandSourceStack> ctx, int level, String categoryStr, String rarityId, Long seed) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        long finalSeed = seed != null ? seed : new Random().nextLong();
        if (finalSeed == 0) finalSeed = 1;

        DefinitionKey rarityKey = null;
        if (rarityId != null) {
            rarityKey = DefinitionKey.parse(rarityId, Relicwrought.MOD_ID);
            if (rarities.get(rarityKey).isEmpty()) {
                throw ERROR_UNKNOWN_RARITY.create(rarityId);
            }
        }

        ItemCategory category = null;
        if (categoryStr != null) {
            try {
                category = ItemCategory.valueOf(categoryStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                ctx.getSource().sendFailure(Component.translatable("command.relicwrought.arpgitem.error.invalid_category", categoryStr));
                return 0;
            }
        }

        ItemLevel itemLevel = ItemLevel.of(level);
        ItemGenerationRequest request = new ItemGenerationRequest(
                null, itemLevel, finalSeed, rarityKey, null,
                category != null ? java.util.Set.of(category) : null,
                null, null, null,
                null, true, null
        );

        ItemGenerationResult result = generator.generate(request);
        if (!result.success()) {
            ctx.getSource().sendFailure(Component.translatable("command.relicwrought.arpgitem.generation_failed",
                    result.errorCode().name()));
            return 0;
        }

        ItemStack stack = result.itemStack();
        ItemStack deliveryStack = stack.copy();
        Component displayName = stack.getHoverName().copy();
        if (!player.getInventory().add(deliveryStack)) {
            ItemEntity entity = player.drop(stack.copy(), false);
            if (entity != null) {
                entity.setNoPickUpDelay();
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("command.relicwrought.arpgitem.dropped", displayName), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("command.relicwrought.arpgitem.given", displayName), true);
        }
        return 1;
    }

    private static int inspect(CommandContext<CommandSourceStack> ctx, boolean full) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw ERROR_NO_ITEM.create();
        }

        ArpgItemReadResult readResult = stackService.read(stack);
        if (readResult.status() == ArpgItemReadStatus.NOT_ARPG_ITEM) {
            throw ERROR_NOT_ARPG.create();
        }

        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(readResult, itemBases, scalingProfiles);

        ctx.getSource().sendSuccess(() -> Component.literal("=== ARPG Item Inspection ==="), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Display: " + model.displayName().baseTranslationKey()
                + " (" + model.displayName().rarityTranslationKey() + ")"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Status: " + readResult.status().name()), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Minecraft Item: " + stack.getItem().toString()), false);

        for (DisplayLine stat : model.baseStatLines()) {
            ctx.getSource().sendSuccess(() -> Component.literal(stat.label() + ": " + stat.value()), false);
        }

        if (!model.tooltipStatus().isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Status Messages: " + String.join(", ", model.tooltipStatus())), false);
        }

        if (!model.implicitLines().isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Implicits:"), false);
            for (DisplayLine line : model.implicitLines()) {
                ctx.getSource().sendSuccess(() -> Component.literal("  " + line.value()), false);
            }
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Prefixes (" + model.prefixLines().size() + "):"), false);
        for (DisplayLine line : model.prefixLines()) {
            ctx.getSource().sendSuccess(() -> Component.literal("  " + line.value()), false);
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Suffixes (" + model.suffixLines().size() + "):"), false);
        for (DisplayLine line : model.suffixLines()) {
            ctx.getSource().sendSuccess(() -> Component.literal("  " + line.value()), false);
        }

        if (full) {
            ctx.getSource().sendSuccess(() -> Component.literal("--- Technical ---"), false);
            for (DisplayLine techLine : model.technicalLines()) {
                ctx.getSource().sendSuccess(() -> Component.literal(techLine.label() + ": " + techLine.value()), false);
            }
        }

        return 1;
    }

    private static int validate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw ERROR_NO_ITEM.create();
        }

        ArpgItemReadResult readResult = stackService.read(stack);
        ctx.getSource().sendSuccess(() -> Component.literal("Validation: " + readResult.status().name()), false);

        switch (readResult.status()) {
            case NOT_ARPG_ITEM ->
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.relicwrought.arpgitem.validation.not_arpg"), false);
            case VALID -> ctx.getSource().sendSuccess(() -> Component.translatable("command.relicwrought.arpgitem.validation.valid"), false);
            case MIGRATED -> {
                ctx.getSource().sendSuccess(() -> Component.translatable("command.relicwrought.arpgitem.validation.migrated"), false);
                readResult.data().ifPresent(data -> ctx.getSource().sendSuccess(() -> Component.literal("  Now v" + data.dataVersion()), false));
            }
            case INVALID -> {
                ctx.getSource().sendSuccess(() -> Component.translatable("command.relicwrought.arpgitem.validation.invalid"), false);
                for (String msg : readResult.messages()) {
                    ctx.getSource().sendSuccess(() -> Component.literal("  - " + msg), false);
                }
            }
            case UNSUPPORTED_VERSION ->
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.relicwrought.arpgitem.validation.unsupported_version"), false);
            case MISSING_DEFINITION ->
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.relicwrought.arpgitem.validation.missing_definition"), false);
        }

        if (!readResult.messages().isEmpty() && readResult.status() != ArpgItemReadStatus.INVALID) {
            for (String msg : readResult.messages()) {
                ctx.getSource().sendSuccess(() -> Component.literal("  Note: " + msg), false);
            }
        }

        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw ERROR_NO_ITEM.create();
        }

        if (!stackService.hasArpgData(stack)) {
            throw ERROR_NOT_ARPG.create();
        }

        stackService.remove(stack);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.relicwrought.arpgitem.data_removed"), true);
        return 1;
    }

    private static int help(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("=== Relicwrought Commands ==="), false);
        ctx.getSource().sendSuccess(() -> Component.literal("/arpgitem generate <base> <level> [rarity] [quality] [seed] - Generate an ARPG item"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("/arpgitem random <level> [category] [rarity] [seed] - Generate a random ARPG item"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("/arpgitem inspect [full] - Inspect held ARPG item"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("/arpgitem validate - Validate held ARPG item"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("/arpgitem remove - Remove ARPG data from held item"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("/arpgitem help - Show this help"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("/arpgitem loot simulate <profile> <count> [seed] - Simulate loot drops"), false);
        return 1;
    }

    public static void registerLootCommands(
            CommandDispatcher<CommandSourceStack> dispatcher,
            ArpgDropGenerator dropGenerator,
            LootProfileResolver profileResolver,
            ArpgItemGenerator itemGenerator,
            DataRegistry<ItemBaseDefinition> itemBases
    ) {
        dispatcher.register(Commands.literal("arpgitem")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("loot")
                        .then(Commands.literal("simulate")
                                .then(Commands.argument("profile", StringArgumentType.word())
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 10))
                                                .executes(ctx -> simulateLoot(ctx, dropGenerator, profileResolver,
                                                        StringArgumentType.getString(ctx, "profile"),
                                                        IntegerArgumentType.getInteger(ctx, "count"), null))
                                                .then(Commands.argument("seed", LongArgumentType.longArg())
                                                        .executes(ctx -> simulateLoot(ctx, dropGenerator, profileResolver,
                                                                StringArgumentType.getString(ctx, "profile"),
                                                                IntegerArgumentType.getInteger(ctx, "count"),
                                                                LongArgumentType.getLong(ctx, "seed")))
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int simulateLoot(
            CommandContext<CommandSourceStack> ctx,
            ArpgDropGenerator dropGenerator,
            LootProfileResolver profileResolver,
            String profileId,
            int count,
            Long seed
    ) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        long finalSeed = seed != null ? seed : new Random().nextLong();

        DefinitionKey key = DefinitionKey.parse(profileId, Relicwrought.MOD_ID);
        LootProfileDefinition profile;
        try {
            profile = profileResolver.resolve(key);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.translatable("command.relicwrought.arpgitem.error.unknown_profile", profileId));
            return 0;
        }

        SplittableRandom random = new SplittableRandom(finalSeed);
        int successful = 0;
        int failed = 0;

        for (int i = 0; i < count; i++) {
            LootContextData context = new LootContextData(
                    profile.sourceType(), "minecraft:overworld", "simulated",
                    BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("zombie")), 20.0, 2.0, 3.0,
                    true, 0
            );
            long itemSeed = random.nextLong();
            LootDropResult result = dropGenerator.generateDrops(profile, context, itemSeed);

            if (result.didDrop() && !result.generatedItems().isEmpty()) {
                for (var stack : result.generatedItems()) {
                    if (!player.getInventory().add(stack)) {
                        ItemEntity entity = player.drop(stack, false);
                        if (entity != null) entity.setNoPickUpDelay();
                    }
                }
                successful++;
            } else {
                failed++;
            }
        }

        int finalSuccessful = successful;
        int finalFailed = failed;
        ctx.getSource().sendSuccess(() -> Component.literal("Loot simulation complete: "
                + finalSuccessful + " successful, " + finalFailed + " failed (seed: " + finalSeed + ")"), false);
        return 1;
    }
}
