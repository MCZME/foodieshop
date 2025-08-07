package mczme.foodieshop.api.trading;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.api.trading.config.FixedTrade;
import mczme.foodieshop.api.trading.config.RewardItem;
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

public class TradingManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final File CONFIG_DIR = new File("config/" + FoodieShop.MODID + "/trading");

    private static final Map<String, String> foodValueTiers = new HashMap<>();
    private static final Map<String, RewardItem> rewardItems = new HashMap<>();
    private static final List<String> tiers = new ArrayList<>();
    private static final List<FixedTrade> fixedTrades = new ArrayList<>();

    public static void load() {
        if (!CONFIG_DIR.exists() && !CONFIG_DIR.mkdirs()) {
            LOGGER.error("无法创建交易配置目录: " + CONFIG_DIR.getAbsolutePath());
            return;
        }
        
        clearConfigs();
        LOGGER.info("开始从 JSON 文件加载 FoodieShop 交易数据...");

        createExampleDirectory();

        loadTiers();
        loadFoodValueTiers();
        loadRewardItems();
        loadFixedTrades();
        
        LOGGER.info("交易数据加载完成。");
    }

    public static void reload() {
        if (!CONFIG_DIR.exists() && !CONFIG_DIR.mkdirs()) {
            LOGGER.error("无法创建交易配置目录: " + CONFIG_DIR.getAbsolutePath());
            return;
        }
        
        clearConfigs();
        LOGGER.info("开始从 JSON 文件重新加载 FoodieShop 交易数据...");

        loadTiers();
        loadFoodValueTiers();
        loadRewardItems();
        loadFixedTrades();
        
        LOGGER.info("交易数据重新加载完成。");
    }

    private static void clearConfigs() {
        foodValueTiers.clear();
        rewardItems.clear();
        tiers.clear();
        fixedTrades.clear();
    }

    private static void createExampleDirectory() {
        File exampleDir = new File(CONFIG_DIR, "minecraft");
        if (!exampleDir.exists()) {
            exampleDir.mkdirs();
            // 在示例目录中创建文件，以展示覆盖功能
            loadValueTiersFromFile(new File(exampleDir, "food_value_tiers.json"));
            loadRewardItemsFromFile(new File(exampleDir, "reward_items.json"));
        }
    }

    private static void loadTiers() {
        File tiersFile = new File(CONFIG_DIR, "tiers.json");
        if (!tiersFile.exists()) {
            try (FileWriter writer = new FileWriter(tiersFile)) {
                GSON.toJson(List.of("LOW", "MEDIUM", "HIGH"), writer);
                LOGGER.info("创建默认 tiers.json 文件。");
            } catch (IOException e) {
                LOGGER.error("无法创建默认的 tiers.json", e);
            }
        }

        try (FileReader reader = new FileReader(tiersFile)) {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> loadedTiers = GSON.fromJson(reader, type);
            if (loadedTiers != null) {
                tiers.addAll(loadedTiers);
            }
        } catch (IOException e) {
            LOGGER.error("无法加载 tiers.json", e);
        }
    }

    private static void loadFoodValueTiers() {
        // 加载主文件
        loadValueTiersFromFile(new File(CONFIG_DIR, "food_value_tiers.json"));

        // 加载并覆盖模组专属文件
        File[] subDirs = CONFIG_DIR.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                File modSpecificFile = new File(subDir, "food_value_tiers.json");
                if (modSpecificFile.exists()) {
                    loadValueTiersFromFile(modSpecificFile);
                }
            }
        }
    }

    private static void loadValueTiersFromFile(File file) {
        if (!file.exists()) {
            // 为不同的文件创建不同的默认内容
            Map<String, String> defaultContent = new HashMap<>();
            if (file.getParentFile().getName().equals("minecraft")) {
                defaultContent.put("minecraft:cooked_beef", "MEDIUM");
            } else {
                defaultContent.put("minecraft:apple", "LOW");
                defaultContent.put("minecraft:golden_apple", "HIGH");
            }
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(defaultContent, writer);
                LOGGER.info("创建默认 " + file.getName() + " 文件于 " + file.getParent());
            } catch (IOException e) {
                LOGGER.error("无法创建默认的 " + file.getName(), e);
            }
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> loadedTiers = GSON.fromJson(reader, type);
            if (loadedTiers != null) {
                foodValueTiers.putAll(loadedTiers);
            }
        } catch (IOException e) {
            LOGGER.error("无法加载 " + file.getName(), e);
        }
    }

    private static void loadRewardItems() {
        // 加载主文件
        loadRewardItemsFromFile(new File(CONFIG_DIR, "reward_items.json"));

        // 加载并覆盖模组专属文件
        File[] subDirs = CONFIG_DIR.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                File modSpecificFile = new File(subDir, "reward_items.json");
                if (modSpecificFile.exists()) {
                    loadRewardItemsFromFile(modSpecificFile);
                }
            }
        }
    }

    private static void loadRewardItemsFromFile(File file) {
        if (!file.exists()) {
            Map<String, Map<String, Object>> defaultItems = new HashMap<>();
            if (file.getParentFile().getName().equals("minecraft")) {
                Map<String, Object> ironIngot = new HashMap<>();
                ironIngot.put("value_score", 20);
                defaultItems.put("minecraft:iron_ingot", ironIngot);
            } else {
                Map<String, Object> diamond = new HashMap<>();
                diamond.put("value_score", 100);
                defaultItems.put("minecraft:diamond", diamond);
            }
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(defaultItems, writer);
                LOGGER.info("创建默认 " + file.getName() + " 文件于 " + file.getParent());
            } catch (IOException e) {
                LOGGER.error("无法创建默认的 " + file.getName(), e);
            }
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, RewardItem>>() {}.getType();
            Map<String, RewardItem> loadedItems = GSON.fromJson(reader, type);
            if (loadedItems != null) {
                rewardItems.putAll(loadedItems);
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

    public static Map<String, String> getFoodValueTiers() {
        return foodValueTiers;
    }

    public static Map<String, RewardItem> getRewardItems() {
        return rewardItems;
    }

    public static List<String> getTiers() {
        return tiers;
    }

    public static List<FixedTrade> getFixedTrades() {
        return fixedTrades;
    }
}
