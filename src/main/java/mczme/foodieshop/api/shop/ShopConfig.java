package mczme.foodieshop.api.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import mczme.foodieshop.util.PathGraph;

import java.util.ArrayList;
import java.util.List;

public class ShopConfig {
    private String shopId;
    private String shopOwnerUUID;
    private BlockPos cashierDeskLocation;
    private BlockPos menuContainerPos;
    private BlockPos cashBoxPos;
    private BlockPos shopAreaPos1;
    private BlockPos shopAreaPos2;
    private List<SeatInfo> seatLocations;
    private List<TableInfo> tableLocations;
    private PathGraph pathGraph;

    public ShopConfig(String shopId, String shopOwnerUUID, BlockPos cashierDeskLocation, BlockPos menuContainerPos, BlockPos cashBoxPos, BlockPos shopAreaPos1, BlockPos shopAreaPos2, List<SeatInfo> seatLocations, List<TableInfo> tableLocations, PathGraph pathGraph) {
        this.shopId = shopId;
        this.shopOwnerUUID = shopOwnerUUID;
        this.cashierDeskLocation = cashierDeskLocation;
        this.menuContainerPos = menuContainerPos;
        this.cashBoxPos = cashBoxPos;
        this.shopAreaPos1 = shopAreaPos1;
        this.shopAreaPos2 = shopAreaPos2;
        this.seatLocations = seatLocations;
        this.tableLocations = tableLocations;
        this.pathGraph = pathGraph != null ? pathGraph : new PathGraph();
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("shopId", this.shopId);
        tag.putString("shopOwnerUUID", this.shopOwnerUUID);
        tag.put("cashierDeskLocation", NbtUtils.writeBlockPos(this.cashierDeskLocation));
        if (this.menuContainerPos != null) {
            tag.put("menuContainerPos", NbtUtils.writeBlockPos(this.menuContainerPos));
        }
        if (this.cashBoxPos != null) {
            tag.put("cashBoxPos", NbtUtils.writeBlockPos(this.cashBoxPos));
        }
        if (this.shopAreaPos1 != null) {
            tag.put("shopAreaPos1", NbtUtils.writeBlockPos(this.shopAreaPos1));
        }
        if (this.shopAreaPos2 != null) {
            tag.put("shopAreaPos2", NbtUtils.writeBlockPos(this.shopAreaPos2));
        }

        ListTag seatList = new ListTag();
        for (SeatInfo seat : this.seatLocations) {
            seatList.add(seat.toNbt());
        }
        tag.put("seatLocations", seatList);

        ListTag tableList = new ListTag();
        for (TableInfo table : this.tableLocations) {
            tableList.add(table.toNbt());
        }
        tag.put("tableLocations", tableList);

        if (this.pathGraph != null) {
            tag.put("pathGraph", this.pathGraph.toNbt());
        }
        return tag;
    }

    public static ShopConfig fromNbt(CompoundTag tag) {
        String shopId = tag.getString("shopId");
        String shopOwnerUUID = tag.getString("shopOwnerUUID");
        BlockPos cashierDeskLocation = NbtUtils.readBlockPos(tag, "cashierDeskLocation").orElse(BlockPos.ZERO);
        BlockPos menuContainerPos = NbtUtils.readBlockPos(tag, "menuContainerPos").orElse(null);
        BlockPos cashBoxPos = NbtUtils.readBlockPos(tag, "cashBoxPos").orElse(null);
        BlockPos shopAreaPos1 = NbtUtils.readBlockPos(tag, "shopAreaPos1").orElse(null);
        BlockPos shopAreaPos2 = NbtUtils.readBlockPos(tag, "shopAreaPos2").orElse(null);

        List<SeatInfo> seatLocations = new ArrayList<>();
        ListTag seatList = tag.getList("seatLocations", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < seatList.size(); i++) {
            seatLocations.add(SeatInfo.fromNbt(seatList.getCompound(i)));
        }

        List<TableInfo> tableLocations = new ArrayList<>();
        ListTag tableList = tag.getList("tableLocations", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < tableList.size(); i++) {
            tableLocations.add(TableInfo.fromNbt(tableList.getCompound(i)));
        }

        PathGraph pathGraph = PathGraph.fromNbt(tag.getCompound("pathGraph"));

        return new ShopConfig(shopId, shopOwnerUUID, cashierDeskLocation, menuContainerPos, cashBoxPos, shopAreaPos1, shopAreaPos2, seatLocations, tableLocations, pathGraph);
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getShopOwnerUUID() {
        return shopOwnerUUID;
    }

    public void setShopOwnerUUID(String shopOwnerUUID) {
        this.shopOwnerUUID = shopOwnerUUID;
    }

    public BlockPos getCashierDeskLocation() {
        return cashierDeskLocation;
    }

    public void setCashierDeskLocation(BlockPos cashierDeskLocation) {
        this.cashierDeskLocation = cashierDeskLocation;
    }

    public BlockPos getMenuContainerPos() {
        return menuContainerPos;
    }

    public void setMenuContainerPos(BlockPos menuContainerPos) {
        this.menuContainerPos = menuContainerPos;
    }

    public BlockPos getCashBoxPos() {
        return cashBoxPos;
    }

    public void setCashBoxPos(BlockPos cashBoxPos) {
        this.cashBoxPos = cashBoxPos;
    }

    public BlockPos getShopAreaPos1() {
        return shopAreaPos1;
    }

    public void setShopAreaPos1(BlockPos shopAreaPos1) {
        this.shopAreaPos1 = shopAreaPos1;
    }

    public BlockPos getShopAreaPos2() {
        return shopAreaPos2;
    }

    public void setShopAreaPos2(BlockPos shopAreaPos2) {
        this.shopAreaPos2 = shopAreaPos2;
    }

    public List<SeatInfo> getSeatLocations() {
        return seatLocations;
    }

    public void setSeatLocations(List<SeatInfo> seatLocations) {
        this.seatLocations = seatLocations;
    }

    public List<TableInfo> getTableLocations() {
        return tableLocations;
    }

    public void setTableLocations(List<TableInfo> tableLocations) {
        this.tableLocations = tableLocations;
    }

    public PathGraph getPathGraph() {
        return pathGraph;
    }

    public void setPathGraph(PathGraph pathGraph) {
        this.pathGraph = pathGraph;
    }
}
