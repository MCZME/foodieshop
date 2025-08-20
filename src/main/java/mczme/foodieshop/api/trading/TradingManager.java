package mczme.foodieshop.api.trading;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.api.trading.config.FixedTrade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import mczme.foodieshop.api.trading.config.ItemValue;

public class TradingManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final File CONFIG_DIR = new File("config/" + FoodieShop.MODID + "/trading");

    private static final Map<String, Map<ItemStack, Integer>> sellableItems = new HashMap<>();
    private static final Map<String, Map<ItemStack, Integer>> currencyItems = new HashMap<>();
    private static final List<FixedTrade> fixedTrades = new ArrayList<>();
    private static final List<String> modFolders = new ArrayList<>();

    public static void load(HolderLookup.Provider registries) {
        if (!CONFIG_DIR.exists() && !CONFIG_DIR.mkdirs()) {
            LOGGER.error("无法创建交易配置目录: " + CONFIG_DIR.getAbsolutePath());
            return;
        }
        
        clearConfigs();
        LOGGER.info("开始从 JSON 文件加载 FoodieShop 交易数据...");

        createExampleDirectory(registries);

        loadGeneralConfigs(registries);
        loadModSpecificConfigs(registries);
        loadFixedTrades();
        
        LOGGER.info("交易数据加载完成。");
    }

    public static void reload(HolderLookup.Provider registries) {
        if (!CONFIG_DIR.exists() && !CONFIG_DIR.mkdirs()) {
            LOGGER.error("无法创建交易配置目录: " + CONFIG_DIR.getAbsolutePath());
            return;
        }
        
        clearConfigs();
        LOGGER.info("开始从 JSON 文件重新加载 FoodieShop 交易数据...");

        loadGeneralConfigs(registries);
        loadModSpecificConfigs(registries);
        loadFixedTrades();
        
        LOGGER.info("交易数据重新加载完成。");
    }

    private static void clearConfigs() {
        sellableItems.clear();
        currencyItems.clear();
        fixedTrades.clear();
        modFolders.clear();
    }

    private static void createExampleDirectory(HolderLookup.Provider registries) {
        File exampleDir = new File(CONFIG_DIR, "minecraft");
        if (!exampleDir.exists()) {
            exampleDir.mkdirs();
            // 在示例目录中创建文件，以展示覆盖功能
            loadItemValueFromFile(new File(exampleDir, "sellable_items.json"), "minecraft", sellableItems, registries);
            loadItemValueFromFile(new File(exampleDir, "currency_items.json"), "minecraft", currencyItems, registries);
        }
    }

    private static void loadGeneralConfigs(HolderLookup.Provider registries) {
        loadItemValueFromFile(new File(CONFIG_DIR, "general_sellable_items.json"), "general", sellableItems, registries);
        loadItemValueFromFile(new File(CONFIG_DIR, "general_currency_items.json"), "general", currencyItems, registries);
    }

    private static void loadModSpecificConfigs(HolderLookup.Provider registries) {
        File[] subDirs = CONFIG_DIR.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                String modId = subDir.getName();
                modFolders.add(modId);
                File modSellableFile = new File(subDir, "sellable_items.json");
                if (modSellableFile.exists()) {
                    loadItemValueFromFile(modSellableFile, modId, sellableItems, registries);
                }
                File modCurrencyFile = new File(subDir, "currency_items.json");
                if (modCurrencyFile.exists()) {
                    loadItemValueFromFile(modCurrencyFile, modId, currencyItems, registries);
                }
            }
        }
    }

    private static void loadItemValueFromFile(File file, String modId, Map<String, Map<ItemStack, Integer>> targetMap, HolderLookup.Provider registries) {
        if (!file.exists()) {
            List<ItemValue> defaultContent = new ArrayList<>();
            if (file.getName().equals("general_sellable_items.json")) {
                defaultContent.add(new ItemValue(GSON.toJsonTree("minecraft:apple"), 10));
                defaultContent.add(new ItemValue(GSON.toJsonTree("minecraft:cooked_beef"), 20));
            } else if (file.getName().equals("general_currency_items.json")) {
                defaultContent.add(new ItemValue(GSON.toJsonTree("minecraft:gold_ingot"), 50));
            } else if (file.getName().equals("sellable_items.json") && file.getParentFile().getName().equals("minecraft")) {
                defaultContent.add(new ItemValue(GSON.toJsonTree("minecraft:diamond"), 100));
            } else if (file.getName().equals("currency_items.json") && file.getParentFile().getName().equals("minecraft")) {
                defaultContent.add(new ItemValue(GSON.toJsonTree("minecraft:emerald"), 200));
            }
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(defaultContent, writer);
                LOGGER.info("创建默认 " + file.getName() + " 文件于 " + file.getParent());
            } catch (IOException e) {
                LOGGER.error("无法创建默认的 " + file.getName(), e);
            }
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<List<ItemValue>>() {}.getType();
            List<ItemValue> loadedItemValues = GSON.fromJson(reader, type);
            if (loadedItemValues != null) {
                Map<ItemStack, Integer> modSpecificMap = targetMap.computeIfAbsent(modId, k -> new HashMap<>());
                for (ItemValue itemValue : loadedItemValues) {
                    modSpecificMap.put(itemValue.getItemStack(registries), itemValue.getValue());
                }
            }
        } catch (IOException e) {
            LOGGER.error("无法加载 " + file.getName(), e);
        }
    }

    private static void loadFixedTrades() {
        File fixedTradesFile = new File(CONFIG_DIR, "fixed_trades.json");
        if (!fixedTradesFile.exists()) {
            try (FileWriter writer = new FileWriter(fixedTradesFile)) {
                GSON.toJson(new ArrayList<>(), writer);
                LOGGER.info("创建默认 fixed_trades.json 文件。");
            } catch (IOException e) {
                LOGGER.error("无法创建默认的 fixed_trades.json", e);
            }
        }

        try (FileReader reader = new FileReader(fixedTradesFile)) {
            Type type = new TypeToken<List<FixedTrade>>() {}.getType();
            List<FixedTrade> loadedTrades = GSON.fromJson(reader, type);
            if (loadedTrades != null) {
                fixedTrades.addAll(loadedTrades);
            }
        } catch (IOException e) {
            LOGGER.error("无法加载 fixed_trades.json", e);
        }
    }

    public static Map<ItemStack, Integer> getSellableItems(String modId) {
        return sellableItems.getOrDefault(modId, new HashMap<>());
    }

    public static Map<ItemStack, Integer> getCurrencyItems(String modId) {
        return currencyItems.getOrDefault(modId, new HashMap<>());
    }

    public static Map<ItemStack, Integer> getAllSellableItems() {
        Map<ItemStack, Integer> allItems = new HashMap<>();
        sellableItems.values().forEach(allItems::putAll);
        return allItems;
    }

    public static Map<ItemStack, Integer> getAllCurrencyItems() {
        Map<ItemStack, Integer> allItems = new HashMap<>();
        currencyItems.values().forEach(allItems::putAll);
        return allItems;
    }

    public static List<FixedTrade> getFixedTrades() {
        return fixedTrades;
    }

    public static List<String> getModFolders() {
        return modFolders;
    }
}
