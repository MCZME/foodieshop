package mczme.foodieshop.screen;

import mczme.foodieshop.api.shop.SeatInfo;
import mczme.foodieshop.api.shop.ShopConfig;
import mczme.foodieshop.api.shop.TableInfo;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import mczme.foodieshop.network.packet.c2s.ResetLayoutPacket;
import mczme.foodieshop.network.packet.c2s.UpdateShopConfigPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector2d;

@OnlyIn(Dist.CLIENT)
public class ShopConfigScreen extends AbstractContainerScreen<ShopConfigMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("foodieshop", "textures/gui/shop_config.png");
    private final CashierDeskBlockEntity blockEntity;

    private Tabs currentTab = Tabs.GENERAL;
    private Component validationMessage = Component.empty();

    private float zoom = 10.0f;
    private final Vector2d pan = new Vector2d(0, 0);
    private SelectedElement selectedElement = null;
    private Vector2d lastMousePos = null;
    private boolean panInitialized = false;
    private EditBox shopNameEditBox;

    public ShopConfigScreen(ShopConfigMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.blockEntity = menu.getBlockEntity();
        this.imageWidth = 220;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();
        this.updateWidgets();
    }

    private void setCurrentTab(Tabs tab) {
        this.currentTab = tab;
        this.updateWidgets();
    }

    private void updateWidgets() {
        this.clearWidgets();
        // 选项卡
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.tab.general"), (button) -> this.setCurrentTab(Tabs.GENERAL))
                .pos(this.leftPos + 5, this.topPos + 5)
                .size(60, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.tab.layout"), (button) -> this.setCurrentTab(Tabs.LAYOUT))
                .pos(this.leftPos + 70, this.topPos + 5)
                .size(70, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.tab.save"), (button) -> this.setCurrentTab(Tabs.SAVE))
                .pos(this.leftPos + 145, this.topPos + 5)
                .size(70, 20)
                .build());

        // 根据当前选项卡添加控件和底部按钮
        switch (this.currentTab) {
            case GENERAL:
                initGeneralSettingsWidgets();
                break;
            case LAYOUT:
                initAreaAndLayoutWidgets();
                break;
            case SAVE:
                initSaveAndValidateWidgets();
                break;
        }
        
        // 所有选项卡通用的底部按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.close"), (button) -> this.onClose())
                .pos(this.leftPos + 5, this.topPos + this.imageHeight - 25)
                .size(50, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.save"), (button) -> this.saveAndClose())
                .pos(this.leftPos + this.imageWidth - 55, this.topPos + this.imageHeight - 25)
                .size(50, 20)
                .build());

        // 特定选项卡的底部按钮
        if (this.currentTab == Tabs.LAYOUT) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.reset_layout"), (button) -> {
                this.minecraft.setScreen(new ConfirmScreen(
                        (confirmed) -> {
                            if (confirmed) {
                                ResetLayoutPacket packet = new ResetLayoutPacket(this.blockEntity.getBlockPos());
                                if (Minecraft.getInstance().getConnection() != null) {
                                    Minecraft.getInstance().getConnection().send(packet);
                                }
                            }
                            this.minecraft.setScreen(this);
                        },
                        Component.translatable("gui.foodieshop.shop_config.reset_layout.confirm.title"),
                        Component.translatable("gui.foodieshop.shop_config.reset_layout.confirm.message")
                ));
            })
                    .pos(this.leftPos + (this.imageWidth / 2) - 40, this.topPos + this.imageHeight - 25)
                    .size(80, 20)
                    .build());
        } else if (this.currentTab == Tabs.SAVE) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.validate"), (button) -> {
                // TODO: 发送数据包到服务器以运行验证
                this.validationMessage = Component.literal("验证完成。一切正常！");
            })
                    .pos(this.leftPos + (this.imageWidth / 2) - 40, this.topPos + this.imageHeight - 25)
                    .size(80, 20)
                    .build());
        }
    }

    private void saveAndClose() {
        // 保存前确保从编辑框获取最新值
        if (this.shopNameEditBox != null && this.currentTab == Tabs.GENERAL) {
            this.menu.getShopConfig().setShopName(this.shopNameEditBox.getValue());
        }
        
        UpdateShopConfigPacket packet = new UpdateShopConfigPacket(this.blockEntity.getBlockPos(), this.menu.getShopConfig());
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(packet);
        }
        this.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 无操作
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // 渲染当前选项卡的内容
        switch (this.currentTab) {
            case GENERAL:
                renderGeneralSettings(guiGraphics, mouseX, mouseY, partialTicks);
                break;
            case LAYOUT:
                renderAreaAndLayout(guiGraphics, mouseX, mouseY, partialTicks);
                renderLegend(guiGraphics);
                break;
            case SAVE:
                renderSaveAndValidate(guiGraphics, mouseX, mouseY, partialTicks);
                break;
        }
    }

    private void initGeneralSettingsWidgets() {
        this.shopNameEditBox = new EditBox(this.font, this.leftPos + 15, this.topPos + 70, 150, 18, Component.translatable("gui.foodieshop.shop_config.shop_name"));
        this.shopNameEditBox.setValue(this.menu.getShopConfig().getShopName());
        this.shopNameEditBox.setResponder((text) -> this.menu.getShopConfig().setShopName(text));
        this.addRenderableWidget(this.shopNameEditBox);
    }

    private void renderGeneralSettings(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        ShopConfig config = this.menu.getShopConfig();

        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.owner_name"), this.leftPos + 10, this.topPos + 35, 0x404040, false);
        String ownerName = config.getOwnerName().isEmpty() ? "Not set" : config.getOwnerName();
        guiGraphics.drawString(this.font, ownerName, this.leftPos + 15, this.topPos + 45, 0x7F7F7F, false);

        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.shop_name"), this.leftPos + 10, this.topPos + 60, 0x404040, false);
        // EditBox 会自行渲染。

        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.shop_location"), this.leftPos + 10, this.topPos + 95, 0x404040, false);
        guiGraphics.drawString(this.font, this.blockEntity.getBlockPos().toShortString(), this.leftPos + 15, this.topPos + 105, 0x7F7F7F, false);
    }

    private void initAreaAndLayoutWidgets() {
        // 此选项卡的主区域不需要控件
    }

    private void renderAreaAndLayout(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        ShopConfig config = this.menu.getShopConfig();
        BlockPos center = this.blockEntity.getBlockPos();

        int x = this.leftPos + 10;
        int y = this.topPos + 30;
        int width = this.imageWidth - 20;
        int height = this.imageHeight - 60;

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
            guiGraphics.fill((int) (currentPos.x - size / 2), (int) (currentPos.y - size / 2), (int) (currentPos.x + size / 2), (int) (currentPos.y + size / 2), 0xFF0000FF);
            if (selectedElement != null && selectedElement.type == ElementType.WAYPOINT && selectedElement.element.equals(waypoint)) {
                guiGraphics.renderOutline((int) (currentPos.x - size / 2 - 1), (int) (currentPos.y - size / 2 - 1), (int) size + 2, (int) size + 2, 0xFFFFFF00);
            }
        }
    }

    private void renderLegend(GuiGraphics guiGraphics) {
        int areaX = this.leftPos + 10;
        int areaY = this.topPos + 30;
        int areaWidth = this.imageWidth - 20;
        int areaHeight = this.imageHeight - 60;

        int legendWidth = 84;
        int legendHeight = 74;
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
    }

    private void initSaveAndValidateWidgets() {
        // 此选项卡的主区域不需要控件
    }

    private void renderSaveAndValidate(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.validation_results"), this.leftPos + 10, this.topPos + 65, 0x404040, false);
        guiGraphics.drawString(this.font, this.validationMessage, this.leftPos + 15, this.topPos + 78, 0x7F7F7F, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.logs"), this.leftPos + 10, this.topPos + 100, 0x404040, false);
        guiGraphics.drawString(this.font, "日志将显示在这里。", this.leftPos + 15, this.topPos + 113, 0x7F7F7F, false);
    }

    private Vector2d worldToScreen(BlockPos pos, BlockPos center) {
        double screenX = (pos.getX() - center.getX()) * zoom + pan.x;
        double screenY = (pos.getZ() - center.getZ()) * zoom + pan.y;
        return new Vector2d(screenX, screenY);
    }

    private BlockPos screenToWorld(double mouseX, double mouseY, BlockPos center) {
        int worldX = (int) Math.round(((mouseX - pan.x) / zoom) + center.getX());
        int worldZ = (int) Math.round(((mouseY - pan.y) / zoom) + center.getZ());
        return new BlockPos(worldX, center.getY(), worldZ);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.currentTab == Tabs.LAYOUT) {
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
                        selectedElement = new SelectedElement(ElementType.WAYPOINT, waypoint);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        lastMousePos = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.currentTab == Tabs.LAYOUT && lastMousePos != null) {
            pan.add(new Vector2d(mouseX, mouseY).sub(lastMousePos));
            lastMousePos.set(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.currentTab == Tabs.LAYOUT) {
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
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    enum Tabs {
        GENERAL,
        LAYOUT,
        SAVE
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
    }
}
