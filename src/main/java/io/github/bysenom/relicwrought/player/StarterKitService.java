package io.github.bysenom.relicwrought.player;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.generation.ArpgItemGenerator;
import io.github.bysenom.relicwrought.item.generation.ItemGenerationRequest;
import io.github.bysenom.relicwrought.item.generation.ItemGenerationResult;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class StarterKitService {
    private final ArpgItemGenerator itemGenerator;

    public StarterKitService(ArpgItemGenerator itemGenerator) {
        this.itemGenerator = itemGenerator;
    }

    public KitGrantResult grantKit(ServerPlayer player, StarterKitDefinition kit) {
        int inserted = 0;
        int equipped = 0;
        int dropped = 0;
        List<String> failed = new ArrayList<>();

        for (StarterKitEntry entry : kit.entries()) {
            try {
                ItemStack stack = createStarterItem(entry, player.getUUID(), kit.id().toString());
                if (stack.isEmpty()) {
                    failed.add("Failed to create item for: " + entry.itemBaseId());
                    continue;
                }

                if (entry.autoEquip()) {
                    EquipmentSlot slot = resolveSlot(entry.slot());
                    if (slot != null && !player.getItemBySlot(slot).is(Items.AIR)) {
                        if (!player.getInventory().add(stack)) {
                            dropItem(player, stack);
                            dropped++;
                        } else {
                            inserted++;
                        }
                    } else if (slot != null) {
                        player.setItemSlot(slot, stack);
                        equipped++;
                    } else {
                        if (!player.getInventory().add(stack)) {
                            dropItem(player, stack);
                            dropped++;
                        } else {
                            inserted++;
                        }
                    }
                } else {
                    if (!player.getInventory().add(stack)) {
                        dropItem(player, stack);
                        dropped++;
                    } else {
                        inserted++;
                    }
                }
            } catch (Exception e) {
                Relicwrought.LOGGER.warn("Failed to grant kit item {}: {}", entry.itemBaseId(), e.getMessage());
                failed.add(entry.itemBaseId() + ": " + e.getMessage());
            }
        }

        return new KitGrantResult(inserted, equipped, dropped, failed.size(), failed);
    }

    private ItemStack createStarterItem(StarterKitEntry entry, UUID playerUuid, String kitId) {
        long seed = playerUuid.getMostSignificantBits()
                ^ playerUuid.getLeastSignificantBits()
                ^ kitId.hashCode()
                ^ (long) entry.itemBaseId().hashCode() << 16
                ^ entry.itemLevel();

        DefinitionKey baseKey = DefinitionKey.parse(entry.itemBaseId(), Relicwrought.MOD_ID);
        ItemLevel level = ItemLevel.of(entry.itemLevel());

        ItemGenerationRequest request = new ItemGenerationRequest(
                baseKey, level, seed, null, entry.quality(),
                null, null, null, null,
                null, false, "starter_kit:" + kitId
        );

        ItemGenerationResult result = itemGenerator.generate(request);
        if (!result.success() || result.itemStack() == null) {
            Relicwrought.LOGGER.warn("Failed to generate starter item {}: {}", entry.itemBaseId(), result.errorCode());
            return ItemStack.EMPTY;
        }

        ItemStack stack = result.itemStack();

        var component = stack.get(io.github.bysenom.relicwrought.item.persistence.ArpgItemComponents.ARPG_ITEM_DATA);
        if (component != null) {
            var data = component.data();
            var modifiedData = new io.github.bysenom.relicwrought.item.model.ArpgItemData(
                    data.dataVersion(), data.itemId(), data.itemBaseId(),
                    data.itemLevel(), data.requiredCharacterLevel(), data.rarity(),
                    data.quality(), data.seed(), true,
                    data.implicitAffixes(), data.prefixes(), data.suffixes()
            );
            stack.set(io.github.bysenom.relicwrought.item.persistence.ArpgItemComponents.ARPG_ITEM_DATA,
                    new io.github.bysenom.relicwrought.item.persistence.ArpgItemComponent(modifiedData));
        }

        return stack;
    }

    private void dropItem(ServerPlayer player, ItemStack stack) {
        var entity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
        entity.setNoPickUpDelay();
        player.level().addFreshEntity(entity);
    }

    private EquipmentSlot resolveSlot(String slot) {
        if (slot == null || slot.isBlank()) return null;
        return switch (slot.toLowerCase()) {
            case "head", "helmet" -> EquipmentSlot.HEAD;
            case "chest", "chestplate" -> EquipmentSlot.CHEST;
            case "legs", "leggings" -> EquipmentSlot.LEGS;
            case "feet", "boots" -> EquipmentSlot.FEET;
            case "mainhand", "weapon", "main_hand" -> EquipmentSlot.MAINHAND;
            case "offhand", "shield", "off_hand" -> EquipmentSlot.OFFHAND;
            default -> null;
        };
    }

    public record KitGrantResult(
            int inserted,
            int equipped,
            int dropped,
            int failed,
            List<String> failureMessages
    ) {
        public boolean completeSuccess() {
            return failed == 0;
        }

        public int totalItems() {
            return inserted + equipped + dropped + failed;
        }
    }
}
