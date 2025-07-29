package mczme.foodieshop.screen;

import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import mczme.foodieshop.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShopConfigMenu extends AbstractContainerMenu {
    private final CashierDeskBlockEntity blockEntity;

    public ShopConfigMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, extraData.readBlockPos());
    }

    public ShopConfigMenu(int containerId, Inventory inventory, BlockPos blockPos) {
        super(ModMenuTypes.SHOP_CONFIG_MENU.get(), containerId);
        this.blockEntity = (CashierDeskBlockEntity) inventory.player.level().getBlockEntity(blockPos);
    }

    public CashierDeskBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public mczme.foodieshop.api.shop.ShopConfig getShopConfig() {
        return this.blockEntity.getShopConfig();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
