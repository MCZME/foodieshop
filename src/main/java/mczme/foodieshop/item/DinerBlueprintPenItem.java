package mczme.foodieshop.item;

import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class DinerBlueprintPenItem extends ShopEditPenItem {
    public static final String TAG_SETUP_MODE = "setup_mode";
    public static final String TAG_SELECTED_POS = "selected_pos";

    public DinerBlueprintPenItem(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult handleInteraction(Player player, ItemStack stack, CashierDeskBlockEntity cashierDesk, BlockPos clickedPos, Level level) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        SetupMode currentMode = SetupMode.valueOf(tag.getString(TAG_SETUP_MODE).isEmpty() ? SetupMode.SHOP_AREA.name() : tag.getString(TAG_SETUP_MODE));

        if (currentMode != SetupMode.SHOP_AREA && !cashierDesk.isShopAreaSet()) {
            player.sendSystemMessage(Component.translatable("message.foodieshop.shop_area_not_set"));
            return InteractionResult.FAIL;
        }

        Optional<BlockPos> selectedPosOpt = getSelectedPos(tag);

        if (currentMode == SetupMode.SHOP_AREA) {
            handleShopAreaInteraction(player, cashierDesk, clickedPos);
        } else if (selectedPosOpt.isPresent()) {
            handleInteractionWithSelection(player, tag, cashierDesk, clickedPos, currentMode, selectedPosOpt.get());
        } else {
            handleInteractionWithoutSelection(player, tag, cashierDesk, clickedPos, currentMode);
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return InteractionResult.SUCCESS;
    }

    private void handleInteractionWithSelection(Player player, CompoundTag tag, CashierDeskBlockEntity cashierDesk, BlockPos clickedPos, SetupMode mode, BlockPos selectedPos) {
        if (selectedPos.equals(clickedPos)) {
            // Remove
            if (mode == SetupMode.SEAT && cashierDesk.removeSeat(clickedPos)) {
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.remove_seat", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
            } else if (mode == SetupMode.TABLE && cashierDesk.removeTable(clickedPos)) {
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.remove_table", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
            }
        } else {
            // Operate
            if (mode == SetupMode.SEAT && cashierDesk.getTableAt(clickedPos).isPresent()) {
                if (cashierDesk.bindSeatToTable(selectedPos, clickedPos, player)) {
                    player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.seat_bound_to_table", selectedPos.getX(), selectedPos.getY(), selectedPos.getZ(), clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                }
            } else if (mode == SetupMode.TABLE && cashierDesk.getTableAt(clickedPos).isPresent()) {
                if (cashierDesk.combineTables(selectedPos, clickedPos, player)) {
                    player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.tables_combined", selectedPos.getX(), selectedPos.getY(), selectedPos.getZ(), clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                }
            } else {
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.selection_cancelled"));
            }
        }
        clearSelectedPos(tag);
    }

    private void handleInteractionWithoutSelection(Player player, CompoundTag tag, CashierDeskBlockEntity cashierDesk, BlockPos clickedPos, SetupMode mode) {
        boolean isSeat = cashierDesk.getSeatAt(clickedPos).isPresent();
        boolean isTable = cashierDesk.getTableAt(clickedPos).isPresent();

        if ((mode == SetupMode.SEAT && isSeat) || (mode == SetupMode.TABLE && isTable)) {
            setSelectedPos(tag, clickedPos);
            player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.selected", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
        } else if (!isSeat && !isTable) {
            if (mode == SetupMode.SEAT && cashierDesk.addSeat(clickedPos, player)) {
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.add_seat", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
            } else if (mode == SetupMode.TABLE && cashierDesk.addTable(clickedPos, player)) {
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.add_table", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.wrong_type_for_selection"));
        }
    }

    private void handleShopAreaInteraction(Player player, CashierDeskBlockEntity cashierDesk, BlockPos pos) {
        boolean willSetPos1 = cashierDesk.getShopConfig().getShopAreaPos1() == null || cashierDesk.getShopConfig().getShopAreaPos2() != null;
        cashierDesk.setShopAreaPos(pos);
        if (willSetPos1) {
            player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.set_area_pos1", pos.getX(), pos.getY(), pos.getZ()));
        } else {
            if (cashierDesk.isShopAreaSet()) {
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.set_area_pos2", pos.getX(), pos.getY(), pos.getZ()));
            } else {
                cashierDesk.getShopConfig().setShopAreaPos2(null);
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.area_invalid_cashier_not_in"));
            }
        }
    }

    @Override
    protected InteractionResultHolder<ItemStack> handleShiftRightClickInAir(Player player, ItemStack stack) {
        if (switchMode(stack, player)) {
            return InteractionResultHolder.sidedSuccess(stack, player.level().isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    protected InteractionResult handleShiftRightClickOnBlock(UseOnContext context) {
        if (switchMode(context.getItemInHand(), context.getPlayer())) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onShopPosSet(CompoundTag tag, Player player) {
        tag.putString(TAG_SETUP_MODE, SetupMode.SHOP_AREA.name());
        tag.remove(TAG_SELECTED_POS); // Clear selection on rebind
    }

    private boolean switchMode(ItemStack itemStack, Player player) {
        CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(TAG_SHOP_POS)) {
            return false;
        }

        if (!player.level().isClientSide()) {
            clearSelectedPos(tag);
            SetupMode currentMode = SetupMode.valueOf(tag.getString(TAG_SETUP_MODE).isEmpty() ? SetupMode.SHOP_AREA.name() : tag.getString(TAG_SETUP_MODE));
            SetupMode nextMode = currentMode.next();
            tag.putString(TAG_SETUP_MODE, nextMode.name());
            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.switch_mode", Component.translatable("setup_mode.foodieshop." + nextMode.name().toLowerCase())));
        }
        return true;
    }

    /**
     * 将指定位置保存为当前选择到物品的NBT中。
     * @param tag 物品的CustomData标签
     * @param pos 要设为选中的位置
     */
    private void setSelectedPos(CompoundTag tag, BlockPos pos) {
        tag.putLong(TAG_SELECTED_POS, pos.asLong());
    }

    /**
     * 从物品的NBT中清除当前选择。
     * @param tag 物品的CustomData标签
     */
    private void clearSelectedPos(CompoundTag tag) {
        tag.remove(TAG_SELECTED_POS);
    }

    /**
     * 从物品的NBT中获取当前选择的位置。
     * @param tag 物品的CustomData标签
     * @return 如果有选择，则返回包含位置的Optional，否则返回空的Optional
     */
    private Optional<BlockPos> getSelectedPos(CompoundTag tag) {
        if (tag.contains(TAG_SELECTED_POS)) {
            return Optional.of(BlockPos.of(tag.getLong(TAG_SELECTED_POS)));
        }
        return Optional.empty();
    }
    
    public enum SetupMode {
        SHOP_AREA,
        SEAT,
        TABLE;

        public SetupMode next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

}
