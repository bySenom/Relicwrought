package io.github.bysenom.relicwrought.ui;

import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStats;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.progression.CharacterAttribute;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class RpgEquipmentScreenModel {
    private static final List<SlotAnchor> SLOT_ANCHORS = List.of(
            new SlotAnchor(ArpgEquipmentSlot.HEAD, 74, 2, "Hd"),
            new SlotAnchor(ArpgEquipmentSlot.SHOULDERS, 34, 24, "Sh"),
            new SlotAnchor(ArpgEquipmentSlot.NECK, 114, 24, "Nk"),
            new SlotAnchor(ArpgEquipmentSlot.CLOAK, 34, 46, "Cl"),
            new SlotAnchor(ArpgEquipmentSlot.CHEST, 114, 46, "Ch"),
            new SlotAnchor(ArpgEquipmentSlot.BELT, 34, 68, "Bt"),
            new SlotAnchor(ArpgEquipmentSlot.LEGS, 114, 68, "Lg"),
            new SlotAnchor(ArpgEquipmentSlot.FEET, 74, 90, "Ft"),
            new SlotAnchor(ArpgEquipmentSlot.RING_1, 34, 114, "R1"),
            new SlotAnchor(ArpgEquipmentSlot.RING_2, 114, 114, "R2"),
            new SlotAnchor(ArpgEquipmentSlot.TRINKET_1, 34, 136, "T1"),
            new SlotAnchor(ArpgEquipmentSlot.TRINKET_2, 114, 136, "T2"),
            new SlotAnchor(ArpgEquipmentSlot.MAIN_HAND, 34, 158, "Mn"),
            new SlotAnchor(ArpgEquipmentSlot.OFF_HAND, 114, 158, "Of")
    );

    private final List<SlotView> slots;
    private final List<StatLine> stats;
    private final SelectedItemView selectedItem;

    private RpgEquipmentScreenModel(List<SlotView> slots, List<StatLine> stats, SelectedItemView selectedItem) {
        this.slots = List.copyOf(slots);
        this.stats = List.copyOf(stats);
        this.selectedItem = selectedItem;
    }

    public static RpgEquipmentScreenModel create(
            Map<ArpgEquipmentSlot, ItemStack> equipment,
            ItemStack selectedItem,
            int characterLevel,
            Map<CharacterAttribute, Integer> totalAttributes,
            CharacterCombatStats combatStats
    ) {
        Map<ArpgEquipmentSlot, ItemStack> copiedEquipment = new EnumMap<>(ArpgEquipmentSlot.class);
        if (equipment != null) {
            copiedEquipment.putAll(equipment);
        }
        List<SlotView> slots = SLOT_ANCHORS.stream()
                .map(anchor -> {
                    ItemStack stack = copiedEquipment.getOrDefault(anchor.slot(), ItemStack.EMPTY);
                    return new SlotView(
                            anchor.slot(),
                            stack,
                            anchor.x(),
                            anchor.y(),
                            anchor.placeholder()
                    );
                })
                .toList();
        return new RpgEquipmentScreenModel(
                slots,
                buildStats(characterLevel, totalAttributes, combatStats),
                SelectedItemView.of(selectedItem)
        );
    }

    public List<SlotView> slots() {
        return slots;
    }

    public List<StatLine> stats() {
        return stats;
    }

    public SelectedItemView selectedItem() {
        return selectedItem;
    }

    public boolean hasCraftingComponents() {
        return false;
    }

    public InteractionPreview previewClick(ArpgEquipmentSlot slot, ItemStack selectedStack) {
        if (slot == null) {
            return InteractionPreview.error("ui.relicwrought.inventory.invalid_slot");
        }
        if (!slot.isExtraSlot()) {
            return InteractionPreview.error("ui.relicwrought.inventory.vanilla_slot_read_only");
        }
        ItemStack selected = selectedStack == null ? ItemStack.EMPTY : selectedStack;
        if (selected.isEmpty()) {
            return InteractionPreview.error("ui.relicwrought.inventory.no_selected_item");
        }
        if (selected.getCount() != 1) {
            return InteractionPreview.error("ui.relicwrought.inventory.stack_size_invalid");
        }
        return InteractionPreview.success("ui.relicwrought.inventory.request_sent");
    }

    private static List<StatLine> buildStats(
            int characterLevel,
            Map<CharacterAttribute, Integer> totalAttributes,
            CharacterCombatStats combatStats
    ) {
        Map<CharacterAttribute, Integer> attributes = totalAttributes == null
                ? Map.of()
                : totalAttributes;
        CharacterCombatStats stats = combatStats == null
                ? CharacterCombatStats.empty()
                : combatStats;
        return List.of(
                new StatLine("ui.relicwrought.inventory.stat.class", Component.translatable("ui.relicwrought.inventory.class_unknown")),
                new StatLine("ui.relicwrought.inventory.stat.level", Component.literal(Integer.toString(Math.max(1, characterLevel)))),
                new StatLine("ui.relicwrought.inventory.stat.strength", Component.literal(Integer.toString(attribute(attributes, CharacterAttribute.STRENGTH)))),
                new StatLine("ui.relicwrought.inventory.stat.dexterity", Component.literal(Integer.toString(attribute(attributes, CharacterAttribute.DEXTERITY)))),
                new StatLine("ui.relicwrought.inventory.stat.intelligence", Component.literal(Integer.toString(attribute(attributes, CharacterAttribute.INTELLIGENCE)))),
                new StatLine("ui.relicwrought.inventory.stat.vitality", Component.literal(Integer.toString(attribute(attributes, CharacterAttribute.VITALITY)))),
                new StatLine("ui.relicwrought.inventory.stat.damage", Component.literal(formatPercent(stats.physicalDamagePercent()))),
                new StatLine("ui.relicwrought.inventory.stat.crit_chance", Component.literal(formatPercent(stats.criticalStrikeChance()))),
                new StatLine("ui.relicwrought.inventory.stat.armor", Component.literal(formatNumber(stats.armor()))),
                new StatLine("ui.relicwrought.inventory.stat.life", Component.literal(formatNumber(stats.maximumLife())))
        );
    }

    private static int attribute(Map<CharacterAttribute, Integer> attributes, CharacterAttribute attribute) {
        return attributes.getOrDefault(attribute, 0);
    }

    private static String formatPercent(double value) {
        return String.format(java.util.Locale.ROOT, "+%.1f%%", value * 100.0);
    }

    private static String formatNumber(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private record SlotAnchor(ArpgEquipmentSlot slot, int x, int y, String placeholder) {
    }

    public record SlotView(
            ArpgEquipmentSlot slot,
            ItemStack stack,
            int x,
            int y,
            String placeholder
    ) {
        public SlotView {
            if (slot == null) {
                throw new IllegalArgumentException("Slot must not be null");
            }
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
        }

        public boolean occupied() {
            return !stack.isEmpty();
        }

        public boolean interactive() {
            return slot.isExtraSlot();
        }

        public Component label() {
            return Component.translatable(slot.translationKey());
        }

        public List<Component> hoverText() {
            if (occupied()) {
                return List.of(label(), stack.getHoverName());
            }
            return List.of(label(), Component.translatable("ui.relicwrought.inventory.empty_slot_hint"));
        }
    }

    public record StatLine(String labelKey, Component value) {
        public Component label() {
            return Component.translatable(labelKey);
        }
    }

    public record SelectedItemView(ItemStack stack) {
        public SelectedItemView {
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
        }

        public static SelectedItemView of(ItemStack stack) {
            return new SelectedItemView(stack);
        }

        public boolean present() {
            return !stack.isEmpty();
        }

        public Component displayName() {
            return present() ? stack.getHoverName() : Component.translatable("ui.relicwrought.inventory.selected_empty_short");
        }
    }

    public record InteractionPreview(boolean allowed, String messageKey) {
        public static InteractionPreview success(String messageKey) {
            return new InteractionPreview(true, messageKey);
        }

        public static InteractionPreview error(String messageKey) {
            return new InteractionPreview(false, messageKey);
        }
    }
}
