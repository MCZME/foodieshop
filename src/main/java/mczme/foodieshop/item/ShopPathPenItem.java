package mczme.foodieshop.item;

import mczme.foodieshop.api.shop.PathGraph;
import mczme.foodieshop.block.CashierDeskBlock;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class ShopPathPenItem extends Item {
    public static final String TAG_SHOP_POS = "shop_pos";
    public static final String TAG_CONNECTION_START_NODE = "connection_start_node";

    public ShopPathPenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || context.getHand() != InteractionHand.MAIN_HAND) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();

        // 潜行右键绑定到收银台
        if (player.isShiftKeyDown()) {
            if (level.getBlockState(pos).getBlock() instanceof CashierDeskBlock) {
                if (!level.isClientSide()) {
                    CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    tag.putLong(TAG_SHOP_POS, pos.asLong());
                    itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.bind_success", pos.getX(), pos.getY(), pos.getZ()));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(TAG_SHOP_POS)) {
            BlockPos shopPos = BlockPos.of(tag.getLong(TAG_SHOP_POS));
            if (!level.isClientSide() && level.getBlockEntity(shopPos) instanceof CashierDeskBlockEntity cashierDesk) {
                if (!cashierDesk.isShopAreaSet()) {
                    player.sendSystemMessage(Component.translatable("message.foodieshop.shop_area_not_set"));
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
                handlePathInteraction(player, itemStack, cashierDesk, pos);
                level.sendBlockUpdated(shopPos, level.getBlockState(shopPos), level.getBlockState(shopPos), 3);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
    }

    private void handlePathInteraction(Player player, ItemStack itemStack, CashierDeskBlockEntity cashierDesk, BlockPos clickedPos) {
        if (!cashierDesk.isPosInShopArea(clickedPos)) {
            player.sendSystemMessage(Component.translatable("message.foodieshop.pos_not_in_area"));
            return;
        }
        CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        PathGraph pathGraph = cashierDesk.getShopConfig().getPathGraph();
        Optional<BlockPos> connectionStartNodeOpt = getConnectionStartNode(tag);

        if (connectionStartNodeOpt.isPresent()) {
            BlockPos startNode = connectionStartNodeOpt.get();

            if (startNode.equals(clickedPos)) {
                pathGraph.removeNode(startNode);
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.path_node_removed", startNode.getX(), startNode.getY(), startNode.getZ()));
                clearConnectionStartNode(tag);
            } else if (pathGraph.hasNode(clickedPos)) {
                if (pathGraph.hasEdge(startNode, clickedPos)) {
                    pathGraph.removeEdge(startNode, clickedPos);
                    player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.path_edge_removed", startNode.getX(), startNode.getY(), startNode.getZ(), clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                } else {
                    pathGraph.addEdge(startNode, clickedPos);
                    player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.path_edge_created", startNode.getX(), startNode.getY(), startNode.getZ(), clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                }
                clearConnectionStartNode(tag);
            } else {
                clearConnectionStartNode(tag);
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.path_connection_cancelled"));
            }
        } else {
            if (pathGraph.hasNode(clickedPos)) {
                setConnectionStartNode(tag, clickedPos);
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.path_connection_start", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
            } else {
                pathGraph.addNode(clickedPos);
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.path_node_added", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
            }
        }

        cashierDesk.getShopConfig().setPathGraph(pathGraph);
        cashierDesk.setChanged();
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
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
