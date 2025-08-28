package mczme.foodieshop.network;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.network.packet.c2s.ReloadTradingDataPacket;
import mczme.foodieshop.network.packet.c2s.RequestStockContentsPacket;
import mczme.foodieshop.network.packet.c2s.ResetLayoutPacket;
import mczme.foodieshop.network.packet.c2s.ToggleBusinessStatusPacket;
import mczme.foodieshop.network.packet.c2s.TogglePathNodeModePacket;
import mczme.foodieshop.network.packet.c2s.UpdateShopConfigPacket;
import mczme.foodieshop.network.packet.c2s.ValidateShopPacket;
import mczme.foodieshop.network.packet.s2c.UpdateStockContentsPacket;
import mczme.foodieshop.network.packet.s2c.ValidateShopResultPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = FoodieShop.MODID, bus = EventBusSubscriber.Bus.MOD)
public class PacketRegister {

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
        registrar.playToServer(
                TogglePathNodeModePacket.TYPE,
                TogglePathNodeModePacket.STREAM_CODEC,
                TogglePathNodeModePacket::handle
        );
        registrar.playToServer(
                ValidateShopPacket.TYPE,
                ValidateShopPacket.STREAM_CODEC,
                ValidateShopPacket::handle
        );
        registrar.playToServer(
                ToggleBusinessStatusPacket.TYPE,
                ToggleBusinessStatusPacket.STREAM_CODEC,
                ToggleBusinessStatusPacket::handle
        );

        // 注册服务器到客户端的数据包
        registrar.playToClient(
                UpdateStockContentsPacket.TYPE,
                UpdateStockContentsPacket.STREAM_CODEC,
                UpdateStockContentsPacket::handle
        );
        registrar.playToClient(
                ValidateShopResultPacket.TYPE,
                ValidateShopResultPacket.STREAM_CODEC,
                ValidateShopResultPacket::handle
        );
    }

}
