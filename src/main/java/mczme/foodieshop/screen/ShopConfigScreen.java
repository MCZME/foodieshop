package mczme.foodieshop.screen;

import mczme.foodieshop.api.shop.SeatInfo;
import mczme.foodieshop.api.shop.ShopConfig;
import mczme.foodieshop.api.shop.TableInfo;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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


    public ShopConfigScreen(ShopConfigMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.blockEntity = menu.getBlockEntity();
        this.imageWidth = 220;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        // Tabs
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

        // 底部按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.close"), (button) -> this.onClose())
                .pos(this.leftPos + 5, this.topPos + this.imageHeight - 25)
                .size(50, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.save"), (button) -> {
            // TODO: 发送数据包到服务器以保存配置
            this.onClose();
        })
                .pos(this.leftPos + this.imageWidth - 55, this.topPos + this.imageHeight - 25)
                .size(50, 20)
                .build());

        this.updateWidgets();
    }

    private void setCurrentTab(Tabs tab) {
        this.currentTab = tab;
        this.updateWidgets();
    }

    private void updateWidgets() {
        this.clearWidgets();
        // Re-add common widgets
        // Tabs
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

        // Bottom buttons
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.close"), (button) -> this.onClose())
                .pos(this.leftPos + 5, this.topPos + this.imageHeight - 25)
                .size(50, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.save"), (button) -> {
            // TODO: Send packet to server to save config
            this.onClose();
        })
                .pos(this.leftPos + this.imageWidth - 55, this.topPos + this.imageHeight - 25)
                .size(50, 20)
                .build());

        // Add widgets based on the current tab
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
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX,
            int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Render content for the current tab
        switch (this.currentTab) {
            case GENERAL:
                renderGeneralSettings(guiGraphics, mouseX, mouseY, partialTicks);
                break;
            case LAYOUT:
                renderAreaAndLayout(guiGraphics, mouseX, mouseY, partialTicks);
                break;
            case SAVE:
                renderSaveAndValidate(guiGraphics, mouseX, mouseY, partialTicks);
                break;
        }
    }

    private void initGeneralSettingsWidgets() {
        // 菜单库存容器
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.select_container"), (button) -> {
            // TODO: 发送数据包到服务器以进入菜单容器的选择模式
        })
                .pos(this.leftPos + 120, this.topPos + 88)
                .size(50, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.clear_link"), (button) -> {
            // TODO: 发送数据包到服务器以清除菜单容器的链接
        })
                .pos(this.leftPos + 175, this.topPos + 88)
                .size(40, 20)
                .build());

        // 收银箱容器
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.select_container"), (button) -> {
            // TODO: 发送数据包到服务器以进入收银箱的选择模式
        })
                .pos(this.leftPos + 120, this.topPos + 128)
                .size(50, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.clear_link"), (button) -> {
            // TODO: 发送数据包到服务器以清除收银箱的链接
        })
                .pos(this.leftPos + 175, this.topPos + 128)
                .size(40, 20)
                .build());
    }

    private void renderGeneralSettings(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.owner_uuid"), this.leftPos + 10, this.topPos + 35, 0x404040, false);
        String ownerUUID = this.blockEntity.getOwnerUUID() != null ? this.blockEntity.getOwnerUUID().toString() : "Not set";
        guiGraphics.drawString(this.font, ownerUUID, this.leftPos + 15, this.topPos + 45, 0x7F7F7F, false);

        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.cashier_pos"), this.leftPos + 10, this.topPos + 60, 0x404040, false);
        guiGraphics.drawString(this.font, this.blockEntity.getBlockPos().toShortString(), this.leftPos + 15, this.topPos + 70, 0x7F7F7F, false);

        // Menu Inventory Container
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.menu_container"), this.leftPos + 10, this.topPos + 90, 0x404040, false);
        String menuContainerPos = this.blockEntity.getMenuContainerPos() != null ? this.blockEntity.getMenuContainerPos().toShortString() : "Not set";
        guiGraphics.drawString(this.font, menuContainerPos, this.leftPos + 15, this.topPos + 100, 0x7F7F7F, false);

        // Cash Box Container
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.cash_box_container"), this.leftPos + 10, this.topPos + 130, 0x404040, false);
        String cashBoxPos = this.blockEntity.getCashBoxPos() != null ? this.blockEntity.getCashBoxPos().toShortString() : "Not set";
        guiGraphics.drawString(this.font, cashBoxPos, this.leftPos + 15, this.topPos + 140, 0x7F7F7F, false);
    }

    private void initAreaAndLayoutWidgets() {
        // 店铺区域
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.redefine_area"), (button) -> {
            // TODO: 发送数据包到服务器以激活蓝图笔进行区域选择
        })
                .pos(this.leftPos + 115, this.topPos + 45)
                .size(95, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.preview_area"), (button) -> {
            // TODO: 发送数据包到服务器以请求区域预览
        })
                .pos(this.leftPos + 115, this.topPos + 68)
                .size(95, 20)
                .build());

        // 座位
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.edit_seats"), (button) -> {
            // TODO: 发送数据包以激活蓝图笔进行座位设置
        })
                .pos(this.leftPos + 115, this.topPos + 95)
                .size(95, 20)
                .build());

        // 桌子
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.edit_tables"), (button) -> {
            // TODO: 发送数据包以激活蓝图笔进行桌子设置
        })
                .pos(this.leftPos + 115, this.topPos + 120)
                .size(95, 20)
                .build());

        // 路径点
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.edit_waypoints"), (button) -> {
            // TODO: 发送数据包以激活蓝图笔进行路径点设置
        })
                .pos(this.leftPos + 115, this.topPos + 145)
                .size(95, 20)
                .build());
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
        for (int i = 0; i < config.getTableLocations().size(); i++) {
            TableInfo table = config.getTableLocations().get(i);
            for (BlockPos tablePos : table.getLocations()) {
                Vector2d pos = worldToScreen(tablePos, center);
                float size = zoom;
                guiGraphics.fill((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) (pos.x + size / 2), (int) (pos.y + size / 2), 0xFF8B4513); // SaddleBrown

                if (selectedElement != null && selectedElement.type == ElementType.TABLE && selectedElement.index == i) {
                    guiGraphics.renderOutline((int) (pos.x - size / 2 - 1), (int) (pos.y - size / 2 - 1), (int) size + 2, (int) size + 2, 0xFFFFFF00); // Yellow
                }
            }
        }
    }

    private void renderSeats(GuiGraphics guiGraphics, ShopConfig config, BlockPos center) {
        for (int i = 0; i < config.getSeatLocations().size(); i++) {
            SeatInfo seat = config.getSeatLocations().get(i);
            Vector2d pos = worldToScreen(seat.getLocation(), center);
            float size = zoom;
            guiGraphics.fill((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) (pos.x + size / 2), (int) (pos.y + size / 2), 0xFF00FF00); // Lime

            if (selectedElement != null && selectedElement.type == ElementType.SEAT && selectedElement.index == i) {
                guiGraphics.renderOutline((int) (pos.x - size / 2 - 1), (int) (pos.y - size / 2 - 1), (int) size + 2, (int) size + 2, 0xFFFFFF00); // Yellow
            }
        }
    }

    private void renderWaypoints(GuiGraphics guiGraphics, ShopConfig config, BlockPos center) {
        if (config.getPathGraph() == null) return;

        // Render edges
        for (java.util.List<BlockPos> edge : config.getPathGraph().getEdges()) {
            if (edge.size() == 2) {
                Vector2d pos1 = worldToScreen(edge.get(0), center);
                Vector2d pos2 = worldToScreen(edge.get(1), center);
                guiGraphics.hLine((int) pos1.x, (int) pos2.x, (int) pos1.y, 0xFF0000FF);
                guiGraphics.vLine((int) pos2.x, (int) pos1.y, (int) pos2.y, 0xFF0000FF);
            }
        }

        // Render nodes
        for (int i = 0; i < config.getPathGraph().getNodes().size(); i++) {
            BlockPos waypoint = config.getPathGraph().getNodes().get(i);
            Vector2d currentPos = worldToScreen(waypoint, center);
            float size = zoom * 0.6f; // Waypoints are slightly smaller
            guiGraphics.fill((int) (currentPos.x - size / 2), (int) (currentPos.y - size / 2), (int) (currentPos.x + size / 2), (int) (currentPos.y + size / 2), 0xFF0000FF); // Blue

            if (selectedElement != null && selectedElement.type == ElementType.WAYPOINT && selectedElement.index == i) {
                guiGraphics.renderOutline((int) (currentPos.x - size / 2 - 1), (int) (currentPos.y - size / 2 - 1), (int) size + 2, (int) size + 2, 0xFFFFFF00); // Yellow
            }
        }
    }

    private void initSaveAndValidateWidgets() {
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.validate"), (button) -> {
            // TODO: 发送数据包到服务器以运行验证
            // 目前，只设置一个占位符消息
            this.validationMessage = Component.literal("验证完成。一切正常！");
        })
                .pos(this.leftPos + 10, this.topPos + 35)
                .size(100, 20)
                .build());
    }

    private void renderSaveAndValidate(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.validation_results"), this.leftPos + 10, this.topPos + 65, 0x404040, false);
        guiGraphics.drawString(this.font, this.validationMessage, this.leftPos + 15, this.topPos + 78, 0x7F7F7F, false);

        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.logs"), this.leftPos + 10, this.topPos + 100, 0x404040, false);
        // TODO: 显示日志或提示
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

            // Check seats
            for (int i = 0; i < config.getSeatLocations().size(); i++) {
                Vector2d pos = worldToScreen(config.getSeatLocations().get(i).getLocation(), center);
                if (Math.abs(pos.x - mouseX) < zoom / 2 && Math.abs(pos.y - mouseY) < zoom / 2) {
                    selectedElement = new SelectedElement(ElementType.SEAT, i);
                    return true;
                }
            }

            // Check tables
            for (int i = 0; i < config.getTableLocations().size(); i++) {
                for (BlockPos tablePos : config.getTableLocations().get(i).getLocations()) {
                    Vector2d pos = worldToScreen(tablePos, center);
                    if (Math.abs(pos.x - mouseX) < zoom / 2 && Math.abs(pos.y - mouseY) < zoom / 2) {
                        selectedElement = new SelectedElement(ElementType.TABLE, i);
                        return true;
                    }
                }
            }

            // Check waypoints
            if (config.getPathGraph() != null) {
                for (int i = 0; i < config.getPathGraph().getNodes().size(); i++) {
                    Vector2d pos = worldToScreen(config.getPathGraph().getNodes().get(i), center);
                    if (Math.abs(pos.x - mouseX) < zoom * 0.3f && Math.abs(pos.y - mouseY) < zoom * 0.3f) {
                        selectedElement = new SelectedElement(ElementType.WAYPOINT, i);
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

            // Adjust pan to zoom towards the mouse cursor
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
        WAYPOINT
    }

    private record SelectedElement(ElementType type, int index) {
    }
}
