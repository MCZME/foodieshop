package mczme.foodieshop.item;

import mczme.foodieshop.api.shop.PathGraph;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class ShopPathPenItem extends ShopEditPenItem {
    public static final String TAG_CONNECTION_START_NODE = "connection_start_node";

    public ShopPathPenItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void onShopPosSet(CompoundTag tag, Player player) {
        clearConnectionStartNode(tag);
    }

    @Override
    protected InteractionResult handleInteraction(Player player, ItemStack stack, CashierDeskBlockEntity cashierDesk, BlockPos clickedPos, Level level) {
        BlockPos targetPos = clickedPos.above(); // 默认选择方块上面的位置

        if (!cashierDesk.isShopAreaSet()) {
            player.sendSystemMessage(Component.translatable("message.foodieshop.shop_area_not_set"));
            return InteractionResult.FAIL;
        }
        if (!cashierDesk.isPosInShopArea(targetPos)) { // 使用 targetPos
            player.sendSystemMessage(Component.translatable("message.foodieshop.pos_not_in_area"));
            return InteractionResult.FAIL;
        }

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        PathGraph pathGraph = cashierDesk.getShopConfig().getPathGraph();
        Optional<BlockPos> connectionStartNodeOpt = getConnectionStartNode(tag);

        if (connectionStartNodeOpt.isPresent()) {
            handleInteractionWithSelection(player, tag, pathGraph, targetPos, connectionStartNodeOpt.get()); // 使用 targetPos
        } else {
            handleInteractionWithoutSelection(player, tag, pathGraph, targetPos, cashierDesk); // 使用 targetPos
        }

        cashierDesk.getShopConfig().setPathGraph(pathGraph);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return InteractionResult.SUCCESS;
    }

    private void handleInteractionWithSelection(Player player, CompoundTag tag, PathGraph pathGraph, BlockPos targetPos, BlockPos startNode) { // 使用 targetPos
        if (startNode.equals(targetPos)) { // 使用 targetPos
            pathGraph.removeNode(startNode);
            player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.path_node_removed", startNode.getX(), startNode.getY(), startNode.getZ()));
        } else if (pathGraph.hasNode(targetPos)) { // 使用 targetPos
            if (pathGraph.hasEdge(startNode, targetPos)) { // 使用 targetPos
                pathGraph.removeEdge(startNode, targetPos); // 使用 targetPos
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.path_edge_removed", startNode.getX(), startNode.getY(), startNode.getZ(), targetPos.getX(), targetPos.getY(), targetPos.getZ())); // 使用 targetPos
            } else {
                pathGraph.addEdge(startNode, targetPos); // 使用 targetPos
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.path_edge_created", startNode.getX(), startNode.getY(), startNode.getZ(), targetPos.getX(), targetPos.getY(), targetPos.getZ())); // 使用 targetPos
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.path_connection_cancelled"));
        }
        clearConnectionStartNode(tag);
    }

    private void handleInteractionWithoutSelection(Player player, CompoundTag tag, PathGraph pathGraph, BlockPos targetPos, CashierDeskBlockEntity cashierDesk) { // 使用 targetPos
        if (pathGraph.hasNode(targetPos)) { // 使用 targetPos
            setConnectionStartNode(tag, targetPos); // 使用 targetPos
            player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.path_connection_start", targetPos.getX(), targetPos.getY(), targetPos.getZ())); // 使用 targetPos
        } else {
            if (cashierDesk.getShopConfig().isPositionOccupied(targetPos)) { // 使用 targetPos
                player.sendSystemMessage(Component.translatable("message.foodieshop.pos_already_occupied"));
                return;
            }
            pathGraph.addNode(targetPos); // 使用 targetPos
            player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.path_node_added", targetPos.getX(), targetPos.getY(), targetPos.getZ())); // 使用 targetPos
        }
    }

    private void setConnectionStartNode(CompoundTag tag, BlockPos pos) {
        tag.putLong(TAG_CONNECTION_START_NODE, pos.asLong());
    }

    private void clearConnectionStartNode(CompoundTag tag) {
        tag.remove(TAG_CONNECTION_START_NODE);
    }

    private Optional<BlockPos> getConnectionStartNode(CompoundTag tag) {
        if (tag.contains(TAG_CONNECTION_START_NODE)) {
            return Optional.of(BlockPos.of(tag.getLong(TAG_CONNECTION_START_NODE)));
        }
        return Optional.empty();
    }
}
