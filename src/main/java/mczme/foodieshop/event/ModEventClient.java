package mczme.foodieshop.event;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.client.entityrenderer.FoodieEntityRenderer;
import mczme.foodieshop.client.renderer.ShopLayoutRenderer;
import mczme.foodieshop.registry.ModBlockEntities;
import mczme.foodieshop.registry.ModEntityTypes;
import mczme.foodieshop.registry.ModMenuTypes;
import mczme.foodieshop.screen.ShopConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = FoodieShop.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventClient {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.FOODIE.get(), FoodieEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CASHIER_DESK_BE.get(), ShopLayoutRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SHOP_CONFIG_MENU.get(), ShopConfigScreen::new);
    }

}
