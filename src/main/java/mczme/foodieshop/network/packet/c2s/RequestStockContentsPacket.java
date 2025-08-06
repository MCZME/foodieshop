package mczme.foodieshop.network.packet.c2s;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import mczme.foodieshop.network.packet.s2c.UpdateStockContentsPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record RequestStockContentsPacket(BlockPos pos) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FoodieShop.MODID, "request_stock_contents");
    public static final Type<RequestStockContentsPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestStockContentsPacket> STREAM_CODEC = CustomPacketPayload.codec(RequestStockContentsPacket::write, RequestStockContentsPacket::new);

    public RequestStockContentsPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestStockContentsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().isLoaded(packet.pos)) {
                    BlockEntity be = player.level().getBlockEntity(packet.pos);
                    if (be instanceof CashierDeskBlockEntity cashierDesk) {
                        Map<Item, Integer> itemCounts = new HashMap<>();
                        
                        // 聚合库存
                        for (BlockPos inventoryPos : cashierDesk.getShopConfig().getInventoryLocations()) {
                            BlockEntity inventoryBE = player.level().getBlockEntity(inventoryPos);
                            if (inventoryBE instanceof RandomizableContainerBlockEntity container) {
                                for (int i = 0; i < container.getContainerSize(); i++) {
                                    ItemStack stack = container.getItem(i);
                                    if (!stack.isEmpty()) {
                                        itemCounts.merge(stack.getItem(), stack.getCount(), Integer::sum);
                                    }
                                }
                            }
                        }
                        // 聚合收货箱
                        for (BlockPos deliveryBoxPos : cashierDesk.getShopConfig().getDeliveryBoxLocations()) {
                            BlockEntity deliveryBE = player.level().getBlockEntity(deliveryBoxPos);
                            if (deliveryBE instanceof RandomizableContainerBlockEntity container) {
                                for (int i = 0; i < container.getContainerSize(); i++) {
                                    ItemStack stack = container.getItem(i);
                                    if (!stack.isEmpty()) {
                                        itemCounts.merge(stack.getItem(), stack.getCount(), Integer::sum);
                                    }
                                }
                            }
                        }

                        List<ItemStack> aggregatedStacks = new ArrayList<>();
                        for (Map.Entry<Item, Integer> entry : itemCounts.entrySet()) {
                            aggregatedStacks.add(new ItemStack(entry.getKey(), entry.getValue()));
                        }

                        PacketDistributor.sendToPlayer(player, new UpdateStockContentsPacket(aggregatedStacks));
                    }
                }
            }
        });
    }
}
