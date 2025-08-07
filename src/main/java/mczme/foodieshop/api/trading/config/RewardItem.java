package mczme.foodieshop.api.trading.config;

import com.google.gson.annotations.SerializedName;

public class RewardItem {
    @SerializedName("item_id")
    private String itemId;

    @SerializedName("value_score")
    private int valueScore;

    public String getItemId() {
        return itemId;
    }

    public int getValueScore() {
        return valueScore;
    }
}
