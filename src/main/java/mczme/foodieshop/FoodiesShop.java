package mczme.foodieshop;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import mczme.foodieshop.registry.ModEntityTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(FoodiesShop.MODID)
public class FoodiesShop {
    public static final String MODID = "foodieshop";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public FoodiesShop(IEventBus modEventBus, ModContainer modContainer) {
        ModEntityTypes.register(modEventBus);
    }
}
