package mczme.foodieshop.api.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Set;

public class ShopConfig {
    private String shopId;
    private String shopOwnerUUID;
    private String ownerName;
    private String shopName;
    private BlockPos cashierDeskLocation;
    private Set<BlockPos> inventoryLocations;
    private Set<BlockPos> deliveryBoxLocations;
    private BlockPos shopAreaPos1;
    private BlockPos shopAreaPos2;
    private Set<SeatInfo> seatLocations;
    private Set<TableInfo> tableLocations;
    private PathGraph pathGraph;
    private Set<Item> menuItems;

    public ShopConfig(String shopId, String shopOwnerUUID, String ownerName, String shopName, BlockPos cashierDeskLocation, Set<BlockPos> inventoryLocations, Set<BlockPos> deliveryBoxLocations, BlockPos shopAreaPos1, BlockPos shopAreaPos2, Set<SeatInfo> seatLocations, Set<TableInfo> tableLocations, PathGraph pathGraph, Set<Item> menuItems) {
        this.shopId = shopId;
        this.shopOwnerUUID = shopOwnerUUID;
        this.ownerName = ownerName != null ? ownerName : "";
        this.shopName = shopName != null ? shopName : "";
        this.cashierDeskLocation = cashierDeskLocation;
        this.inventoryLocations = inventoryLocations != null ? inventoryLocations : new HashSet<>();
        this.deliveryBoxLocations = deliveryBoxLocations != null ? deliveryBoxLocations : new HashSet<>();
        this.shopAreaPos1 = shopAreaPos1;
        this.shopAreaPos2 = shopAreaPos2;
        this.seatLocations = seatLocations != null ? seatLocations : new HashSet<>();
        this.tableLocations = tableLocations != null ? tableLocations : new HashSet<>();
        this.pathGraph = pathGraph != null ? pathGraph : new PathGraph();
        this.menuItems = menuItems != null ? menuItems : new HashSet<>();
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("shopId", this.shopId);
        tag.putString("shopOwnerUUID", this.shopOwnerUUID);
        tag.putString("ownerName", this.ownerName);
        tag.putString("shopName", this.shopName);
        tag.put("cashierDeskLocation", NbtUtils.writeBlockPos(this.cashierDeskLocation));
        if (this.inventoryLocations != null) {
            ListTag list = new ListTag();
            for (BlockPos pos : this.inventoryLocations) {
                list.add(NbtUtils.writeBlockPos(pos));
            }
            tag.put("inventoryLocations", list);
        }
        if (this.deliveryBoxLocations != null) {
            ListTag list = new ListTag();
            for (BlockPos pos : this.deliveryBoxLocations) {
                list.add(NbtUtils.writeBlockPos(pos));
            }
            tag.put("deliveryBoxLocations", list);
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

        ListTag menuList = new ListTag();
        for (Item item : this.menuItems) {
            menuList.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
        }
        tag.put("menuItems", menuList);

        return tag;
    }

    public static ShopConfig fromNbt(CompoundTag tag) {
        String shopId = tag.getString("shopId");
        String shopOwnerUUID = tag.getString("shopOwnerUUID");
        String ownerName = tag.getString("ownerName");
        String shopName = tag.getString("shopName");
        BlockPos cashierDeskLocation = NbtUtils.readBlockPos(tag, "cashierDeskLocation").orElse(BlockPos.ZERO);

        Set<BlockPos> inventoryLocations = new HashSet<>();
        if (tag.contains("inventoryLocations", Tag.TAG_LIST)) {
            ListTag list = tag.getList("inventoryLocations", CompoundTag.TAG_INT_ARRAY);
            for (int i = 0; i < list.size(); i++) {
                int[] posTag = list.getIntArray(i);
                inventoryLocations.add(new BlockPos(posTag[0], posTag[1], posTag[2]));
            }
        }

        Set<BlockPos> deliveryBoxLocations = new HashSet<>();
        if (tag.contains("deliveryBoxLocations", Tag.TAG_LIST)) {
            ListTag list = tag.getList("deliveryBoxLocations", CompoundTag.TAG_INT_ARRAY);
            for (int i = 0; i < list.size(); i++) {
                int[] posTag = list.getIntArray(i);
                deliveryBoxLocations.add(new BlockPos(posTag[0], posTag[1], posTag[2]));
            }
        }

        BlockPos shopAreaPos1 = NbtUtils.readBlockPos(tag, "shopAreaPos1").orElse(null);
        BlockPos shopAreaPos2 = NbtUtils.readBlockPos(tag, "shopAreaPos2").orElse(null);

        Set<SeatInfo> seatLocations = new HashSet<>();
        ListTag seatList = tag.getList("seatLocations", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < seatList.size(); i++) {
            seatLocations.add(SeatInfo.fromNbt(seatList.getCompound(i)));
        }

        Set<TableInfo> tableLocations = new HashSet<>();
        ListTag tableList = tag.getList("tableLocations", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < tableList.size(); i++) {
            tableLocations.add(TableInfo.fromNbt(tableList.getCompound(i)));
        }

        PathGraph pathGraph = PathGraph.fromNbt(tag.getCompound("pathGraph"));

        Set<Item> menuItems = new HashSet<>();
        if (tag.contains("menuItems", Tag.TAG_LIST)) {
            ListTag menuList = tag.getList("menuItems", Tag.TAG_STRING);
            for (int i = 0; i < menuList.size(); i++) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(menuList.getString(i)));
                menuItems.add(item);
            }
        }

        return new ShopConfig(shopId, shopOwnerUUID, ownerName, shopName, cashierDeskLocation, inventoryLocations, deliveryBoxLocations, shopAreaPos1, shopAreaPos2, seatLocations, tableLocations, pathGraph, menuItems);
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

    public Set<BlockPos> getInventoryLocations() {
        return inventoryLocations;
    }

    public void setInventoryLocations(Set<BlockPos> inventoryLocations) {
        this.inventoryLocations = inventoryLocations;
    }

    public Set<BlockPos> getDeliveryBoxLocations() {
        return deliveryBoxLocations;
    }

    public void setDeliveryBoxLocations(Set<BlockPos> deliveryBoxLocations) {
        this.deliveryBoxLocations = deliveryBoxLocations;
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

    public Set<SeatInfo> getSeatLocations() {
        return seatLocations;
    }

    public void setSeatLocations(Set<SeatInfo> seatLocations) {
        this.seatLocations = seatLocations;
    }

    public Set<TableInfo> getTableLocations() {
        return tableLocations;
    }

    public void setTableLocations(Set<TableInfo> tableLocations) {
        this.tableLocations = tableLocations;
    }

    public PathGraph getPathGraph() {
        return pathGraph;
    }

    public void setPathGraph(PathGraph pathGraph) {
        this.pathGraph = pathGraph;
    }

    public Set<Item> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(Set<Item> menuItems) {
        this.menuItems = menuItems;
    }

    public void togglePathNodeMode(BlockPos pos) {
        if (this.pathGraph != null) {
            this.pathGraph.toggleNodeMode(pos);
        }
    }

    public boolean isPositionOccupied(BlockPos pos) {
        if (this.cashierDeskLocation.equals(pos)) {
            return true;
        }
        if (this.inventoryLocations.contains(pos)) {
            return true;
        }
        if (this.deliveryBoxLocations.contains(pos)) {
            return true;
        }
        for (SeatInfo seat : this.seatLocations) {
            if (seat.getLocation().equals(pos)) {
                return true;
            }
        }
        for (TableInfo table : this.tableLocations) {
            if (table.getLocations().contains(pos)) {
                return true;
            }
        }
        return false;
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
            seat.setValid(isConnectedToTable);
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
