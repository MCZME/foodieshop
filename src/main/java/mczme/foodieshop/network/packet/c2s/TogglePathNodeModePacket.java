package mczme.foodieshop.network.packet.c2s;

import mczme.foodieshop.api.shop.ShopConfig;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TogglePathNodeModePacket(BlockPos cashierDeskPos, BlockPos nodePos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TogglePathNodeModePacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath("foodieshop", "toggle_path_node_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TogglePathNodeModePacket> STREAM_CODEC = CustomPacketPayload.codec(TogglePathNodeModePacket::write, TogglePathNodeModePacket::new);


    public TogglePathNodeModePacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readBlockPos());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(cashierDeskPos);
        buf.writeBlockPos(nodePos);
    }

    @Override
    public CustomPacketPayload.Type<TogglePathNodeModePacket> type() {
        return TYPE;
    }

    public static void handle(TogglePathNodeModePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();
            if (level.isLoaded(packet.cashierDeskPos)) {
                BlockEntity blockEntity = level.getBlockEntity(packet.cashierDeskPos);
                if (blockEntity instanceof CashierDeskBlockEntity cashierDesk) {
                    if (cashierDesk.canEdit(player)) {
                        ShopConfig shopConfig = cashierDesk.getShopConfig();
                        shopConfig.togglePathNodeMode(packet.nodePos);
                        cashierDesk.setShopConfig(shopConfig);
                        // 强制更新到客户端
                        level.sendBlockUpdated(packet.cashierDeskPos, cashierDesk.getBlockState(), cashierDesk.getBlockState(), 3);
                    }
                }
            }
        });
    }
}
