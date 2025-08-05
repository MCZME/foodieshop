package mczme.foodieshop.api.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.List;

public class ShopConfig {
    private String shopId;
    private String shopOwnerUUID;
    private String ownerName;
    private String shopName;
    private BlockPos cashierDeskLocation;
    private List<BlockPos> inventoryPos;
    private List<BlockPos> cashBoxPos;
    private BlockPos shopAreaPos1;
    private BlockPos shopAreaPos2;
    private List<SeatInfo> seatLocations;
    private List<TableInfo> tableLocations;
    private PathGraph pathGraph;

    public ShopConfig(String shopId, String shopOwnerUUID, String ownerName, String shopName, BlockPos cashierDeskLocation, List<BlockPos> inventoryPos, List<BlockPos> cashBoxPos, BlockPos shopAreaPos1, BlockPos shopAreaPos2, List<SeatInfo> seatLocations, List<TableInfo> tableLocations, PathGraph pathGraph) {
        this.shopId = shopId;
        this.shopOwnerUUID = shopOwnerUUID;
        this.ownerName = ownerName != null ? ownerName : "";
        this.shopName = shopName != null ? shopName : "";
        this.cashierDeskLocation = cashierDeskLocation;
        this.inventoryPos = inventoryPos != null ? inventoryPos : new ArrayList<>();
        this.cashBoxPos = cashBoxPos != null ? cashBoxPos : new ArrayList<>();
        this.shopAreaPos1 = shopAreaPos1;
        this.shopAreaPos2 = shopAreaPos2;
        this.seatLocations = seatLocations != null ? seatLocations : new ArrayList<>();
        this.tableLocations = tableLocations != null ? tableLocations : new ArrayList<>();
        this.pathGraph = pathGraph != null ? pathGraph : new PathGraph();
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("shopId", this.shopId);
        tag.putString("shopOwnerUUID", this.shopOwnerUUID);
        tag.putString("ownerName", this.ownerName);
        tag.putString("shopName", this.shopName);
        tag.put("cashierDeskLocation", NbtUtils.writeBlockPos(this.cashierDeskLocation));
        if (this.inventoryPos != null) {
            ListTag list = new ListTag();
            for (BlockPos pos : this.inventoryPos) {
                list.add(NbtUtils.writeBlockPos(pos));
            }
            tag.put("inventoryPos", list);
        }
        if (this.cashBoxPos != null) {
            ListTag list = new ListTag();
            for (BlockPos pos : this.cashBoxPos) {
                list.add(NbtUtils.writeBlockPos(pos));
            }
            tag.put("cashBoxPos", list);
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
        String ownerName = tag.getString("ownerName");
        String shopName = tag.getString("shopName");
        BlockPos cashierDeskLocation = NbtUtils.readBlockPos(tag, "cashierDeskLocation").orElse(BlockPos.ZERO);

        List<BlockPos> inventoryPos = new ArrayList<>();
        if (tag.contains("inventoryPos", Tag.TAG_LIST)) {
            ListTag list = tag.getList("inventoryPos", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag posTag = list.getCompound(i);
                inventoryPos.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
            }
        } else if (tag.contains("menuContainerPos", CompoundTag.TAG_COMPOUND)) { // Legacy support
            NbtUtils.readBlockPos(tag, "menuContainerPos").ifPresent(inventoryPos::add);
        }

        List<BlockPos> cashBoxPos = new ArrayList<>();
        if (tag.contains("cashBoxPos", Tag.TAG_LIST)) {
            ListTag list = tag.getList("cashBoxPos", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag posTag = list.getCompound(i);
                cashBoxPos.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
            }
        } else if (tag.contains("cashBoxPos", CompoundTag.TAG_COMPOUND)) { // Legacy support
            NbtUtils.readBlockPos(tag, "cashBoxPos").ifPresent(cashBoxPos::add);
        }

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

        return new ShopConfig(shopId, shopOwnerUUID, ownerName, shopName, cashierDeskLocation, inventoryPos, cashBoxPos, shopAreaPos1, shopAreaPos2, seatLocations, tableLocations, pathGraph);
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
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

    public List<BlockPos> getInventoryPos() {
        return inventoryPos;
    }

    public void setInventoryPos(List<BlockPos> inventoryPos) {
        this.inventoryPos = inventoryPos;
    }

    public List<BlockPos> getCashBoxPos() {
        return cashBoxPos;
    }

    public void setCashBoxPos(List<BlockPos> cashBoxPos) {
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

    public TableInfo getTableById(java.util.UUID tableId) {
        if (tableId == null) {
            return null;
        }
        for (TableInfo table : this.tableLocations) {
            if (tableId.equals(table.getTableId())) {
                return table;
            }
        }
        return null;
    }

    public void validateLayout() {
        for (SeatInfo seat : this.seatLocations) {
            boolean isConnectedToTable = seat.getBoundTableId() != null;
            boolean isConnectedToPath = this.pathGraph.isNode(seat.getLocation());
            seat.setValid(isConnectedToTable && isConnectedToPath);
        }

        for (TableInfo table : this.tableLocations) {
            boolean hasValidSeat = false;
            for (SeatInfo seat : table.getBoundSeats()) {
                if (seat.isValid()) {
                    hasValidSeat = true;
                    break;
                }
            }
            table.setValid(hasValidSeat);
        }
    }
}
