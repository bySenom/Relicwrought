package io.github.bysenom.relicwrought.equipment;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemDataCodec;
import io.github.bysenom.relicwrought.item.persistence.ArpgItemStackService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ArpgEquipmentStackCodec implements EquipmentStackCodec {
    private final ArpgItemStackService itemService;

    public ArpgEquipmentStackCodec(ArpgItemStackService itemService) {
        this.itemService = itemService;
    }

    @Override
    public JsonObject encode(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        JsonObject json = new JsonObject();
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        json.addProperty("minecraftItem", itemId.toString());
        json.addProperty("count", stack.getCount());
        if (itemService.hasArpgData(stack)) {
            ArpgItemData data = itemService.read(stack).data().orElse(null);
            if (data != null) {
                json.add("arpgItemData", ArpgItemDataCodec.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow());
            }
        }
        return json;
    }

    @Override
    public ItemStack decode(JsonObject json) {
        String itemId = json.has("minecraftItem") ? json.get("minecraftItem").getAsString() : "minecraft:air";
        int count = json.has("count") ? Math.max(1, json.get("count").getAsInt()) : 1;
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, count);
        if (json.has("arpgItemData") && json.get("arpgItemData").isJsonObject()) {
            ArpgItemData data = ArpgItemDataCodec.CODEC.decode(JsonOps.INSTANCE, json.get("arpgItemData"))
                    .getOrThrow()
                    .getFirst();
            itemService.write(stack, data);
        }
        return stack;
    }
}
