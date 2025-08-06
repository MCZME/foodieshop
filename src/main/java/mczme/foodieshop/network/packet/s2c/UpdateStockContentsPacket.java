package mczme.foodieshop.network.packet.s2c;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.screen.ShopConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record UpdateStockContentsPacket(List<ItemStack> stockContents) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FoodieShop.MODID, "update_stock_contents");
    public static final Type<UpdateStockContentsPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateStockContentsPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.LIST_STREAM_CODEC,
            UpdateStockContentsPacket::stockContents,
            UpdateStockContentsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateStockContentsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ShopConfigScreen shopConfigScreen) {
                shopConfigScreen.updateStockContents(packet.stockContents());
            }
        });
    }
}
