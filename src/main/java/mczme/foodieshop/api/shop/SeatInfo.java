package mczme.foodieshop.api.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

import java.util.UUID;

public class SeatInfo {
    private final BlockPos location;
    private UUID boundTableId = null;
    private boolean isValid;

    public SeatInfo(BlockPos location) {
        this.location = location;
        this.isValid = false;
    }

    public BlockPos getLocation() {
        return location;
    }

    public UUID getBoundTableId() {
        return boundTableId;
    }

    public void bindToTable(UUID tableId) {
        this.boundTableId = tableId;
    }

    public boolean isBoundToTable() {
        return this.boundTableId != null;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.put("location", NbtUtils.writeBlockPos(this.location));
        if (this.boundTableId != null) {
            tag.putUUID("boundTableId", this.boundTableId);
        }
        tag.putBoolean("isValid", this.isValid);
        return tag;
    }

    public static SeatInfo fromNbt(CompoundTag tag) {
        BlockPos location = NbtUtils.readBlockPos(tag, "location").orElse(BlockPos.ZERO);
        SeatInfo seatInfo = new SeatInfo(location);
        if (tag.hasUUID("boundTableId")) {
            seatInfo.bindToTable(tag.getUUID("boundTableId"));
        } else {
            seatInfo.bindToTable(null);
        }
        seatInfo.setValid(tag.getBoolean("isValid"));
        return seatInfo;
    }
}
