package mczme.foodieshop.network.packet.c2s;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import mczme.foodieshop.api.shop.ShopConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleBusinessStatusPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ToggleBusinessStatusPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FoodieShop.MODID, "toggle_business_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleBusinessStatusPacket> STREAM_CODEC = CustomPacketPayload.codec(ToggleBusinessStatusPacket::write, ToggleBusinessStatusPacket::new);

    public ToggleBusinessStatusPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleBusinessStatusPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Level level = player.level();
                if (level.isLoaded(packet.pos)) {
                    BlockEntity blockEntity = level.getBlockEntity(packet.pos);
                    if (blockEntity instanceof CashierDeskBlockEntity cashierDesk) {
                        ShopConfig config = cashierDesk.getShopConfig();
                        if (config != null && !config.isModified()) {
                            config.setBusiness(!config.isBusiness());
                            cashierDesk.setChanged();
                            level.sendBlockUpdated(packet.pos, cashierDesk.getBlockState(), cashierDesk.getBlockState(), 3);
                        }
                    }
                }
            }
        });
    }
}
