package mczme.foodieshop.network.packet.c2s;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import mczme.foodieshop.api.shop.ShopConfig;
import mczme.foodieshop.api.shop.PathGraph;
import mczme.foodieshop.api.shop.SeatInfo;
import mczme.foodieshop.api.shop.TableInfo;
import mczme.foodieshop.network.packet.s2c.ValidateShopResultPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mczme.foodieshop.network.packet.s2c.ValidateShopResultPacket.ValidationMessage;
import mczme.foodieshop.network.packet.s2c.ValidateShopResultPacket.ValidationResultType;
import net.minecraft.network.chat.Component; // 导入 Component
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Queue;
import java.util.LinkedList;

public record ValidateShopPacket(BlockPos pos) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FoodieShop.MODID, "validate_shop");
    public static final Type<ValidateShopPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ValidateShopPacket> STREAM_CODEC = CustomPacketPayload.codec(ValidateShopPacket::write, ValidateShopPacket::new);

    public ValidateShopPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ValidateShopPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().isLoaded(packet.pos)) {
                    BlockEntity be = player.level().getBlockEntity(packet.pos);
                    if (be instanceof CashierDeskBlockEntity cashierDesk) {
                        ShopConfig config = cashierDesk.getShopConfig();
                        List<ValidationMessage> validationMessages = new ArrayList<>();
                        ValidationResultType resultType = ValidationResultType.SUCCESS;

                        // 验证商店区域
                        if (config.getShopAreaPos1() == null || config.getShopAreaPos2() == null) {
                            validationMessages.add(new ValidationMessage("foodieshop.validation.error.no_area", List.of()));
                            resultType = updateResultType(resultType, ValidationResultType.ERROR);
                        }

                        // 验证收银台位置
                        if (config.getCashierDeskLocation() == null) {
                            validationMessages.add(new ValidationMessage("foodieshop.validation.error.no_cashier", List.of()));
                            resultType = updateResultType(resultType, ValidationResultType.ERROR);
                        } else if (!config.getCashierDeskLocation().equals(packet.pos)) {
                            validationMessages.add(new ValidationMessage("foodieshop.validation.error.cashier_mismatch", List.of()));
                            resultType = updateResultType(resultType, ValidationResultType.ERROR);
                        }

                        // 验证桌子和座位
                        if (config.getTableLocations().isEmpty()) {
                            validationMessages.add(new ValidationMessage("foodieshop.validation.warning.no_tables", List.of()));
                            resultType = updateResultType(resultType, ValidationResultType.WARNING);
                        } else {
                            for (TableInfo table : config.getTableLocations()) {
                                if (!table.isValid()) {
                                    validationMessages.add(new ValidationMessage("foodieshop.validation.warning.invalid_table", List.of(Component.literal(table.getCenter().toShortString()))));
                                    resultType = updateResultType(resultType, ValidationResultType.WARNING);
                                }
                            }
                        }

                        if (config.getSeatLocations().isEmpty()) {
                            validationMessages.add(new ValidationMessage("foodieshop.validation.warning.no_seats", List.of()));
                            resultType = updateResultType(resultType, ValidationResultType.WARNING);
                        } else {
                            Set<BlockPos> allTableBlocks = config.getTableLocations().stream()
                                    .flatMap(table -> table.getLocations().stream())
                                    .collect(Collectors.toSet());

                            for (SeatInfo seat : config.getSeatLocations()) {
                                if (!seat.isValid()) {
                                    validationMessages.add(new ValidationMessage("foodieshop.validation.warning.invalid_seat", List.of(Component.literal(seat.getLocation().toShortString()))));
                                    resultType = updateResultType(resultType, ValidationResultType.WARNING);
                                }

                                boolean connectedToTable = false;
                                for (BlockPos tableBlock : allTableBlocks) {
                                    if (seat.getLocation().distManhattan(tableBlock) == 1) { // 曼哈顿距离为1表示相邻
                                        connectedToTable = true;
                                        break;
                                    }
                                }
                                if (!connectedToTable) {
                                    validationMessages.add(new ValidationMessage("foodieshop.validation.warning.seat_not_connected_to_table", List.of(Component.literal(seat.getLocation().toShortString()))));
                                    resultType = updateResultType(resultType, ValidationResultType.WARNING);
                                }
                            }
                        }

                        // 验证路径图
                        PathGraph pathGraph = config.getPathGraph();
                        if (pathGraph == null || pathGraph.getNodes().isEmpty()) {
                            validationMessages.add(new ValidationMessage("foodieshop.validation.error.no_path", List.of()));
                            resultType = updateResultType(resultType, ValidationResultType.ERROR);
                        } else {
                            if (pathGraph.getEntry() == null) {
                                validationMessages.add(new ValidationMessage("foodieshop.validation.error.no_path_entry", List.of()));
                                resultType = updateResultType(resultType, ValidationResultType.ERROR);
                            }
                            if (pathGraph.getExit() == null) {
                                validationMessages.add(new ValidationMessage("foodieshop.validation.error.no_path_exit", List.of()));
                                resultType = updateResultType(resultType, ValidationResultType.ERROR);
                            }
                            // 检查路径图连通性
                            PathConnectivityResult connectivity = checkPathConnectivity(pathGraph);
                            if (!connectivity.isEntryExitConnected()) {
                                validationMessages.add(new ValidationMessage("foodieshop.validation.error.path_not_connected", List.of()));
                                resultType = updateResultType(resultType, ValidationResultType.ERROR);
                            }
                            if (connectivity.hasIsolatedNodes()) {
                                validationMessages.add(new ValidationMessage("foodieshop.validation.warning.path_isolated_nodes", List.of()));
                                resultType = updateResultType(resultType, ValidationResultType.WARNING);
                            }


                            // 检查入口和出口是否在节点列表中
                            if (pathGraph.getEntry() != null && !pathGraph.getNodes().contains(pathGraph.getEntry())) {
                                validationMessages.add(new ValidationMessage("foodieshop.validation.error.path_entry_not_in_nodes", List.of()));
                                resultType = updateResultType(resultType, ValidationResultType.ERROR);
                            }
                            if (pathGraph.getExit() != null && !pathGraph.getNodes().contains(pathGraph.getExit())) {
                                validationMessages.add(new ValidationMessage("foodieshop.validation.error.path_exit_not_in_nodes", List.of()));
                                resultType = updateResultType(resultType, ValidationResultType.ERROR);
                            }

                            // 验证合法座位与路径图边的相邻性
                            for (SeatInfo seat : config.getSeatLocations()) {
                                if (seat.isValid()) {
                                    boolean adjacentToPath = false;
                                    for (List<BlockPos> edge : pathGraph.getEdges()) {
                                        if (isHorizontallyAdjacentToEdge(seat.getLocation(), edge.get(0), edge.get(1))) {
                                            adjacentToPath = true;
                                            break;
                                        }
                                    }
                                    if (!adjacentToPath) {
                                        validationMessages.add(new ValidationMessage("foodieshop.validation.warning.seat_not_adjacent_to_path", List.of(Component.literal(seat.getLocation().toShortString()))));
                                        resultType = updateResultType(resultType, ValidationResultType.WARNING);
                                    }
                                }
                            }
                        }

                        // 验证菜单物品
                        if (config.getMenuItems().isEmpty()) {
                            validationMessages.add(new ValidationMessage("foodieshop.validation.error.no_menu_items", List.of()));
                            resultType = updateResultType(resultType, ValidationResultType.ERROR);
                        }

                        // 如果没有特定错误，则添加成功消息
                        if (validationMessages.isEmpty()) {
                            validationMessages.add(new ValidationMessage("foodieshop.validation.success", List.of()));
                        }

                        PacketDistributor.sendToPlayer(player, new ValidateShopResultPacket(resultType, validationMessages));
                    }
                }
            }
        });
    }

    /**
     * 检查一个点是否与由两个端点定义的线段水平相邻。
     * 假设边是轴对齐的（沿着X轴或Z轴）。
     */
    private static boolean isHorizontallyAdjacentToEdge(BlockPos point, BlockPos edgeStart, BlockPos edgeEnd) {
        // 确保Y坐标相同
        if (point.getY() != edgeStart.getY() || point.getY() != edgeEnd.getY()) {
            return false;
        }

        // 检查点是否与线段水平相邻
        // 沿着X轴的边
        if (edgeStart.getZ() == edgeEnd.getZ()) {
            // 检查点的Z坐标是否与边的Z坐标水平相邻
            if (Math.abs(point.getZ() - edgeStart.getZ()) == 1) {
                // 检查点的X坐标是否在线段的X坐标范围内
                int minX = Math.min(edgeStart.getX(), edgeEnd.getX());
                int maxX = Math.max(edgeStart.getX(), edgeEnd.getX());
                return point.getX() >= minX && point.getX() <= maxX;
            }
        }
        // 沿着Z轴的边
        else if (edgeStart.getX() == edgeEnd.getX()) {
            // 检查点的X坐标是否与边的X坐标水平相邻
            if (Math.abs(point.getX() - edgeStart.getX()) == 1) {
                // 检查点的Z坐标是否在线段的Z坐标范围内
                int minZ = Math.min(edgeStart.getZ(), edgeEnd.getZ());
                int maxZ = Math.max(edgeStart.getZ(), edgeEnd.getZ());
                return point.getZ() >= minZ && point.getZ() <= maxZ;
            }
        }
        return false;
    }

    private static ValidationResultType updateResultType(ValidationResultType current, ValidationResultType newType) {
        if (current.ordinal() < newType.ordinal()) {
            return newType;
        }
        return current;
    }

    private record PathConnectivityResult(boolean isEntryExitConnected, boolean hasIsolatedNodes) {}

    private static PathConnectivityResult checkPathConnectivity(PathGraph graph) {
        BlockPos entry = graph.getEntry();
        BlockPos exit = graph.getExit();
        List<BlockPos> nodes = graph.getNodes();

        if (entry == null || exit == null || nodes.isEmpty()) {
            return new PathConnectivityResult(false, !nodes.isEmpty());
        }

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.offer(entry);
        visited.add(entry);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (List<BlockPos> edge : graph.getEdges()) {
                BlockPos neighbor = null;
                if (current.equals(edge.get(0))) {
                    neighbor = edge.get(1);
                } else if (current.equals(edge.get(1))) {
                    neighbor = edge.get(0);
                }

                if (neighbor != null && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        boolean entryExitConnected = visited.contains(exit);
        boolean hasIsolatedNodes = visited.size() < nodes.size();

        return new PathConnectivityResult(entryExitConnected, hasIsolatedNodes);
    }
}
