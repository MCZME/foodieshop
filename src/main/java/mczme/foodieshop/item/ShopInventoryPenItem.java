package mczme.foodieshop.item;

import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

public class ShopInventoryPenItem extends ShopEditPenItem {

    private static final String MODE_TAG = "Mode";
    private static final String SELECTED_POS_TAG = "SelectedPos";

    public ShopInventoryPenItem(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult handleShiftRightClickOnBlock(UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResultHolder<ItemStack> handleShiftRightClickInAir(Player player, ItemStack stack) {
        if (!player.level().isClientSide()) {
            int currentMode = getMode(stack);
            int nextMode = (currentMode + 1) % 2;
            setMode(stack, nextMode);
            player.sendSystemMessage(Component.translatable("message.foodieshop.shop_inventory_pen.mode_switched", getModeName(nextMode)));
        }
        return InteractionResultHolder.sidedSuccess(stack, player.level().isClientSide());
    }

    @Override
    protected InteractionResult handleInteraction(Player player, ItemStack stack, CashierDeskBlockEntity cashierDesk, BlockPos clickedPos, Level level) {
        int mode = getMode(stack);
        BlockPos selectedPos = getSelectedPos(stack);

        if (selectedPos != null && selectedPos.equals(clickedPos)) {
            // Remove logic
            if (mode == 0) {
                cashierDesk.removeInventoryPos(clickedPos);
                player.sendSystemMessage(Component.translatable("message.foodieshop.shop_inventory_pen.inventory_pos_removed", clickedPos.toShortString()));
            } else {
                cashierDesk.removeCashBoxPos(clickedPos);
                player.sendSystemMessage(Component.translatable("message.foodieshop.shop_inventory_pen.cash_box_pos_removed", clickedPos.toShortString()));
            }
            clearSelectedPos(stack);
        } else {
            // Add or Select logic
            boolean isAlreadyInList = (mode == 0) ?
                    cashierDesk.getInventoryPos().contains(clickedPos) :
                    cashierDesk.getCashBoxPos().contains(clickedPos);

            if (isAlreadyInList) {
                setSelectedPos(stack, clickedPos);
                player.sendSystemMessage(Component.translatable("message.foodieshop.shop_inventory_pen.pos_selected", clickedPos.toShortString()));
            } else {
                if (mode == 0) {
                    cashierDesk.addInventoryPos(clickedPos, player);
                    player.sendSystemMessage(Component.translatable("message.foodieshop.shop_inventory_pen.inventory_pos_added", clickedPos.toShortString()));
                } else {
                    cashierDesk.addCashBoxPos(clickedPos, player);
                    player.sendSystemMessage(Component.translatable("message.foodieshop.shop_inventory_pen.cash_box_pos_added", clickedPos.toShortString()));
                }
                clearSelectedPos(stack);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private int getMode(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(MODE_TAG);
    }

    private void setMode(ItemStack stack, int mode) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt(MODE_TAG, mode);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private Component getModeName(int mode) {
        return mode == 0 ? Component.translatable("mode.foodieshop.inventory") : Component.translatable("mode.foodieshop.cash_box");
    }

    private void setSelectedPos(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("X", pos.getX());
        posTag.putInt("Y", pos.getY());
        posTag.putInt("Z", pos.getZ());
        tag.put(SELECTED_POS_TAG, posTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private BlockPos getSelectedPos(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(SELECTED_POS_TAG)) {
            CompoundTag posTag = tag.getCompound(SELECTED_POS_TAG);
            return new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));
        }
        return null;
    }

    private void clearSelectedPos(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove(SELECTED_POS_TAG);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
