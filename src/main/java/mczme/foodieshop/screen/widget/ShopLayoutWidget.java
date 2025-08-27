package mczme.foodieshop.screen.widget;

import mczme.foodieshop.api.shop.SeatInfo;
import mczme.foodieshop.api.shop.ShopConfig;
import mczme.foodieshop.api.shop.TableInfo;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import mczme.foodieshop.network.packet.c2s.TogglePathNodeModePacket;
import mczme.foodieshop.screen.ShopConfigMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.joml.Vector2d;

import java.util.Objects;

public class ShopLayoutWidget {

    private float zoom = 10.0f;
    private final Vector2d pan = new Vector2d(0, 0);
    private SelectedElement selectedElement = null;
    private Vector2d lastMousePos = null;
    private boolean panInitialized = false;
    private long lastClickTime = 0;
    private BlockPos lastClickedPos = null;

    private final ShopConfigMenu menu;
    private final CashierDeskBlockEntity blockEntity;
    private final Font font;
    private final int screenLeftPos;
    private final int screenTopPos;
    private final int screenImageWidth;
    private final int screenImageHeight;

    public ShopLayoutWidget(ShopConfigMenu menu, CashierDeskBlockEntity blockEntity, Font font, int screenLeftPos, int screenTopPos, int screenImageWidth, int screenImageHeight) {
        this.menu = menu;
        this.blockEntity = blockEntity;
        this.font = font;
        this.screenLeftPos = screenLeftPos;
        this.screenTopPos = screenTopPos;
        this.screenImageWidth = screenImageWidth;
        this.screenImageHeight = screenImageHeight;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        ShopConfig config = this.menu.getShopConfig();
        BlockPos center = this.blockEntity.getBlockPos();

        int x = this.screenLeftPos + 10;
        int y = this.screenTopPos + 30;
        int width = this.screenImageWidth - 20;
        int height = this.screenImageHeight - 60;

        if (!panInitialized) {
            pan.set(x + width / 2.0, y + height / 2.0);
            panInitialized = true;
        }

        guiGraphics.enableScissor(x, y, x + width, y + height);

        renderShopArea(guiGraphics, config, center);
        renderWaypoints(guiGraphics, config, center);
        renderTables(guiGraphics, config, center);
        renderSeats(guiGraphics, config, center);
        renderInventories(guiGraphics, config, center);
        renderDeliveryBoxes(guiGraphics, config, center);
        renderCashierDesk(guiGraphics, config, center);

        guiGraphics.disableScissor();
        renderLegend(guiGraphics);
    }

    private void renderShopArea(GuiGraphics guiGraphics, ShopConfig config, BlockPos center) {
        if (config.getShopAreaPos1() != null && config.getShopAreaPos2() != null) {
            BlockPos pos1 = config.getShopAreaPos1();
            BlockPos pos2 = config.getShopAreaPos2();
            BlockPos minPos = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
            BlockPos maxPos = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
            Vector2d screenMin = worldToScreen(minPos, center);
            Vector2d screenMax = worldToScreen(maxPos, center);
            float halfZoom = zoom / 2.0f;
            int minX = (int) (screenMin.x - halfZoom);
            int minY = (int) (screenMin.y - halfZoom);
            int maxX = (int) (screenMax.x + halfZoom);
            int maxY = (int) (screenMax.y + halfZoom);
            guiGraphics.fill(minX, minY, maxX, maxY, 0x33FF0000);
            guiGraphics.renderOutline(minX, minY, maxX - minX, maxY - minY, 0xFFFF0000);
        }
    }

    private void renderTables(GuiGraphics guiGraphics, ShopConfig config, BlockPos center) {
        for (TableInfo table : config.getTableLocations()) {
            for (BlockPos tablePos : table.getLocations()) {
                Vector2d pos = worldToScreen(tablePos, center);
                float size = zoom;
                guiGraphics.fill((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) (pos.x + size / 2), (int) (pos.y + size / 2), 0xFF8B4513);
                if (selectedElement != null && selectedElement.type == ElementType.TABLE && selectedElement.element.equals(table)) {
                    guiGraphics.renderOutline((int) (pos.x - size / 2 - 1), (int) (pos.y - size / 2 - 1), (int) size + 2, (int) size + 2, 0xFFFFFF00);
                }
            }
        }
    }

    private void renderSeats(GuiGraphics guiGraphics, ShopConfig config, BlockPos center) {
        for (SeatInfo seat : config.getSeatLocations()) {
            Vector2d pos = worldToScreen(seat.getLocation(), center);
            float size = zoom;
            guiGraphics.fill((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) (pos.x + size / 2), (int) (pos.y + size / 2), 0xFF00FF00);
            if (selectedElement != null && selectedElement.type == ElementType.SEAT && selectedElement.element.equals(seat)) {
                guiGraphics.renderOutline((int) (pos.x - size / 2 - 1), (int) (pos.y - size / 2 - 1), (int) size + 2, (int) size + 2, 0xFFFFFF00);
            }
        }
    }

    private void renderInventories(GuiGraphics guiGraphics, ShopConfig config, BlockPos center) {
        for (BlockPos inventoryPos : config.getInventoryLocations()) {
            Vector2d pos = worldToScreen(inventoryPos, center);
            float size = zoom;
            guiGraphics.fill((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) (pos.x + size / 2), (int) (pos.y + size / 2), 0xFF00008B);
            if (selectedElement != null && selectedElement.type == ElementType.INVENTORY && selectedElement.element.equals(inventoryPos)) {
                guiGraphics.renderOutline((int) (pos.x - size / 2 - 1), (int) (pos.y - size / 2 - 1), (int) size + 2, (int) size + 2, 0xFFFFFF00);
            }
        }
    }

    private void renderDeliveryBoxes(GuiGraphics guiGraphics, ShopConfig config, BlockPos center) {
        for (BlockPos deliveryBoxPos : config.getDeliveryBoxLocations()) {
            Vector2d pos = worldToScreen(deliveryBoxPos, center);
            float size = zoom;
            guiGraphics.fill((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) (pos.x + size / 2), (int) (pos.y + size / 2), 0xFFFFD700); // Orange for delivery boxes
            if (selectedElement != null && selectedElement.type == ElementType.DELIVERY_BOX && selectedElement.element.equals(deliveryBoxPos)) {
                guiGraphics.renderOutline((int) (pos.x - size / 2 - 1), (int) (pos.y - size / 2 - 1), (int) size + 2, (int) size + 2, 0xFFFFFF00);
            }
        }
    }

    private void renderCashierDesk(GuiGraphics guiGraphics, ShopConfig config, BlockPos center) {
        BlockPos cashierDeskPos = config.getCashierDeskLocation();
        if (cashierDeskPos != null) {
            Vector2d pos = worldToScreen(cashierDeskPos, center);
            float size = zoom;
            guiGraphics.fill((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) (pos.x + size / 2), (int) (pos.y + size / 2), 0xFFFFA500); // Gold for cashier desk
            if (selectedElement != null && selectedElement.type == ElementType.CASHIER_DESK && selectedElement.element.equals(cashierDeskPos)) {
                guiGraphics.renderOutline((int) (pos.x - size / 2 - 1), (int) (pos.y - size / 2 - 1), (int) size + 2, (int) size + 2, 0xFFFFFF00);
            }
        }
    }

    private void renderWaypoints(GuiGraphics guiGraphics, ShopConfig config, BlockPos center) {
        if (config.getPathGraph() == null) return;
        for (java.util.List<BlockPos> edge : config.getPathGraph().getEdges()) {
            if (edge.size() == 2) {
                Vector2d pos1 = worldToScreen(edge.get(0), center);
                Vector2d pos2 = worldToScreen(edge.get(1), center);
                guiGraphics.hLine((int) pos1.x, (int) pos2.x, (int) pos1.y, 0xFF0000FF);
                guiGraphics.vLine((int) pos2.x, (int) pos1.y, (int) pos2.y, 0xFF0000FF);
            }
        }
        for (BlockPos waypoint : config.getPathGraph().getNodes()) {
            Vector2d currentPos = worldToScreen(waypoint, center);
            float size = zoom * 0.6f;
            int color = 0xFF0000FF; // 默认颜色：蓝色
            if (waypoint.equals(config.getPathGraph().getEntry())) {
                color = 0xFF00FF00; // 人口颜色：绿色
            } else if (waypoint.equals(config.getPathGraph().getExit())) {
                color = 0xFFFF0000; // 出口颜色：红色
            }
            guiGraphics.fill((int) (currentPos.x - size / 2), (int) (currentPos.y - size / 2), (int) (currentPos.x + size / 2), (int) (currentPos.y + size / 2), color);
            if (selectedElement != null && selectedElement.type == ElementType.WAYPOINT && selectedElement.element.equals(waypoint)) {
                guiGraphics.renderOutline((int) (currentPos.x - size / 2 - 1), (int) (currentPos.y - size / 2 - 1), (int) size + 2, (int) size + 2, 0xFFFFFF00);
            }
        }
    }

    private void renderLegend(GuiGraphics guiGraphics) {
        int areaX = this.screenLeftPos + 10;
        int areaY = this.screenTopPos + 30;
        int areaWidth = this.screenImageWidth - 20;
        int areaHeight = this.screenImageHeight - 60;

        int legendWidth = 60;
        int legendHeight = 95;
        int x = areaX + areaWidth - legendWidth + 10;
        int y = areaY + areaHeight - legendHeight - 15;

        int legendColor = 0xA0FFFFFF;
        int borderColor = 0xFF000000;
        guiGraphics.fill(x - 2, y - 2, x + legendWidth - 10, y + legendHeight - 15, legendColor);
        guiGraphics.renderOutline(x - 2, y - 2, legendWidth-8, legendHeight-13, borderColor);

        int iconSize = 8;
        int spacing = 10;
        int currentY = y;

        guiGraphics.fill(x, currentY, x + iconSize, currentY + iconSize, 0xFF8B4513);
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.legend.table"), x + iconSize + 4, currentY, 0x404040, false);
        currentY += spacing;

        guiGraphics.fill(x, currentY, x + iconSize, currentY + iconSize, 0xFF00FF00);
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.legend.seat"), x + iconSize + 4, currentY, 0x404040, false);
        currentY += spacing;

        guiGraphics.fill(x, currentY, x + iconSize, currentY + iconSize, 0xFF00008B);
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.legend.inventory"), x + iconSize + 4, currentY, 0x404040, false);
        currentY += spacing;

        guiGraphics.fill(x, currentY, x + iconSize, currentY + iconSize, 0xFFFFD700);
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.legend.delivery_box"), x + iconSize + 4, currentY, 0x404040, false);
        currentY += spacing;

        guiGraphics.fill(x, currentY, x + iconSize, currentY + iconSize, 0xFFFFA500);
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.legend.cashier_desk"), x + iconSize + 4, currentY, 0x404040, false);
        currentY += spacing;

        int waypointIconSize = (int) (iconSize * 0.8);
        int waypointIconOffset = (iconSize - waypointIconSize) / 2;
        guiGraphics.fill(x + waypointIconOffset, currentY + waypointIconOffset, x + waypointIconSize, currentY + waypointIconSize, 0xFF0000FF);
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.legend.waypoint"), x + iconSize + 4, currentY, 0x404040, false);
        currentY += spacing;

        guiGraphics.fill(x + waypointIconOffset, currentY + waypointIconOffset, x + waypointIconSize, currentY + waypointIconSize, 0xFF00FF00);
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.legend.entry"), x + iconSize + 4, currentY, 0x404040, false);
        currentY += spacing;

        guiGraphics.fill(x + waypointIconOffset, currentY + waypointIconOffset, x + waypointIconSize, currentY + waypointIconSize, 0xFFFF0000);
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.legend.exit"), x + iconSize + 4, currentY, 0x404040, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        lastMousePos = new Vector2d(mouseX, mouseY);
        ShopConfig config = this.menu.getShopConfig();
        BlockPos center = this.blockEntity.getBlockPos();
        selectedElement = null;
        for (SeatInfo seat : config.getSeatLocations()) {
            Vector2d pos = worldToScreen(seat.getLocation(), center);
            if (Math.abs(pos.x - mouseX) < zoom / 2 && Math.abs(pos.y - mouseY) < zoom / 2) {
                selectedElement = new SelectedElement(ElementType.SEAT, seat);
                return true;
            }
        }
        for (TableInfo table : config.getTableLocations()) {
            for (BlockPos tablePos : table.getLocations()) {
                Vector2d pos = worldToScreen(tablePos, center);
                if (Math.abs(pos.x - mouseX) < zoom / 2 && Math.abs(pos.y - mouseY) < zoom / 2) {
                    selectedElement = new SelectedElement(ElementType.TABLE, table);
                    return true;
                }
            }
        }
        for (BlockPos inventoryPos : config.getInventoryLocations()) {
            Vector2d pos = worldToScreen(inventoryPos, center);
            if (Math.abs(pos.x - mouseX) < zoom / 2 && Math.abs(pos.y - mouseY) < zoom / 2) {
                selectedElement = new SelectedElement(ElementType.INVENTORY, inventoryPos);
                return true;
            }
        }
        for (BlockPos deliveryBoxPos : config.getDeliveryBoxLocations()) {
            Vector2d pos = worldToScreen(deliveryBoxPos, center);
            if (Math.abs(pos.x - mouseX) < zoom / 2 && Math.abs(pos.y - mouseY) < zoom / 2) {
                selectedElement = new SelectedElement(ElementType.DELIVERY_BOX, deliveryBoxPos);
                return true;
            }
        }
        BlockPos cashierDeskPos = config.getCashierDeskLocation();
        if (cashierDeskPos != null) {
            Vector2d pos = worldToScreen(cashierDeskPos, center);
            if (Math.abs(pos.x - mouseX) < zoom / 2 && Math.abs(pos.y - mouseY) < zoom / 2) {
                selectedElement = new SelectedElement(ElementType.CASHIER_DESK, cashierDeskPos);
                return true;
            }
        }
        if (config.getPathGraph() != null) {
            for (BlockPos waypoint : config.getPathGraph().getNodes()) {
                Vector2d pos = worldToScreen(waypoint, center);
                if (Math.abs(pos.x - mouseX) < zoom * 0.3f && Math.abs(pos.y - mouseY) < zoom * 0.3f) {
                    long time = System.currentTimeMillis();
                    if (time - lastClickTime < 250 && waypoint.equals(lastClickedPos)) {
                        // 双击
                        if (Minecraft.getInstance().getConnection() != null) {
                            Minecraft.getInstance().getConnection().send(new TogglePathNodeModePacket(this.blockEntity.getBlockPos(), waypoint));
                        }
                    } else {
                        // 单击
                        selectedElement = new SelectedElement(ElementType.WAYPOINT, waypoint);
                    }
                    lastClickTime = time;
                    lastClickedPos = waypoint;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        lastMousePos = null;
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (lastMousePos != null) {
            pan.add(new Vector2d(mouseX, mouseY).sub(lastMousePos));
            lastMousePos.set(mouseX, mouseY);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        float oldZoom = this.zoom;
        float zoomFactor = (float) Math.pow(1.1, scrollY);
        this.zoom *= zoomFactor;
        this.zoom = Math.max(1.0f, Math.min(this.zoom, 50.0f));
        double mouseXRel = mouseX - this.pan.x;
        double mouseYRel = mouseY - this.pan.y;
        this.pan.x -= mouseXRel * (this.zoom / oldZoom - 1);
        this.pan.y -= mouseYRel * (this.zoom / oldZoom - 1);
        return true;
    }

    private Vector2d worldToScreen(BlockPos pos, BlockPos center) {
        double screenX = (pos.getX() - center.getX()) * zoom + pan.x;
        double screenY = (pos.getZ() - center.getZ()) * zoom + pan.y;
        return new Vector2d(screenX, screenY);
    }

    private enum ElementType {
        SEAT,
        TABLE,
        WAYPOINT,
        INVENTORY,
        DELIVERY_BOX,
        CASHIER_DESK
    }

    private record SelectedElement(ElementType type, Object element) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SelectedElement that = (SelectedElement) o;
            return type == that.type && Objects.equals(element, that.element);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, element);
        }
    }
}
