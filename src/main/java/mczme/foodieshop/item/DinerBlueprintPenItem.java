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

public class DinerBlueprintPenItem extends Item {
    public static final String TAG_SHOP_POS = "shop_pos";
    public static final String TAG_SETUP_MODE = "setup_mode";

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
        BlockPos pos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();

        // 潜行右键的优先级处理
        if (player.isShiftKeyDown()) {
            // 最高优先级：绑定到收银台
            if (level.getBlockState(pos).getBlock() instanceof CashierDeskBlock) {
                if (!level.isClientSide()) {
                    // 绑定笔，并让收银台清空其旧数据
                    if (level.getBlockEntity(pos) instanceof CashierDeskBlockEntity cashierDesk) {
                        cashierDesk.clearAllData();
                        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                    }
                    CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    tag.putLong(TAG_SHOP_POS, pos.asLong());
                    tag.putString(TAG_SETUP_MODE, SetupMode.SHOP_AREA.name()); // 重置模式为默认
                    itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.bind_success", pos.getX(), pos.getY(), pos.getZ()));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            } else {
                // 次高优先级：切换模式
                if (switchMode(itemStack, player)) {
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }
        }

        CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        // 2. 交互逻辑 (仅当笔已绑定时)
        if (tag.contains(TAG_SHOP_POS)) {
            BlockPos shopPos = BlockPos.of(tag.getLong(TAG_SHOP_POS));
            if (!level.isClientSide() && level.getBlockEntity(shopPos) instanceof CashierDeskBlockEntity cashierDesk) {
                SetupMode currentMode = SetupMode.valueOf(tag.getString(TAG_SETUP_MODE).isEmpty() ? SetupMode.SHOP_AREA.name() : tag.getString(TAG_SETUP_MODE));
                String modeName = currentMode.name().toLowerCase();

                // 右键交互逻辑 - 直接操作方块实体
                switch (currentMode) {
                    case SHOP_AREA:
                        // 先判断将要设置的是点1还是点2，以便发送正确的反馈
                        boolean willSetPos1 = cashierDesk.getShopConfig().getShopAreaPos1() == null || cashierDesk.getShopConfig().getShopAreaPos2() != null;
                        
                        // 执行操作
                        cashierDesk.setShopAreaPos(pos);

                        // 根据之前的判断发送反馈
                        if (willSetPos1) {
                            player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.set_area_pos1", pos.getX(), pos.getY(), pos.getZ()));
                        } else {
                            player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.set_area_pos2", pos.getX(), pos.getY(), pos.getZ()));
                        }
                        break;
                    case SEAT:
                        boolean addedSeat = cashierDesk.toggleSeat(pos);
                        player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen." + (addedSeat ? "add_" : "remove_") + modeName, pos.getX(), pos.getY(), pos.getZ()));
                        break;
                    case TABLE:
                        boolean addedTable = cashierDesk.toggleTable(pos);
                        player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen." + (addedTable ? "add_" : "remove_") + modeName, pos.getX(), pos.getY(), pos.getZ()));
                        break;
                    case PATH:
                        cashierDesk.addPathWaypoint(pos);
                        player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.add_path", pos.getX(), pos.getY(), pos.getZ(), cashierDesk.getShopConfig().getShopPathWaypoints().size()));
                        break;
                }
                
                // 在所有修改之后，统一发送更新
                level.sendBlockUpdated(shopPos, level.getBlockState(shopPos), level.getBlockState(shopPos), 3);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
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
    
    public enum SetupMode {
        SHOP_AREA,
        SEAT,
        TABLE,
        PATH;

        public SetupMode next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

}
