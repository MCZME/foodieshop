package mczme.foodieshop.network.packet.c2s;

import mczme.foodieshop.api.shop.ShopConfig;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import mczme.foodieshop.FoodieShop;

/**
 * 一个从客户端发送到服务器的数据包，用于更新收银台的 {@link ShopConfig}。
 *
 * @param pos    收银台方块实体的坐标
 * @param config 要应用的新 {@link ShopConfig}
 */
public record UpdateShopConfigPacket(BlockPos pos, ShopConfig config) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FoodieShop.MODID, "update_shop_config");
    public static final Type<UpdateShopConfigPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateShopConfigPacket> STREAM_CODEC = CustomPacketPayload.codec(UpdateShopConfigPacket::write, UpdateShopConfigPacket::new);

    public UpdateShopConfigPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos(), ShopConfig.fromNbt(buf.readNbt()));
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeNbt(this.config.toNbt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateShopConfigPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().isLoaded(packet.pos)) {
                    BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
                    if (blockEntity instanceof CashierDeskBlockEntity cashierDesk) {
                        // 基础验证，确保玩家有权限编辑
                        if (cashierDesk.canEdit(player)) {
                            cashierDesk.setShopConfig(packet.config);
                        }
                    }
                }
            }
        });
    }
}
