package mczme.foodieshop.network.packet.c2s;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.api.shop.ShopConfig;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ResetLayoutPacket(BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FoodieShop.MODID, "reset_layout");
    public static final Type<ResetLayoutPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ResetLayoutPacket> STREAM_CODEC = CustomPacketPayload.codec(ResetLayoutPacket::write, ResetLayoutPacket::new);

    public ResetLayoutPacket(final RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    public void write(final RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ResetLayoutPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();
            if (level.isLoaded(packet.pos)) {
                BlockEntity blockEntity = level.getBlockEntity(packet.pos);
                if (blockEntity instanceof CashierDeskBlockEntity cashierDesk) {
                    if (cashierDesk.canEdit(player)) {
                        ShopConfig config = cashierDesk.getShopConfig();
                        config.getSeatLocations().clear();
                        config.getTableLocations().clear();
                        if (config.getPathGraph() != null) {
                            config.getPathGraph().getNodes().clear();
                            config.getPathGraph().getEdges().clear();
                        }
                        cashierDesk.setChanged();
                        level.sendBlockUpdated(packet.pos, level.getBlockState(packet.pos), level.getBlockState(packet.pos), 3);
                    }
                }
            }
        });
    }
}
