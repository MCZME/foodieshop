package mczme.foodieshop.api.trading.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import mczme.foodieshop.util.ItemUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FixedTrade {
    @SerializedName("food_id")
    private JsonElement foodId;

    @SerializedName("reward_item_id")
    private JsonElement rewardItemId;

    @SerializedName("count")
    private int count;

    @NotNull
    public ItemStack getFoodItemStack(HolderLookup.Provider registries) {
        if (foodId == null || !foodId.isJsonObject()) {
            // 如果 foodId 不是一个 JSON 对象，尝试将其作为字符串处理（旧格式兼容）
            JsonObject json = new JsonObject();
            json.addProperty("id", foodId.getAsString());
            return ItemUtils.fromJson(json, registries);
        }
        return ItemUtils.fromJson(foodId.getAsJsonObject(), registries);
    }

    @NotNull
    public ItemStack getRewardItemStack(HolderLookup.Provider registries) {
        if (rewardItemId == null || !rewardItemId.isJsonObject()) {
            // 如果 rewardItemId 不是一个 JSON 对象，尝试将其作为字符串处理（旧格式兼容）
            JsonObject json = new JsonObject();
            json.addProperty("id", rewardItemId.getAsString());
            return ItemUtils.fromJson(json, registries);
        }
        return ItemUtils.fromJson(rewardItemId.getAsJsonObject(), registries);
    }

    public int getCount() {
        return count;
    }
}
