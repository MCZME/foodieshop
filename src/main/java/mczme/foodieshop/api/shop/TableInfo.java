package mczme.foodieshop.api.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TableInfo {
    private final UUID tableId;
    private List<BlockPos> locations;
    private List<SeatInfo> boundSeats;
    private boolean isValid;

    public TableInfo(List<BlockPos> locations) {
        this(UUID.randomUUID(), locations);
    }

    private TableInfo(UUID tableId, List<BlockPos> locations) {
        this.tableId = tableId;
        this.locations = locations;
        this.boundSeats = new ArrayList<>();
        this.isValid = false;
    }

    public boolean isAdjacent(BlockPos other) {
        for (BlockPos location : locations) {
            // 检查Y坐标是否相同，以及X/Z平面上的曼哈顿距离是否为1（即水平方向上相邻）
            if (location.getY() == other.getY() &&
                (Math.abs(location.getX() - other.getX()) + Math.abs(location.getZ() - other.getZ()) == 1)) {
                return true;
            }
        }
        return false;
    }

    public void merge(TableInfo other) {
        this.locations.addAll(other.getLocations());
    }

    public UUID getTableId() {
        return tableId;
    }

    public List<BlockPos> getLocations() {
        return locations;
    }

    public List<SeatInfo> getBoundSeats() {
        return boundSeats;
    }

    public void bindSeat(SeatInfo seat) {
        // 避免重复添加
        if (this.boundSeats.stream().noneMatch(s -> s.getLocation().equals(seat.getLocation()))) {
            this.boundSeats.add(seat);
        }
    }

    public void unbindSeatByLocation(BlockPos seatLocation) {
        this.boundSeats.removeIf(seat -> seat.getLocation().equals(seatLocation));
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("tableId", this.tableId);
        ListTag locationList = new ListTag();
        for (BlockPos pos : this.locations) {
            CompoundTag posTag = new CompoundTag();
            posTag.put("location", NbtUtils.writeBlockPos(pos));
            locationList.add(posTag);
        }
        tag.put("locations", locationList);

        ListTag boundSeatsList = new ListTag();
        for (SeatInfo seat : this.boundSeats) {
            boundSeatsList.add(seat.toNbt());
        }
        tag.put("boundSeats", boundSeatsList);

        tag.putBoolean("isValid", this.isValid);
        return tag;
    }

    public static TableInfo fromNbt(CompoundTag tag) {
        UUID tableId = tag.getUUID("tableId");
        List<BlockPos> locations = new ArrayList<>();
        ListTag locationList = tag.getList("locations", 10); // NBT.TAG_COMPOUND
        for (int i = 0; i < locationList.size(); i++) {
            NbtUtils.readBlockPos(locationList.getCompound(i), "location").ifPresent(locations::add);
        }

        TableInfo tableInfo = new TableInfo(tableId, locations);

        ListTag boundSeatsList = tag.getList("boundSeats", 10); // NBT.TAG_COMPOUND
        for (int i = 0; i < boundSeatsList.size(); i++) {
            tableInfo.boundSeats.add(SeatInfo.fromNbt(boundSeatsList.getCompound(i)));
        }

        tableInfo.setValid(tag.getBoolean("isValid"));
        return tableInfo;
    }
}
