package mczme.foodieshop.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mczme.foodieshop.api.shop.SeatInfo;
import mczme.foodieshop.api.shop.ShopConfig;
import mczme.foodieshop.api.shop.TableInfo;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import mczme.foodieshop.registry.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import org.joml.Matrix4f;


public class ShopLayoutRenderer implements BlockEntityRenderer<CashierDeskBlockEntity> {
    private static final float MARKER_SIZE = 0.2f;

    public ShopLayoutRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CashierDeskBlockEntity blockEntity, float partialTick,
                      PoseStack poseStack, MultiBufferSource bufferSource,
                      int packedLight, int packedOverlay) {

        Player player = Minecraft.getInstance().player;
        if (player == null || !isHoldingEditPen(player)) {
            return;
        }

        if (!blockEntity.canEdit(player)) {
            return;
        }

        ShopConfig config = blockEntity.getShopConfig();
        if (config == null) {
            return;
        }
        
        poseStack.pushPose();
        
        BlockPos origin = blockEntity.getBlockPos();
        renderShopArea(config, poseStack, bufferSource, origin);
        renderTables(config, poseStack, bufferSource, origin);
        renderSeats(config, poseStack, bufferSource, origin);
        renderInventory(config, poseStack, bufferSource, origin);
        renderCashier(config, poseStack, bufferSource, origin);
        renderPath(config, poseStack, bufferSource, origin);
        
        poseStack.popPose();
    }

    private boolean isHoldingEditPen(Player player) {
        return player.getMainHandItem().is(ModTags.EDIT_PEN) ||
               player.getOffhandItem().is(ModTags.EDIT_PEN);
    }

    private void renderShopArea(ShopConfig config, PoseStack poseStack,
                               MultiBufferSource bufferSource, BlockPos origin) {
        if (config.getShopAreaPos1() == null || config.getShopAreaPos2() == null) {
            return;
        }

        BlockPos pos1 = config.getShopAreaPos1();
        BlockPos pos2 = config.getShopAreaPos2();

        double minX = Math.min(pos1.getX(), pos2.getX()) - origin.getX();
        double minY = Math.min(pos1.getY(), pos2.getY()) - origin.getY();
        double minZ = Math.min(pos1.getZ(), pos2.getZ()) - origin.getZ();
        double maxX = Math.max(pos1.getX(), pos2.getX()) + 1.0 - origin.getX();
        double maxY = Math.max(pos1.getY(), pos2.getY()) + 1.0 - origin.getY();
        double maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1.0 - origin.getZ();

        VertexConsumer consumer = bufferSource.getBuffer(CustomRenderTypes.LINES_NO_DEPTH);
        LevelRenderer.renderLineBox(poseStack, consumer, minX, minY, minZ, maxX, maxY, maxZ, 1.0F, 0.0F, 0.0F, 1.0F); // 红色
    }

    private void renderTables(ShopConfig config, PoseStack poseStack,
                              MultiBufferSource bufferSource, BlockPos origin) {
        VertexConsumer consumer = bufferSource.getBuffer(CustomRenderTypes.LINES_NO_DEPTH);
        float r = 1.0F, g = 0.5F, b = 0.0F, a = 1.0F; // 橙色
        Matrix4f matrix = poseStack.last().pose();

        for (TableInfo table : config.getTableLocations()) {
            if (table.getRenderEdges() == null) {
                continue;
            }
            for (TableInfo.Edge edge : table.getRenderEdges()) {
                float x1 = edge.from.x() - origin.getX();
                float y1 = edge.from.y() - origin.getY();
                float z1 = edge.from.z() - origin.getZ();
                float x2 = edge.to.x() - origin.getX();
                float y2 = edge.to.y() - origin.getY();
                float z2 = edge.to.z() - origin.getZ();

                consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
                consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
            }
        }
    }

    private void renderSeats(ShopConfig config, PoseStack poseStack,
                            MultiBufferSource bufferSource, BlockPos origin) {
        VertexConsumer consumer = bufferSource.getBuffer(CustomRenderTypes.LINES_NO_DEPTH);
        for (SeatInfo seat : config.getSeatLocations()) {
            BlockPos pos = seat.getLocation();
            renderMarker(poseStack, consumer, pos, 0.0F, 1.0F, 0.0F, origin); // 绿色

            if (seat.isBoundToTable()) {
                TableInfo table = config.getTableById(seat.getBoundTableId());
                if (table != null) {
                    Matrix4f matrix = poseStack.last().pose();
                    float x1 = pos.getX() + 0.5f - origin.getX();
                    float y1 = pos.getY() + 0.5f - origin.getY();
                    float z1 = pos.getZ() + 0.5f - origin.getZ();

                    BlockPos tableCenter = table.getCenter();
                    float x2 = tableCenter.getX() + 0.5f - origin.getX();
                    float y2 = tableCenter.getY() + 0.5f - origin.getY();
                    float z2 = tableCenter.getZ() + 0.5f - origin.getZ();

                    consumer.addVertex(matrix, x1, y1, z1).setColor(0.0F, 1.0F, 0.0F, 1.0F);
                    consumer.addVertex(matrix, x2, y2, z2).setColor(1.0F, 0.5F, 0.0F, 1.0F);
                }
            }
        }
    }

    private void renderInventory(ShopConfig config, PoseStack poseStack,
                                 MultiBufferSource bufferSource, BlockPos origin) {
        VertexConsumer consumer = bufferSource.getBuffer(CustomRenderTypes.LINES_NO_DEPTH);
        for (BlockPos pos : config.getInventoryLocations()) {
            renderMarker(poseStack, consumer, pos, 0.2F, 0.5F, 1.0F, origin); // Yellow
        }
    }

    private void renderCashier(ShopConfig config, PoseStack poseStack,
                               MultiBufferSource bufferSource, BlockPos origin) {
        VertexConsumer consumer = bufferSource.getBuffer(CustomRenderTypes.LINES_NO_DEPTH);
        for (BlockPos pos : config.getDeliveryBoxLocations()) {
            renderMarker(poseStack, consumer, pos, 1.0F, 1.0F, 0.0F, origin); // Light Blue
        }
    }

    private void renderMarker(PoseStack poseStack, VertexConsumer consumer,
                            BlockPos pos, float r, float g, float b, BlockPos origin) {
        double minX = pos.getX() + 0.5 - MARKER_SIZE - origin.getX();
        double minY = pos.getY() + 0.5 - MARKER_SIZE - origin.getY();
        double minZ = pos.getZ() + 0.5 - MARKER_SIZE - origin.getZ();
        double maxX = pos.getX() + 0.5 + MARKER_SIZE - origin.getX();
        double maxY = pos.getY() + 0.5 + MARKER_SIZE - origin.getY();
        double maxZ = pos.getZ() + 0.5 + MARKER_SIZE - origin.getZ();

        LevelRenderer.renderLineBox(poseStack, consumer, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, 1.0F);
    }

    private void renderPathNodeMarker(PoseStack poseStack, VertexConsumer consumer,
                                      BlockPos pos, float r, float g, float b, BlockPos origin) {
        double yCenter = pos.getY() + 1.01f + MARKER_SIZE - origin.getY();
        double xCenter = pos.getX() + 0.5 - origin.getX();
        double zCenter = pos.getZ() + 0.5 - origin.getZ();

        double minX = xCenter - MARKER_SIZE;
        double minY = yCenter - MARKER_SIZE;
        double minZ = zCenter - MARKER_SIZE;
        double maxX = xCenter + MARKER_SIZE;
        double maxY = yCenter + MARKER_SIZE;
        double maxZ = zCenter + MARKER_SIZE;

        LevelRenderer.renderLineBox(poseStack, consumer, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, 1.0F);
    }

    private void renderPath(ShopConfig config, PoseStack poseStack,
                           MultiBufferSource bufferSource, BlockPos origin) {
        if (config.getPathGraph() == null) {
            return;
        }

        VertexConsumer quadConsumer = bufferSource.getBuffer(CustomRenderTypes.SOLID_NO_DEPTH);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        // 将边渲染为实心四边形
        for (java.util.List<BlockPos> edge : config.getPathGraph().getEdges()) {
            if (edge.size() == 2) {
                BlockPos pos1 = edge.get(0);
                BlockPos pos2 = edge.get(1);

                // 路径总是直的，所以X或Z坐标总有一个是相同的。
                float y = pos1.getY() - origin.getY() + 1.01f; // 略高于方块顶面

                float minX, maxX, minZ, maxZ;

                if (pos1.getX() == pos2.getX()) { // 沿Z轴的路径
                    minX = pos1.getX() - origin.getX();
                    maxX = pos1.getX() - origin.getX() + 1.0f;
                    minZ = Math.min(pos1.getZ(), pos2.getZ()) - origin.getZ();
                    maxZ = Math.max(pos1.getZ(), pos2.getZ()) - origin.getZ() + 1.0f;
                } else { // 沿X轴的路径
                    minX = Math.min(pos1.getX(), pos2.getX()) - origin.getX();
                    maxX = Math.max(pos1.getX(), pos2.getX()) - origin.getX() + 1.0f;
                    minZ = pos1.getZ() - origin.getZ();
                    maxZ = pos1.getZ() - origin.getZ() + 1.0f;
                }

                // 定义方块顶部四边形的4个角
                quadConsumer.addVertex(matrix, minX, y, minZ).setColor(0.0F, 0.0F, 1.0F, 0.5F); // 蓝色，半透明
                quadConsumer.addVertex(matrix, minX, y, maxZ).setColor(0.0F, 0.0F, 1.0F, 0.5F);
                quadConsumer.addVertex(matrix, maxX, y, maxZ).setColor(0.0F, 0.0F, 1.0F, 0.5F);
                quadConsumer.addVertex(matrix, maxX, y, minZ).setColor(0.0F, 0.0F, 1.0F, 0.5F);
            }
        }

        VertexConsumer lineConsumer = bufferSource.getBuffer(CustomRenderTypes.LINES_NO_DEPTH);
        // 渲染节点
        for (BlockPos node : config.getPathGraph().getNodes()) {
            renderPathNodeMarker(poseStack, lineConsumer, node, 0.0F, 0.0F, 1.0F, origin); // 蓝色
        }
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
    
    @Override
    public boolean shouldRenderOffScreen(CashierDeskBlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(CashierDeskBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}
