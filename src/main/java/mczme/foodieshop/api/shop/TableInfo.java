package mczme.foodieshop.api.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class TableInfo {
    private final BlockPos location;
    private boolean isValid;

    public TableInfo(BlockPos location, boolean isValid) {
        this.location = location;
        this.isValid = isValid;
    }

    public BlockPos getLocation() {
        return location;
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
        tag.putBoolean("isValid", this.isValid);
        return tag;
    }

    public static TableInfo fromNbt(CompoundTag tag) {
        BlockPos location = NbtUtils.readBlockPos(tag, "location").orElse(BlockPos.ZERO);
        boolean isValid = tag.getBoolean("isValid");
        return new TableInfo(location, isValid);
    }
}
