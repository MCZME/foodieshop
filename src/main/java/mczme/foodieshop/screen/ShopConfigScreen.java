package mczme.foodieshop.screen;

import mczme.foodieshop.api.shop.ShopConfig;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShopConfigScreen extends AbstractContainerScreen<ShopConfigMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("foodieshop", "textures/gui/shop_config.png");
    private final CashierDeskBlockEntity blockEntity;

    private Tabs currentTab = Tabs.GENERAL;
    private Component validationMessage = Component.empty();

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
    protected void renderBg(net.minecraft.client.gui.GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
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

    private void renderGeneralSettings(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
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

    private void renderAreaAndLayout(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        ShopConfig config = this.menu.getShopConfig();

        // 区域
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.shop_area"), this.leftPos + 10, this.topPos + 35, 0x404040, false);
        String pos1 = config.getShopAreaPos1() != null ? config.getShopAreaPos1().toShortString() : "未设置";
        String pos2 = config.getShopAreaPos2() != null ? config.getShopAreaPos2().toShortString() : "未设置";
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.pos1", pos1), this.leftPos + 15, this.topPos + 48, 0x7F7F7F, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.pos2", pos2), this.leftPos + 15, this.topPos + 60, 0x7F7F7F, false);

        // 座位
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.seats", config.getSeatLocations().size()), this.leftPos + 10, this.topPos + 100, 0x404040, false);

        // 桌子
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.tables", config.getTableLocations().size()), this.leftPos + 10, this.topPos + 125, 0x404040, false);

        // 路径点
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.waypoints", config.getShopPathWaypoints().size()), this.leftPos + 10, this.topPos + 150, 0x404040, false);
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

    private void renderSaveAndValidate(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.validation_results"), this.leftPos + 10, this.topPos + 65, 0x404040, false);
        guiGraphics.drawString(this.font, this.validationMessage, this.leftPos + 15, this.topPos + 78, 0x7F7F7F, false);

        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.logs"), this.leftPos + 10, this.topPos + 100, 0x404040, false);
        // TODO: 显示日志或提示
        guiGraphics.drawString(this.font, "日志将显示在这里。", this.leftPos + 15, this.topPos + 113, 0x7F7F7F, false);
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
}
