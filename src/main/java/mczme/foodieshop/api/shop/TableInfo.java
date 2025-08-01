package mczme.foodieshop.api.shop;

import org.joml.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;

import java.util.*;

public class TableInfo {
    private final UUID tableId;
    private List<BlockPos> locations;
    private transient Set<Edge> renderEdges;
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
        updateRenderEdges();
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
        updateRenderEdges();
    }

    public UUID getTableId() {
        return tableId;
    }

    public List<BlockPos> getLocations() {
        return locations;
    }

    public Set<Edge> getRenderEdges() {
        return renderEdges;
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

    public BlockPos getCenter() {
        if (locations == null || locations.isEmpty()) {
            return BlockPos.ZERO;
        }
        double x = 0, y = 0, z = 0;
        for (BlockPos pos : locations) {
            x += pos.getX();
            y += pos.getY();
            z += pos.getZ();
        }
        int count = locations.size();
        return new BlockPos((int) (x / count), (int) (y / count), (int) (z / count));
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
        tableInfo.updateRenderEdges();
        return tableInfo;
    }

    public void updateRenderEdges() {
        this.renderEdges = new HashSet<>();
        if (this.locations == null || this.locations.isEmpty()) {
            return;
        }

        Map<Edge, Integer> edgeCounts = new HashMap<>();

        for (BlockPos pos : this.locations) {
            float x = pos.getX();
            float y = pos.getY();
            float z = pos.getZ();

            Vector3f p000 = new Vector3f(x, y, z);
            Vector3f p100 = new Vector3f(x + 1, y, z);
            Vector3f p101 = new Vector3f(x + 1, y, z + 1);
            Vector3f p001 = new Vector3f(x, y, z + 1);
            Vector3f p010 = new Vector3f(x, y + 1, z);
            Vector3f p110 = new Vector3f(x + 1, y + 1, z);
            Vector3f p111 = new Vector3f(x + 1, y + 1, z + 1);
            Vector3f p011 = new Vector3f(x, y + 1, z + 1);

            List<Edge> blockEdges = Arrays.asList(
                    // Bottom face
                    new Edge(p000, p100),
                    new Edge(p100, p101),
                    new Edge(p101, p001),
                    new Edge(p001, p000),
                    // Top face
                    new Edge(p010, p110),
                    new Edge(p110, p111),
                    new Edge(p111, p011),
                    new Edge(p011, p010),
                    // Vertical edges
                    new Edge(p000, p010),
                    new Edge(p100, p110),
                    new Edge(p101, p111),
                    new Edge(p001, p011)
            );

            for (Edge edge : blockEdges) {
                edgeCounts.put(edge, edgeCounts.getOrDefault(edge, 0) + 1);
            }
        }

        for (Map.Entry<Edge, Integer> entry : edgeCounts.entrySet()) {
            if (entry.getValue() == 1) {
                this.renderEdges.add(entry.getKey());
            }
        }
    }

    public static class Edge {
        public final Vector3f from;
        public final Vector3f to;

        public Edge(Vector3f from, Vector3f to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return (from.equals(edge.from) && to.equals(edge.to)) ||
                   (from.equals(edge.to) && to.equals(edge.from));
        }

        @Override
        public int hashCode() {
            return from.hashCode() + to.hashCode();
        }
    }
}
