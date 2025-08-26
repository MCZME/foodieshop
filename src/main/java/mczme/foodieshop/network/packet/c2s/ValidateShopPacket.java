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

import mczme.foodieshop.network.packet.s2c.ValidateShopResultPacket.MessageType;
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
                            validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_NO_AREA, List.of()));
                            resultType = ValidationResultType.ERROR;
                        }

                        // 验证收银台位置
                        if (config.getCashierDeskLocation() == null) {
                            validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_NO_CASHIER, List.of()));
                            resultType = ValidationResultType.ERROR;
                        } else if (!config.getCashierDeskLocation().equals(packet.pos)) {
                            validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_CASHIER_MISMATCH, List.of()));
                            resultType = ValidationResultType.ERROR;
                        }

                        // 验证桌子和座位
                        if (config.getTableLocations().isEmpty()) {
                            validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_NO_TABLES, List.of()));
                            resultType = ValidationResultType.ERROR;
                        } else {
                            for (TableInfo table : config.getTableLocations()) {
                                if (!table.isValid()) {
                                    validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_INVALID_TABLE, List.of(Component.literal(table.getCenter().toShortString()))));
                                    resultType = ValidationResultType.ERROR;
                                }
                            }
                        }

                        if (config.getSeatLocations().isEmpty()) {
                            validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_NO_SEATS, List.of()));
                            resultType = ValidationResultType.ERROR;
                        } else {
                            Set<BlockPos> allTableBlocks = config.getTableLocations().stream()
                                    .flatMap(table -> table.getLocations().stream())
                                    .collect(Collectors.toSet());

                            for (SeatInfo seat : config.getSeatLocations()) {
                                if (!seat.isValid()) {
                                    validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_INVALID_SEAT, List.of(Component.literal(seat.getLocation().toShortString()))));
                                    resultType = ValidationResultType.ERROR;
                                }

                                boolean connectedToTable = false;
                                for (BlockPos tableBlock : allTableBlocks) {
                                    if (seat.getLocation().distManhattan(tableBlock) == 1) { // 曼哈顿距离为1表示相邻
                                        connectedToTable = true;
                                        break;
                                    }
                                }
                                if (!connectedToTable) {
                                    validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_SEAT_NOT_CONNECTED_TO_TABLE, List.of(Component.literal(seat.getLocation().toShortString()))));
                                    resultType = ValidationResultType.ERROR;
                                }
                            }
                        }

                        // 验证路径图
                        PathGraph pathGraph = config.getPathGraph();
                        if (pathGraph == null || pathGraph.getNodes().isEmpty()) {
                            validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_NO_PATH, List.of()));
                            resultType = ValidationResultType.ERROR;
                        } else {
                            if (pathGraph.getEntry() == null) {
                                validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_NO_PATH_ENTRY, List.of()));
                                resultType = ValidationResultType.ERROR;
                            }
                            if (pathGraph.getExit() == null) {
                                validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_NO_PATH_EXIT, List.of()));
                                resultType = ValidationResultType.ERROR;
                            }
                            // 检查路径图连通性
                            if (!isPathGraphConnected(pathGraph)) {
                                validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_PATH_NOT_CONNECTED, List.of()));
                                resultType = ValidationResultType.ERROR;
                            }

                            // 检查入口和出口是否在节点列表中
                            if (pathGraph.getEntry() != null && !pathGraph.getNodes().contains(pathGraph.getEntry())) {
                                validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_PATH_ENTRY_NOT_IN_NODES, List.of()));
                                resultType = ValidationResultType.ERROR;
                            }
                            if (pathGraph.getExit() != null && !pathGraph.getNodes().contains(pathGraph.getExit())) {
                                validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_PATH_EXIT_NOT_IN_NODES, List.of()));
                                resultType = ValidationResultType.ERROR;
                            }

                            // 验证合法座位与路径图边的相邻性
                            for (SeatInfo seat : config.getSeatLocations()) {
                                if (seat.isValid()) {
                                    boolean adjacentToPath = false;
                                    for (List<BlockPos> edge : pathGraph.getEdges()) {
                                        if (isHorizontallyAdjacent(seat.getLocation(), edge.get(0)) ||
                                            isHorizontallyAdjacent(seat.getLocation(), edge.get(1))) {
                                            adjacentToPath = true;
                                            break;
                                        }
                                    }
                                    if (!adjacentToPath) {
                                        validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_SEAT_NOT_ADJACENT_TO_PATH, List.of(Component.literal(seat.getLocation().toShortString()))));
                                        resultType = ValidationResultType.ERROR;
                                    }
                                }
                            }
                        }

                        // 验证菜单物品
                        if (config.getMenuItems().isEmpty()) {
                            validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_NO_MENU_ITEMS, List.of()));
                            resultType = ValidationResultType.ERROR;
                        }

                        // 如果没有特定错误，但结果类型仍为 SUCCESS，则添加成功消息
                        if (validationMessages.isEmpty() && resultType == ValidationResultType.SUCCESS) {
                            validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_SUCCESS, List.of()));
                        } else if (validationMessages.isEmpty() && resultType == ValidationResultType.ERROR) {
                            // 如果有错误但没有具体消息，添加一个通用错误消息
                            validationMessages.add(new ValidationMessage(MessageType.SHOP_VALIDATED_ERROR_UNKNOWN, List.of()));
                        }


                        PacketDistributor.sendToPlayer(player, new ValidateShopResultPacket(resultType, validationMessages));
                    }
                }
            }
        });
    }

    /**
     * 检查两个BlockPos是否在水平方向上相邻（东南西北四个方向，Y坐标相同，X/Z平面曼哈顿距离为1）。
     */
    private static boolean isHorizontallyAdjacent(BlockPos pos1, BlockPos pos2) {
        return pos1.getY() == pos2.getY() &&
               (Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getZ() - pos2.getZ()) == 1);
    }

    /**
     * 检查路径图中的所有节点是否连通。
     * 使用BFS算法。
     */
    private static boolean isPathGraphConnected(PathGraph graph) {
        List<BlockPos> nodes = graph.getNodes();
        if (nodes.isEmpty()) {
            return true; // 空图被认为是连通的
        }

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        // 从第一个节点开始BFS
        BlockPos startNode = nodes.get(0);
        queue.offer(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            // 查找与当前节点相邻的节点（通过边连接）
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

        // 如果所有节点都被访问过，则图是连通的
        return visited.size() == nodes.size();
    }
}
