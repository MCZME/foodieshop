package mczme.foodieshop;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import mczme.foodieshop.registry.ModBlockEntities;
import mczme.foodieshop.registry.ModBlocks;
import mczme.foodieshop.registry.ModCreativeTabs;
import mczme.foodieshop.registry.ModEntityTypes;
import mczme.foodieshop.registry.ModItems;
import mczme.foodieshop.config.ServerConfig;
import mczme.foodieshop.config.CommonConfig;
import mczme.foodieshop.registry.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(FoodieShop.MODID)
public class FoodieShop {
    public static final String MODID = "foodieshop";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public FoodieShop(IEventBus modEventBus, ModContainer modContainer) {
        ModEntityTypes.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "foodieshop-trading.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, "foodieshop-trading-server.toml");
    }
}
