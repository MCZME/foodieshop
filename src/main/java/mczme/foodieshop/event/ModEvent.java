package mczme.foodieshop.event;

import java.util.concurrent.CompletableFuture;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.data.language.ZH_CN;
import mczme.foodieshop.data.modelprovider.FoodieShopModelProvider;
import mczme.foodieshop.data.tag.ModBlockTagsProvider;
import mczme.foodieshop.data.tag.ModItemTagsProvider;
import mczme.foodieshop.entity.FoodieEntity;
import mczme.foodieshop.registry.ModEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = FoodieShop.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEvent {
    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.FOODIE.get(), FoodieEntity.createAttributes().build());
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

        // 标签
        ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(output, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(output, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
    }
    
}
