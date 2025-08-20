package mczme.foodieshop.network.packet.c2s;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.api.trading.TradingManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReloadTradingDataPacket() implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FoodieShop.MODID, "reload_trading_data");
    public static final Type<ReloadTradingDataPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadTradingDataPacket> STREAM_CODEC = CustomPacketPayload.codec(ReloadTradingDataPacket::write, ReloadTradingDataPacket::new);

    public ReloadTradingDataPacket(RegistryFriendlyByteBuf buf) {
        this();
    }

    public void write(RegistryFriendlyByteBuf buf) {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReloadTradingDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                TradingManager.reload(player.level().registryAccess());
            }
        });
    }
}
