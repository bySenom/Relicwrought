package io.github.bysenom.relicwrought.equipment;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;

public interface EquipmentStackCodec {
    JsonObject encode(ItemStack stack);

    ItemStack decode(JsonObject json);
}
