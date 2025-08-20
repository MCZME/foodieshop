package mczme.foodieshop.event;

import java.util.concurrent.CompletableFuture;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.data.language.ZH_CN;
import mczme.foodieshop.data.modelprovider.FoodieShopModelProvider;
import mczme.foodieshop.data.tag.ModBlockTagsProvider;
import mczme.foodieshop.data.tag.ModItemTagsProvider;
import mczme.foodieshop.entity.FoodieEntity;
import mczme.foodieshop.network.packet.c2s.ReloadTradingDataPacket;
import mczme.foodieshop.network.packet.c2s.RequestStockContentsPacket;
import mczme.foodieshop.network.packet.c2s.ResetLayoutPacket;
import mczme.foodieshop.network.packet.c2s.UpdateShopConfigPacket;
import mczme.foodieshop.network.packet.s2c.UpdateStockContentsPacket;
import mczme.foodieshop.registry.ModEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(output, lookupProvider,
                blockTagsProvider.contentsGetter(), existingFileHelper));
    }
    
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(FoodieShop.MODID);

        // 注册客户端到服务器的数据包
        registrar.playToServer(
                UpdateShopConfigPacket.TYPE,
                UpdateShopConfigPacket.STREAM_CODEC,
                UpdateShopConfigPacket::handle
        );
        registrar.playToServer(
                ResetLayoutPacket.TYPE,
                ResetLayoutPacket.STREAM_CODEC,
                ResetLayoutPacket::handle
        );
        registrar.playToServer(
                RequestStockContentsPacket.TYPE,
                RequestStockContentsPacket.STREAM_CODEC,
                RequestStockContentsPacket::handle
        );
        registrar.playToServer(
                ReloadTradingDataPacket.TYPE,
                ReloadTradingDataPacket.STREAM_CODEC,
                ReloadTradingDataPacket::handle
        );

        // 注册服务器到客户端的数据包
        registrar.playToClient(
                UpdateStockContentsPacket.TYPE,
                UpdateStockContentsPacket.STREAM_CODEC,
                UpdateStockContentsPacket::handle
        );
    }

    @SubscribeEvent
    public static void onModConfigLoad(final ModConfigEvent.Loading event) {
        // 配置加载事件不适合加载需要 HolderLookup.Provider 的数据
        // 实际的交易数据加载已在 GameEvent.onServerStarting 中处理
    }

    @SubscribeEvent
    public static void onModConfigReload(final ModConfigEvent.Reloading event) {
        // 配置重新加载事件不适合重新加载需要 HolderLookup.Provider 的数据
        // 实际的交易数据重新加载已在 GameEvent.onServerStarting 中处理
    }
}
