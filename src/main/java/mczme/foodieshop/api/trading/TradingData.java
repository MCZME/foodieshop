package mczme.foodieshop.api.trading;

import mczme.foodieshop.api.trading.config.FixedTrade;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradingData {

    private final Map<String, Map<ItemStack, Integer>> sellableItems = new HashMap<>();
    private final Map<String, Map<ItemStack, Integer>> currencyItems = new HashMap<>();
    private final List<FixedTrade> fixedTrades = new ArrayList<>();
    private final List<String> modFolders = new ArrayList<>();

    public Map<String, Map<ItemStack, Integer>> getSellableItems() {
        return sellableItems;
    }

    public Map<String, Map<ItemStack, Integer>> getCurrencyItems() {
        return currencyItems;
    }

    public List<FixedTrade> getFixedTrades() {
        return fixedTrades;
    }

    public List<String> getModFolders() {
        return modFolders;
    }

    public void clear() {
        sellableItems.clear();
        currencyItems.clear();
        fixedTrades.clear();
        modFolders.clear();
    }

    public void addSellableItem(String modId, ItemStack itemStack, int value) {
        sellableItems.computeIfAbsent(modId, k -> new HashMap<>()).put(itemStack, value);
    }

    public void addCurrencyItem(String modId, ItemStack itemStack, int value) {
        currencyItems.computeIfAbsent(modId, k -> new HashMap<>()).put(itemStack, value);
    }

    public void addSellableItem(ItemStack itemStack, int value) {
        addSellableItem("general", itemStack, value);
    }

    public void addCurrencyItem(ItemStack itemStack, int value) {
        addCurrencyItem("general", itemStack, value);
    }

    public void addFixedTrade(FixedTrade trade) {
        fixedTrades.add(trade);
    }

    public void addModData(String modId) {
        if (!modFolders.contains(modId)) {
            modFolders.add(modId);
            sellableItems.computeIfAbsent(modId, k -> new HashMap<>());
            currencyItems.computeIfAbsent(modId, k -> new HashMap<>());
        }
    }
}
