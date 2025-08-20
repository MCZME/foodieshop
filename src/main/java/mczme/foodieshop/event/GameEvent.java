package mczme.foodieshop.event;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.api.trading.TradingManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@EventBusSubscriber(modid = FoodieShop.MODID)
public class GameEvent {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        TradingManager.load(event.getServer().registryAccess());
    }
}
