package mczme.foodieshop.block.blockentity;

import mczme.foodieshop.api.shop.ShopConfig;
import mczme.foodieshop.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import mczme.foodieshop.screen.ShopConfigMenu;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class CashierDeskBlockEntity extends BlockEntity implements MenuProvider {

    private ShopConfig shopConfig;

    public CashierDeskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CASHIER_DESK_BE.get(), pos, state);
        // 使用默认的空ShopConfig进行初始化
        this.shopConfig = new ShopConfig(
                pos.toShortString(),
                "",
                pos,
                null,
                null,
                null,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public ShopConfig getShopConfig() {
        return shopConfig;
    }

    public void setShopConfig(ShopConfig shopConfig) {
        this.shopConfig = shopConfig;
        setChanged(); // 将方块实体标记为“脏”以强制保存
    }

    public void setShopAreaPos(BlockPos pos) {
        // 如果pos1未设置，则设置它。
        if (this.shopConfig.getShopAreaPos1() == null) {
            this.shopConfig.setShopAreaPos1(pos);
        }
        // 如果pos1已设置但pos2未设置，则设置pos2。
        else if (this.shopConfig.getShopAreaPos2() == null) {
            this.shopConfig.setShopAreaPos2(pos);
        }
        // 如果两者都已设置，则重置pos1并清除pos2。
        else {
            this.shopConfig.setShopAreaPos1(pos);
            this.shopConfig.setShopAreaPos2(null);
        }
        setChanged();
    }

    public boolean toggleSeat(BlockPos pos) {
        // 如果座位存在，则查找并移除
        boolean removed = this.shopConfig.getSeatLocations().removeIf(seatInfo -> seatInfo.getLocation().equals(pos));

        // 如果没有被移除，则添加一个新的
        if (!removed) {
            this.shopConfig.getSeatLocations().add(new mczme.foodieshop.api.shop.SeatInfo(pos, true));
        }
        setChanged();
        return !removed; // 如果添加则返回true，如果移除则返回false
    }

    public boolean toggleTable(BlockPos pos) {
        // 如果桌子存在，则查找并移除
        boolean removed = this.shopConfig.getTableLocations().removeIf(tableInfo -> tableInfo.getLocation().equals(pos));

        // 如果没有被移除，则添加一个新的
        if (!removed) {
            this.shopConfig.getTableLocations().add(new mczme.foodieshop.api.shop.TableInfo(pos, true));
        }
        setChanged();
        return !removed; // 如果添加则返回true，如果移除则返回false
    }

    public void addPathWaypoint(BlockPos pos) {
        // 防止连续添加完全相同的方块
        if (!this.shopConfig.getShopPathWaypoints().isEmpty() &&
                this.shopConfig.getShopPathWaypoints().getLast().equals(pos)) {
            return;
        }
        this.shopConfig.getShopPathWaypoints().add(pos);
        setChanged();
    }

    public void clearAllData() {
        this.shopConfig = new ShopConfig(
                this.getBlockPos().toShortString(),
                "",
                this.getBlockPos(),
                null,
                null,
                null,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        setChanged();
    }

    public void setOwner(Player player) {
        if (this.shopConfig.getShopOwnerUUID() == null || this.shopConfig.getShopOwnerUUID().isEmpty()) {
            this.shopConfig.setShopOwnerUUID(player.getUUID().toString());
            setChanged();
        }
    }

    // 为屏幕提供的getters
    public UUID getOwnerUUID() {
        try {
            return UUID.fromString(this.shopConfig.getShopOwnerUUID());
        } catch (IllegalArgumentException e) {
            return null; // 或返回一个默认的UUID
        }
    }

    public BlockPos getMenuContainerPos() {
        return this.shopConfig.getMenuContainerPos();
    }

    public BlockPos getCashBoxPos() {
        return this.shopConfig.getCashBoxPos();
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("shopConfig")) {
            this.shopConfig = ShopConfig.fromNbt(tag.getCompound("shopConfig"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("shopConfig", this.shopConfig.toNbt());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.foodieshop.cashier_desk");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new ShopConfigMenu(containerId, inventory, this.getBlockPos());
    }
}
