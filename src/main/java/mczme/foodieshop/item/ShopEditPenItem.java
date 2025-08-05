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
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public abstract class ShopEditPenItem extends Item {

    public static final String TAG_SHOP_POS = "shop_pos";

    public ShopEditPenItem(Properties properties) {
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
                    setShopPos(itemStack, clickedPos, player);
                    player.sendSystemMessage(Component.translatable("message.foodieshop.diner_blueprint_pen.bind_success", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            } else {
                return handleShiftRightClickOnBlock(context);
            }
        }

        Optional<BlockPos> shopPosOpt = getShopPos(itemStack);
        if (shopPosOpt.isEmpty()) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.foodieshop.shop_inventory_pen.not_bound"));
            }
            return InteractionResult.FAIL;
        }

        BlockPos shopPos = shopPosOpt.get();
        BlockEntity be = level.getBlockEntity(shopPos);
        if (!(be instanceof CashierDeskBlockEntity cashierDesk)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.foodieshop.shop_inventory_pen.cashier_desk_not_found"));
            }
            return InteractionResult.FAIL;
        }

        if (!canEdit(player, cashierDesk)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.foodieshop.cannot_edit"));
            }
            return InteractionResult.FAIL;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        InteractionResult result = handleInteraction(player, itemStack, cashierDesk, clickedPos, level);
        if (result.consumesAction()) {
            cashierDesk.setChanged();
            level.sendBlockUpdated(shopPos, level.getBlockState(shopPos), level.getBlockState(shopPos), 3);
        }
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            return handleShiftRightClickInAir(player, itemStack);
        }
        return super.use(level, player, hand);
    }

    protected InteractionResult handleShiftRightClickOnBlock(UseOnContext context) {
        return InteractionResult.PASS;
    }

    protected InteractionResultHolder<ItemStack> handleShiftRightClickInAir(Player player, ItemStack stack) {
        return InteractionResultHolder.pass(stack);
    }

    protected boolean canEdit(Player player, CashierDeskBlockEntity cashierDesk) {
        return cashierDesk.canEdit(player);
    }

    protected abstract InteractionResult handleInteraction(Player player, ItemStack stack, CashierDeskBlockEntity cashierDesk, BlockPos clickedPos, Level level);

    protected Optional<BlockPos> getShopPos(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(TAG_SHOP_POS)) {
            return Optional.of(BlockPos.of(tag.getLong(TAG_SHOP_POS)));
        }
        return Optional.empty();
    }

    protected void setShopPos(ItemStack stack, BlockPos pos, Player player) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putLong(TAG_SHOP_POS, pos.asLong());
        onShopPosSet(tag, player);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    protected void onShopPosSet(CompoundTag tag, Player player) {
        // Hook for subclasses
    }
}
