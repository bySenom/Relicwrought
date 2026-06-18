package io.github.bysenom.relicwrought.client.tooltip;

import io.github.bysenom.relicwrought.item.ArpgItemSystems;
import io.github.bysenom.relicwrought.item.format.ArpgItemDisplayModel;
import io.github.bysenom.relicwrought.item.format.ArpgItemDisplayModel.DisplayLine;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadResult;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadStatus;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import io.github.bysenom.relicwrought.item.registry.DefinitionLoadResult;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class ArpgItemTooltipAppender {
    private static final ArpgItemStackService stackService = new ArpgItemStackService(List.of());

    private ArpgItemTooltipAppender() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register(ArpgItemTooltipAppender::onTooltip);
    }

    private static void onTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag flag, List<Component> lines) {
        if (stack.isEmpty()) return;

        ArpgItemReadResult readResult = stackService.read(stack);
        if (readResult.status() == ArpgItemReadStatus.NOT_ARPG_ITEM) return;

        DefinitionLoadResult definitions = ArpgItemSystems.bootstrapResult();
        ArpgItemDisplayModel model = ArpgItemDisplayModel.fromReadResult(readResult, definitions.itemBases(), definitions.scalingProfiles());

        if (!model.hasArpgData()) return;

        boolean advanced = flag.isAdvanced();

        lines.clear();

        TextColor rarityColor = TextColor.parseColor(String.format("#%06X", model.displayName().rarityColor())).getOrThrow();

        MutableComponent nameLine = Component.literal("")
                .append(Component.translatable(model.displayName().baseTranslationKey())
                        .withStyle(style -> style.withColor(rarityColor)));
        lines.add(nameLine);

        for (String statusMsg : model.tooltipStatus()) {
            switch (statusMsg) {
                case "migrated" ->
                        lines.add(Component.translatable("tooltip.relicwrought.migrated").withStyle(ChatFormatting.YELLOW));
                case "invalid_data", "invalid" ->
                        lines.add(Component.translatable("tooltip.relicwrought.invalid_data").withStyle(ChatFormatting.RED));
                case "unsupported_version" ->
                        lines.add(Component.translatable("tooltip.relicwrought.unsupported_version").withStyle(ChatFormatting.RED));
                case "missing_definition" ->
                        lines.add(Component.translatable("tooltip.relicwrought.missing_definition").withStyle(ChatFormatting.RED));
                default -> {
                    if (statusMsg.startsWith("missing_base:")) {
                        lines.add(Component.translatable("tooltip.relicwrought.missing_definition").withStyle(ChatFormatting.RED));
                    } else {
                        lines.add(Component.literal("  - " + statusMsg).withStyle(ChatFormatting.RED));
                    }
                }
            }
        }

        if (!model.baseStatLines().isEmpty()) {
            lines.add(Component.empty());
            for (DisplayLine stat : model.baseStatLines()) {
                String key = stat.translationKey() != null ? stat.translationKey() : "tooltip.relicwrought." + stat.label();
                lines.add(Component.translatable(key, stat.value()).withStyle(ChatFormatting.GRAY));
            }
        }

        if (!model.implicitLines().isEmpty()) {
            lines.add(Component.empty());
            for (DisplayLine line : model.implicitLines()) {
                lines.add(buildAffixComponent(line).withStyle(ChatFormatting.AQUA));
            }
        }

        if (!model.prefixLines().isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.translatable("tooltip.relicwrought.prefixes").withStyle(style -> style.withColor(0xFFAA00)));
            for (DisplayLine line : model.prefixLines()) {
                lines.add(buildAffixComponent(line).withStyle(ChatFormatting.WHITE));
            }
        }

        if (!model.suffixLines().isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.translatable("tooltip.relicwrought.suffixes").withStyle(style -> style.withColor(0x55AAFF)));
            for (DisplayLine line : model.suffixLines()) {
                lines.add(buildAffixComponent(line).withStyle(ChatFormatting.WHITE));
            }
        }

        if (advanced) {
            lines.add(Component.empty());
            lines.add(Component.translatable("tooltip.relicwrought.technical").withStyle(ChatFormatting.DARK_GRAY));
            for (DisplayLine techLine : model.technicalLines()) {
                lines.add(Component.literal(techLine.label() + ": " + techLine.value()).withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            lines.add(Component.empty());
            lines.add(Component.translatable("tooltip.relicwrought.advanced_hint").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static MutableComponent buildAffixComponent(DisplayLine line) {
        if (line.translationKey() != null) {
            return Component.literal(line.value())
                    .append(Component.literal(" "))
                    .append(Component.translatable(line.translationKey()));
        }
        return Component.literal(line.value());
    }
}
