package mczme.foodieshop.api.trading.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import mczme.foodieshop.util.ItemUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemValue {
    @SerializedName("item")
    private JsonElement itemJson;

    @SerializedName("value")
    private int value;

    public ItemValue() {}

    public ItemValue(JsonElement itemJson, int value) {
        this.itemJson = itemJson;
        this.value = value;
    }

    @NotNull
    public ItemStack getItemStack(HolderLookup.Provider registries) {
        if (itemJson == null) {
            return ItemStack.EMPTY;
        }
        if (!itemJson.isJsonObject()) {
            JsonObject json = new JsonObject();
            json.addProperty("id", itemJson.getAsString());
            return ItemUtils.fromJson(json, registries);
        }
        return ItemUtils.fromJson(itemJson.getAsJsonObject(), registries);
    }

    public int getValue() {
        return value;
    }
}
