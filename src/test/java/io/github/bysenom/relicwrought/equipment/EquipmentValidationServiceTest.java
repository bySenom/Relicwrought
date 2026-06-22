package io.github.bysenom.relicwrought.equipment;

import io.github.bysenom.relicwrought.ArpgModConfig;
import io.github.bysenom.relicwrought.item.ItemDataVersions;
import io.github.bysenom.relicwrought.item.model.*;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemReadResult;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import io.github.bysenom.relicwrought.item.registry.InMemoryDataRegistry;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

final class EquipmentValidationServiceTest {
    private static final DefinitionKey RING_ID = DefinitionKey.parse("starter_ring", "relicwrought");
    private static final DefinitionKey NECK_ID = DefinitionKey.parse("starter_necklace", "relicwrought");
    private static final DefinitionKey SHIELD_ID = DefinitionKey.parse("starter_shield", "relicwrought");
    private static final DefinitionKey SWORD_ID = DefinitionKey.parse("starter_sword", "relicwrought");

    private ArpgItemStackService itemService;
    private InMemoryDataRegistry<ItemBaseDefinition> itemBases;
    private EquipmentValidationService validation;

    @BeforeEach
    void setUp() {
        itemService = mock(ArpgItemStackService.class);
        itemBases = new InMemoryDataRegistry<>();
        itemBases.register(base(RING_ID, ItemCategory.RING, Set.of(ArpgEquipmentSlot.RING_1, ArpgEquipmentSlot.RING_2)));
        itemBases.register(base(NECK_ID, ItemCategory.NECKLACE, Set.of(ArpgEquipmentSlot.NECK)));
        itemBases.register(base(SHIELD_ID, ItemCategory.SHIELD, Set.of(ArpgEquipmentSlot.OFF_HAND)));
        itemBases.register(base(SWORD_ID, ItemCategory.SWORD, Set.of(ArpgEquipmentSlot.MAIN_HAND)));
        validation = new EquipmentValidationService(ArpgModConfig.defaults(), itemService, itemBases);
    }

    @Test
    void ringCanUseBothRingSlots() {
        ItemStack ring = arpgStack(RING_ID);

        assertTrue(validation.validateForExtraSlot(ring, ArpgEquipmentSlot.RING_1).success());
        assertTrue(validation.validateForExtraSlot(ring, ArpgEquipmentSlot.RING_2).success());
    }

    @Test
    void necklaceOnlyFitsNeck() {
        ItemStack necklace = arpgStack(NECK_ID);

        assertTrue(validation.validateForExtraSlot(necklace, ArpgEquipmentSlot.NECK).success());
        assertFalse(validation.validateForExtraSlot(necklace, ArpgEquipmentSlot.RING_1).success());
    }

    @Test
    void vanillaMappedSlotsAreReadOnlyInPhaseEightSixA() {
        ItemStack shield = arpgStack(SHIELD_ID);

        EquipmentChangeResult result = validation.validateForExtraSlot(shield, ArpgEquipmentSlot.OFF_HAND);

        assertFalse(result.success());
        assertEquals("ui.relicwrought.inventory.vanilla_slot_read_only", result.translationKey());
    }

    @Test
    void screenValidationAllowsVanillaMappedEquipmentSlots() {
        ItemStack sword = arpgStack(SWORD_ID);
        ItemStack shield = arpgStack(SHIELD_ID);

        assertTrue(validation.validateForSlot(sword, ArpgEquipmentSlot.MAIN_HAND).success());
        assertTrue(validation.validateForSlot(shield, ArpgEquipmentSlot.OFF_HAND).success());
    }

    @Test
    void screenValidationRejectsItemsInWrongSlots() {
        ItemStack sword = arpgStack(SWORD_ID);
        ItemStack ring = arpgStack(RING_ID);

        assertFalse(validation.validateForSlot(sword, ArpgEquipmentSlot.RING_1).success());
        assertFalse(validation.validateForSlot(ring, ArpgEquipmentSlot.HEAD).success());
    }

    @Test
    void rejectsNonArpgItemsByDefault() {
        ItemStack stack = stack(1);
        when(itemService.hasArpgData(stack)).thenReturn(false);

        EquipmentChangeResult result = validation.validateForExtraSlot(stack, ArpgEquipmentSlot.RING_1);

        assertFalse(result.success());
        assertEquals("ui.relicwrought.inventory.invalid_item", result.translationKey());
    }

    @Test
    void rejectsEquipmentStacksLargerThanOne() {
        ItemStack ring = arpgStack(RING_ID);
        when(ring.getCount()).thenReturn(2);

        EquipmentChangeResult result = validation.validateForExtraSlot(ring, ArpgEquipmentSlot.RING_1);

        assertFalse(result.success());
        assertEquals("ui.relicwrought.inventory.stack_size_invalid", result.translationKey());
    }

    private ItemStack arpgStack(DefinitionKey baseId) {
        ItemStack stack = stack(1);
        ArpgItemData data = new ArpgItemData(
                ItemDataVersions.CURRENT,
                UUID.randomUUID(),
                baseId,
                new ItemLevel(1),
                0,
                Rarity.COMMON,
                0,
                1L,
                false,
                List.of(),
                List.of(),
                List.of()
        );
        when(itemService.hasArpgData(stack)).thenReturn(true);
        when(itemService.read(stack)).thenReturn(ArpgItemReadResult.valid(data));
        return stack;
    }

    private static ItemStack stack(int count) {
        ItemStack stack = mock(ItemStack.class);
        when(stack.isEmpty()).thenReturn(false);
        when(stack.getCount()).thenReturn(count);
        when(stack.copy()).thenReturn(stack);
        return stack;
    }

    private static ItemBaseDefinition base(DefinitionKey id, ItemCategory category, Set<ArpgEquipmentSlot> slots) {
        return new ItemBaseDefinition(
                id,
                "item_base." + id.path(),
                "minecraft:gold_nugget",
                category,
                slots,
                BaseStatBlock.empty(),
                List.of(),
                Set.of(),
                DefinitionKey.parse("armor_default", "relicwrought"),
                ItemBaseScaling.defaults(DefinitionKey.parse("armor_default", "relicwrought")),
                Set.of(),
                1
        );
    }
}
