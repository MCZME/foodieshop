package mczme.foodieshop.event;

import java.util.concurrent.CompletableFuture;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.client.entityrenderer.FoodieEntityRenderer;
import mczme.foodieshop.data.language.ZH_CN;
import mczme.foodieshop.data.model.FoodieShopModelProvider;
import mczme.foodieshop.registry.ModEntityTypes;
import mczme.foodieshop.registry.ModMenuTypes;
import mczme.foodieshop.screen.ShopConfigScreen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = FoodieShop.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventClient {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.FOODIE.get(), FoodieEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SHOP_CONFIG_MENU.get(), ShopConfigScreen::new);
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // 语言文件
        generator.addProvider(event.includeClient(), new ZH_CN(output));

        // 模型
        generator.addProvider(event.includeClient(), new FoodieShopModelProvider(output, existingFileHelper));
    }

}
