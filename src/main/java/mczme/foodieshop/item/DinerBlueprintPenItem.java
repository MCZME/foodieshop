package mczme.foodieshop.item;

import mczme.foodieshop.block.CashierDeskBlock;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class DinerBlueprintPenItem extends Item {
    public static final String TAG_SHOP_POS = "shop_pos";
    public static final String TAG_SETUP_MODE = "setup_mode";
    public static final String TAG_SELECTED_POS = "selected_pos";

    public DinerBlueprintPenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || context.getHand() != InteractionHand.MAIN_HAND) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();

        if (player.isShiftKeyDown()) {
            if (level.getBlockState(clickedPos).getBlock() instanceof CashierDeskBlock) {
                if (!level.isClientSide()) {
                    CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    tag.putLong(TAG_SHOP_POS, clickedPos.asLong());
                    tag.putString(TAG_SETUP_MODE, SetupMode.SHOP_AREA.name());
                    tag.remove(TAG_SELECTED_POS); // 重新绑定时清除选择
                    itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.bind_success", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            } else {
                if (switchMode(itemStack, player)) {
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }
        }

        CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(TAG_SHOP_POS)) {
            BlockPos shopPos = BlockPos.of(tag.getLong(TAG_SHOP_POS));
            if (!level.isClientSide() && level.getBlockEntity(shopPos) instanceof CashierDeskBlockEntity cashierDesk) {
                SetupMode currentMode = SetupMode.valueOf(tag.getString(TAG_SETUP_MODE).isEmpty() ? SetupMode.SHOP_AREA.name() : tag.getString(TAG_SETUP_MODE));

                if (currentMode != SetupMode.SHOP_AREA && !cashierDesk.isShopAreaSet()) {
                    player.sendSystemMessage(Component.translatable("message.foodieshop.shop_area_not_set"));
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }

                handleInteraction(player, itemStack, cashierDesk, clickedPos, currentMode);
                level.sendBlockUpdated(shopPos, level.getBlockState(shopPos), level.getBlockState(shopPos), 3);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
    }

    /**
     * 处理玩家使用蓝图笔时的主要交互逻辑。
     * @param player 交互的玩家
     * @param itemStack 玩家手中的蓝图笔
     * @param cashierDesk 绑定的收银台方块实体
     * @param clickedPos 玩家点击的方块位置
     * @param mode 当前的设置模式 (座位/桌子)
     */
    private void handleInteraction(Player player, ItemStack itemStack, CashierDeskBlockEntity cashierDesk, BlockPos clickedPos, SetupMode mode) {
        CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        Optional<BlockPos> selectedPosOpt = getSelectedPos(tag);

        if (mode == SetupMode.SHOP_AREA) {
            handleShopAreaInteraction(player, cashierDesk, clickedPos);
            return;
        }

        if (selectedPosOpt.isPresent()) {
            BlockPos selectedPos = selectedPosOpt.get();
            if (selectedPos.equals(clickedPos)) {
                // 再次点击同一方块：移除
                if (mode == SetupMode.SEAT) {
                    if (cashierDesk.removeSeat(clickedPos)) {
                        player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.remove_seat", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                    }
                } else if (mode == SetupMode.TABLE) {
                    if (cashierDesk.removeTable(clickedPos)) {
                        player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.remove_table", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                    }
                }
                clearSelectedPos(tag);
            } else {
                // 点击不同方块：执行操作
                if (mode == SetupMode.SEAT) {
                    if (cashierDesk.getTableAt(clickedPos).isPresent()) {
                        if (cashierDesk.bindSeatToTable(selectedPos, clickedPos, player)) {
                            player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.seat_bound_to_table", selectedPos.getX(), selectedPos.getY(), selectedPos.getZ(), clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                        }
                    } else {
                        player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.selection_cancelled"));
                    }
                } else if (mode == SetupMode.TABLE) {
                    if (cashierDesk.getTableAt(clickedPos).isPresent()) {
                        if (cashierDesk.combineTables(selectedPos, clickedPos, player)) {
                            player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.tables_combined", selectedPos.getX(), selectedPos.getY(), selectedPos.getZ(), clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                        }
                    } else {
                        player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.selection_cancelled"));
                    }
                }
                clearSelectedPos(tag);
            }
        } else {
            // 没有方块被选中：进行选择或添加
            boolean isSeat = cashierDesk.getSeatAt(clickedPos).isPresent();
            boolean isTable = cashierDesk.getTableAt(clickedPos).isPresent();

            if ((mode == SetupMode.SEAT && isSeat) || (mode == SetupMode.TABLE && isTable)) {
                // 选择已存在的方块
                setSelectedPos(tag, clickedPos);
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.selected", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
            } else if (!isSeat && !isTable) {
                // 添加新方块
                if (mode == SetupMode.SEAT) {
                    if (cashierDesk.addSeat(clickedPos, player)) {
                        player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.add_seat", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                    }
                } else if (mode == SetupMode.TABLE) {
                    if (cashierDesk.addTable(clickedPos, player)) {
                        player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.add_table", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                    }
                }
            } else {
                // 尝试在桌子模式下选择座位，或反之
                 player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.wrong_type_for_selection"));
            }
        }
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        cashierDesk.setChanged();
    }

    /**
     * 处理商店区域设置的交互逻辑。
     * @param player 交互的玩家
     * @param cashierDesk 绑定的收银台方块实体
     * @param pos 玩家点击的方块位置
     */
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
                cashierDesk.setChanged();
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.area_invalid_cashier_not_in"));
            }
        }
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if (switchMode(itemStack, player)) {
                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
            }
        }
        return super.use(level, player, hand);
    }

    /**
     * 切换设置模式。
     * @return 如果成功切换模式则返回 true，否则 false。
     */
    private boolean switchMode(ItemStack itemStack, Player player) {
        CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(TAG_SHOP_POS)) {
            if (!player.level().isClientSide()) {
                // 切换模式时清除选择
                clearSelectedPos(tag);
                SetupMode currentMode = SetupMode
                        .valueOf(tag.getString(TAG_SETUP_MODE).isEmpty() ? SetupMode.SHOP_AREA.name()
                                : tag.getString(TAG_SETUP_MODE));
                SetupMode nextMode = currentMode.next();
                tag.putString(TAG_SETUP_MODE, nextMode.name());
                itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.switch_mode",
                        Component.translatable("setup_mode.foodieshop." + nextMode.name().toLowerCase())));
            }
            return true;
        }
        return false;
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
