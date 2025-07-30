package mczme.foodieshop.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PathGraph {
    private final List<BlockPos> nodes;
    private final List<List<BlockPos>> edges;

    public PathGraph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    private PathGraph(List<BlockPos> nodes, List<List<BlockPos>> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<BlockPos> getNodes() {
        return nodes;
    }

    public List<List<BlockPos>> getEdges() {
        return edges;
    }

    public boolean hasNode(BlockPos pos) {
        return nodes.contains(pos);
    }

    public void addNode(BlockPos pos) {
        if (!hasNode(pos)) {
            nodes.add(pos);
        }
    }

    public void removeNode(BlockPos pos) {
        if (nodes.remove(pos)) {
            edges.removeIf(edge -> edge.contains(pos));
        }
    }

    public boolean hasEdge(BlockPos pos1, BlockPos pos2) {
        for (List<BlockPos> edge : edges) {
            if ((Objects.equals(edge.get(0), pos1) && Objects.equals(edge.get(1), pos2)) ||
                (Objects.equals(edge.get(0), pos2) && Objects.equals(edge.get(1), pos1))) {
                return true;
            }
        }
        return false;
    }

    public void addEdge(BlockPos pos1, BlockPos pos2) {
        if (hasNode(pos1) && hasNode(pos2) && !hasEdge(pos1, pos2)) {
            edges.add(List.of(pos1, pos2));
        }
    }

    public void removeEdge(BlockPos pos1, BlockPos pos2) {
        edges.removeIf(edge -> (Objects.equals(edge.get(0), pos1) && Objects.equals(edge.get(1), pos2)) ||
                                (Objects.equals(edge.get(0), pos2) && Objects.equals(edge.get(1), pos1)));
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag nodeList = new ListTag();
        for (BlockPos node : this.nodes) {
            BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, node).result().ifPresent(nodeList::add);
        }
        tag.put("nodes", nodeList);

        ListTag edgeList = new ListTag();
        for (List<BlockPos> edge : this.edges) {
            if (edge != null && edge.size() == 2) {
                ListTag edgePair = new ListTag();
                BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, edge.get(0)).result().ifPresent(edgePair::add);
                BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, edge.get(1)).result().ifPresent(edgePair::add);
                edgeList.add(edgePair);
            }
        }
        tag.put("edges", edgeList);
        return tag;
    }

    public static PathGraph fromNbt(CompoundTag tag) {
        List<BlockPos> nodes = new ArrayList<>();
        ListTag nodeList = (ListTag) tag.get("nodes");
        for (Tag t : nodeList) {
            BlockPos.CODEC.parse(NbtOps.INSTANCE, t).result().ifPresent(nodes::add);
        }

        List<List<BlockPos>> edges = new ArrayList<>();
        ListTag edgeList = tag.getList("edges", Tag.TAG_LIST);
        for (Tag t : edgeList) {
            if (t instanceof ListTag edgePair && edgePair.size() == 2) {
                List<BlockPos> edge = new ArrayList<>();
                BlockPos.CODEC.parse(NbtOps.INSTANCE, edgePair.get(0)).result().ifPresent(edge::add);
                BlockPos.CODEC.parse(NbtOps.INSTANCE, edgePair.get(1)).result().ifPresent(edge::add);
                if (edge.size() == 2) {
                    edges.add(edge);
                }
            }
        }
        return new PathGraph(nodes, edges);
    }
}
