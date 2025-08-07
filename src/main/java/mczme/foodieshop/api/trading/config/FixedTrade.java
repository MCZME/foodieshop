package mczme.foodieshop.api.trading.config;

import com.google.gson.annotations.SerializedName;

public class FixedTrade {
    @SerializedName("food_id")
    private String foodId;

    @SerializedName("reward_item_id")
    private String rewardItemId;

    @SerializedName("count")
    private int count;

    public String getFoodId() {
        return foodId;
    }

    public String getRewardItemId() {
        return rewardItemId;
    }

    public int getCount() {
        return count;
    }
}
