package io.github.bysenom.relicwrought.ui;

import io.github.bysenom.relicwrought.combat.stats.CharacterCombatStats;
import io.github.bysenom.relicwrought.item.model.ArpgEquipmentSlot;
import io.github.bysenom.relicwrought.progression.CharacterAttribute;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RpgEquipmentScreenModel {
    private static final List<SlotAnchor> SLOT_ANCHORS = List.of(
            new SlotAnchor(ArpgEquipmentSlot.HEAD, 66, 0, "Hd"),
            new SlotAnchor(ArpgEquipmentSlot.SHOULDERS, 24, 24, "Sh"),
            new SlotAnchor(ArpgEquipmentSlot.NECK, 108, 24, "Nk"),
            new SlotAnchor(ArpgEquipmentSlot.CLOAK, 24, 46, "Cl"),
            new SlotAnchor(ArpgEquipmentSlot.CHEST, 108, 46, "Ch"),
            new SlotAnchor(ArpgEquipmentSlot.BELT, 24, 68, "Bt"),
            new SlotAnchor(ArpgEquipmentSlot.LEGS, 108, 68, "Lg"),
            new SlotAnchor(ArpgEquipmentSlot.FEET, 66, 90, "Ft"),
            new SlotAnchor(ArpgEquipmentSlot.RING_1, 24, 114, "R1"),
            new SlotAnchor(ArpgEquipmentSlot.RING_2, 108, 114, "R2"),
            new SlotAnchor(ArpgEquipmentSlot.TRINKET_1, 24, 136, "T1"),
            new SlotAnchor(ArpgEquipmentSlot.TRINKET_2, 108, 136, "T2"),
            new SlotAnchor(ArpgEquipmentSlot.MAIN_HAND, 24, 158, "Mn"),
            new SlotAnchor(ArpgEquipmentSlot.OFF_HAND, 108, 158, "Of")
    );

    private static final PlayerModelArea PLAYER_MODEL_AREA = new PlayerModelArea(198, 44, 116, 162);

    private final List<SlotView> slots;
    private final List<InventorySlotView> inventorySlots;
    private final List<InventorySlotView> hotbarSlots;
    private final List<StatGroup> statGroups;
    private final SelectedItemView selectedItem;

    private RpgEquipmentScreenModel(
            List<SlotView> slots,
            List<InventorySlotView> inventorySlots,
            List<InventorySlotView> hotbarSlots,
            List<StatGroup> statGroups,
            SelectedItemView selectedItem
    ) {
        this.slots = List.copyOf(slots);
        this.inventorySlots = List.copyOf(inventorySlots);
        this.hotbarSlots = List.copyOf(hotbarSlots);
        this.statGroups = List.copyOf(statGroups);
        this.selectedItem = selectedItem;
    }

    public static RpgEquipmentScreenModel create(
            Map<ArpgEquipmentSlot, ItemStack> equipment,
            ItemStack selectedItem,
            int characterLevel,
            Map<CharacterAttribute, Integer> totalAttributes,
            CharacterCombatStats combatStats
    ) {
        return create(equipment, List.of(), selectedItem, Set.of(), characterLevel, totalAttributes, combatStats);
    }

    public static RpgEquipmentScreenModel create(
            Map<ArpgEquipmentSlot, ItemStack> equipment,
            List<ItemStack> inventory,
            ItemStack selectedItem,
            Set<ArpgEquipmentSlot> selectedAllowedSlots,
            int characterLevel,
            Map<CharacterAttribute, Integer> totalAttributes,
            CharacterCombatStats combatStats
    ) {
        Map<ArpgEquipmentSlot, ItemStack> copiedEquipment = new EnumMap<>(ArpgEquipmentSlot.class);
        if (equipment != null) {
            copiedEquipment.putAll(equipment);
        }
        Set<ArpgEquipmentSlot> allowedSlots = selectedAllowedSlots == null ? Set.of() : Set.copyOf(selectedAllowedSlots);
        List<SlotView> slots = SLOT_ANCHORS.stream()
                .map(anchor -> {
                    ItemStack stack = copiedEquipment.getOrDefault(anchor.slot(), ItemStack.EMPTY);
                    return new SlotView(
                            anchor.slot(),
                            stack,
                            anchor.x(),
                            anchor.y(),
                            anchor.placeholder(),
                            highlight(anchor.slot(), allowedSlots)
                    );
                })
                .toList();
        return new RpgEquipmentScreenModel(
                slots,
                buildInventorySlots(inventory, false),
                buildInventorySlots(inventory, true),
                buildStatGroups(characterLevel, totalAttributes, combatStats),
                SelectedItemView.of(selectedItem, allowedSlots)
        );
    }

    public List<SlotView> slots() {
        return slots;
    }

    public List<InventorySlotView> inventorySlots() {
        return inventorySlots;
    }

    public List<InventorySlotView> hotbarSlots() {
        return hotbarSlots;
    }

    public List<StatGroup> statGroups() {
        return statGroups;
    }

    public List<StatLine> stats() {
        return statGroups.stream().flatMap(group -> group.lines().stream()).toList();
    }

    public SelectedItemView selectedItem() {
        return selectedItem;
    }

    public PlayerModelArea playerModelArea() {
        return PLAYER_MODEL_AREA;
    }

    public boolean hasCraftingComponents() {
        return false;
    }

    public InteractionPreview previewClick(ArpgEquipmentSlot slot, ItemStack selectedStack) {
        if (slot == null) {
            return InteractionPreview.error("ui.relicwrought.inventory.invalid_slot");
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

    private static HighlightState highlight(ArpgEquipmentSlot slot, Set<ArpgEquipmentSlot> allowedSlots) {
        if (allowedSlots.isEmpty()) {
            return HighlightState.NEUTRAL;
        }
        return allowedSlots.contains(slot) ? HighlightState.VALID : HighlightState.INVALID;
    }

    private static List<InventorySlotView> buildInventorySlots(List<ItemStack> inventory, boolean hotbar) {
        int startIndex = hotbar ? 0 : 9;
        int count = hotbar ? 9 : 27;
        int yOffset = hotbar ? 68 : 0;
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> {
                    int inventoryIndex = startIndex + index;
                    ItemStack stack = stackAt(inventory, inventoryIndex);
                    return new InventorySlotView(
                            inventoryIndex,
                            stack,
                            (index % 9) * 20,
                            yOffset + (index / 9) * 20,
                            hotbar
                    );
                })
                .toList();
    }

    private static ItemStack stackAt(List<ItemStack> inventory, int index) {
        if (inventory == null || index < 0 || index >= inventory.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = inventory.get(index);
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    private static List<StatGroup> buildStatGroups(
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
                new StatGroup("ui.relicwrought.inventory.stat_group.identity", List.of(
                        new StatLine("ui.relicwrought.inventory.stat.class", Component.translatable("ui.relicwrought.inventory.class_unknown")),
                        new StatLine("ui.relicwrought.inventory.stat.level", Component.literal(Integer.toString(Math.max(1, characterLevel))))
                )),
                new StatGroup("ui.relicwrought.inventory.stat_group.attributes", List.of(
                        new StatLine("ui.relicwrought.inventory.stat.strength", Component.literal(Integer.toString(attribute(attributes, CharacterAttribute.STRENGTH)))),
                        new StatLine("ui.relicwrought.inventory.stat.dexterity", Component.literal(Integer.toString(attribute(attributes, CharacterAttribute.DEXTERITY)))),
                        new StatLine("ui.relicwrought.inventory.stat.intelligence", Component.literal(Integer.toString(attribute(attributes, CharacterAttribute.INTELLIGENCE)))),
                        new StatLine("ui.relicwrought.inventory.stat.vitality", Component.literal(Integer.toString(attribute(attributes, CharacterAttribute.VITALITY))))
                )),
                new StatGroup("ui.relicwrought.inventory.stat_group.offense", List.of(
                        new StatLine("ui.relicwrought.inventory.stat.damage", Component.literal(formatPercent(stats.physicalDamagePercent()))),
                        new StatLine("ui.relicwrought.inventory.stat.attack_speed", Component.literal(formatPercent(stats.attackSpeedPercent()))),
                        new StatLine("ui.relicwrought.inventory.stat.crit_chance", Component.literal(formatPercent(stats.criticalStrikeChance()))),
                        new StatLine("ui.relicwrought.inventory.stat.crit_damage", Component.literal(formatMultiplier(stats.criticalStrikeMultiplier())))
                )),
                new StatGroup("ui.relicwrought.inventory.stat_group.defense", List.of(
                        new StatLine("ui.relicwrought.inventory.stat.life", Component.literal(formatNumber(stats.maximumLife()))),
                        new StatLine("ui.relicwrought.inventory.stat.armor", Component.literal(formatNumber(stats.armor()))),
                        new StatLine("ui.relicwrought.inventory.stat.fire_resistance", Component.literal(formatPercent(stats.fireResistance()))),
                        new StatLine("ui.relicwrought.inventory.stat.cold_resistance", Component.literal(formatPercent(stats.coldResistance()))),
                        new StatLine("ui.relicwrought.inventory.stat.lightning_resistance", Component.literal(formatPercent(stats.lightningResistance()))),
                        new StatLine("ui.relicwrought.inventory.stat.poison_resistance", Component.literal(formatPercent(stats.poisonResistance())))
                ))
        );
    }

    private static int attribute(Map<CharacterAttribute, Integer> attributes, CharacterAttribute attribute) {
        return attributes.getOrDefault(attribute, 0);
    }

    private static String formatPercent(double value) {
        return String.format(java.util.Locale.ROOT, "+%.1f%%", value * 100.0);
    }

    private static String formatMultiplier(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f%%", value * 100.0);
    }

    private static String formatNumber(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private record SlotAnchor(ArpgEquipmentSlot slot, int x, int y, String placeholder) {
    }

    public enum HighlightState {
        NEUTRAL,
        VALID,
        INVALID
    }

    public record SlotView(
            ArpgEquipmentSlot slot,
            ItemStack stack,
            int x,
            int y,
            String placeholder,
            HighlightState highlight
    ) {
        public SlotView {
            if (slot == null) {
                throw new IllegalArgumentException("Slot must not be null");
            }
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
            highlight = highlight == null ? HighlightState.NEUTRAL : highlight;
        }

        public boolean occupied() {
            return !stack.isEmpty();
        }

        public boolean interactive() {
            return true;
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

    public record InventorySlotView(int inventoryIndex, ItemStack stack, int x, int y, boolean hotbar) {
        public InventorySlotView {
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
        }

        public boolean occupied() {
            return !stack.isEmpty();
        }
    }

    public record PlayerModelArea(int x, int y, int width, int height) {
    }

    public record StatGroup(String titleKey, List<StatLine> lines) {
        public StatGroup {
            lines = List.copyOf(lines);
        }

        public Component title() {
            return Component.translatable(titleKey);
        }
    }

    public record StatLine(String labelKey, Component value) {
        public Component label() {
            return Component.translatable(labelKey);
        }
    }

    public record SelectedItemView(ItemStack stack, Set<ArpgEquipmentSlot> allowedSlots) {
        public SelectedItemView {
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
            allowedSlots = allowedSlots == null ? Set.of() : Set.copyOf(allowedSlots);
        }

        public static SelectedItemView of(ItemStack stack, Set<ArpgEquipmentSlot> allowedSlots) {
            return new SelectedItemView(stack, allowedSlots);
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
