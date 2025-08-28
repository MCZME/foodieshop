package mczme.foodieshop.screen;

import mczme.foodieshop.api.shop.ShopConfig;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import mczme.foodieshop.network.packet.c2s.ResetLayoutPacket;
import mczme.foodieshop.network.packet.c2s.ToggleBusinessStatusPacket;
import mczme.foodieshop.network.packet.c2s.UpdateShopConfigPacket;
import mczme.foodieshop.network.packet.c2s.ValidateShopPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import mczme.foodieshop.network.packet.c2s.RequestStockContentsPacket;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import mczme.foodieshop.network.packet.s2c.ValidateShopResultPacket;
import mczme.foodieshop.screen.widget.ShopMenuInventoryWidget;
import mczme.foodieshop.screen.widget.ShopValidationWidget;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ShopConfigScreen extends AbstractContainerScreen<ShopConfigMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("foodieshop", "textures/gui/shop_config.png");
    private final CashierDeskBlockEntity blockEntity;

    private Tabs currentTab = Tabs.GENERAL;

    private EditBox shopNameEditBox;
    private mczme.foodieshop.screen.widget.ShopLayoutWidget shopLayoutWidget;
    private ShopMenuInventoryWidget shopMenuInventoryWidget;
    private ShopValidationWidget shopValidationWidget;

    public ShopConfigScreen(ShopConfigMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.blockEntity = menu.getBlockEntity();
        this.imageWidth = 240;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();
        this.shopLayoutWidget = new mczme.foodieshop.screen.widget.ShopLayoutWidget(this.menu, this.blockEntity, this.font, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        // 初始化新的组件
        // stockContents 初始为空，等待服务器更新
        List<ItemStack> initialStockContents = new ArrayList<>();
        Set<Item> initialMenuItems = new HashSet<>(this.menu.getShopConfig().getMenuItems());
        this.shopMenuInventoryWidget = new ShopMenuInventoryWidget(this.menu, this.font, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, initialStockContents, initialMenuItems);
        this.shopValidationWidget = new ShopValidationWidget(this.menu, this.blockEntity, this.font, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, this::addRenderableWidget);

        this.updateWidgets();
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(new RequestStockContentsPacket(this.blockEntity.getBlockPos()));
        }
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
                .size(50, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.tab.layout"), (button) -> this.setCurrentTab(Tabs.LAYOUT))
                .pos(this.leftPos + 57, this.topPos + 5)
                .size(55, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.tab.menu_inventory"), (button) -> this.setCurrentTab(Tabs.MENU_INVENTORY))
                .pos(this.leftPos + 114, this.topPos + 5)
                .size(60, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.tab.save"), (button) -> this.setCurrentTab(Tabs.SAVE))
                .pos(this.leftPos + 176, this.topPos + 5)
                .size(60, 20)
                .build());

        if (this.currentTab == Tabs.GENERAL) {
            initGeneralSettingsWidgets();
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
                        if (Minecraft.getInstance().getConnection() != null) {
                            Minecraft.getInstance().getConnection().send(new ValidateShopPacket(this.blockEntity.getBlockPos()));
                        }
                    })
                    .pos(this.leftPos + 65, this.topPos + this.imageHeight - 25)
                    .size(50, 20)
                    .build());
            this.addRenderableWidget(Button.builder(Component.translatable("gui.foodieshop.shop_config.business"), (button) -> {
                        if (this.shopValidationWidget.getResultType() != ValidateShopResultPacket.ValidationResultType.ERROR) {
                            if (Minecraft.getInstance().getConnection() != null) {
                                Minecraft.getInstance().getConnection().send(new ToggleBusinessStatusPacket(this.blockEntity.getBlockPos()));
                            }
                        }
                    })
                    .pos(this.leftPos + 125, this.topPos + this.imageHeight - 25)
                    .size(50, 20)
                    .build());
        }
    }

    private void saveAndClose() {
        // 保存前确保从编辑框获取最新值
        if (this.shopNameEditBox != null && this.currentTab == Tabs.GENERAL) {
            this.menu.getShopConfig().setShopName(this.shopNameEditBox.getValue());
        }
        // menuItems 现在由 ShopMenuInventoryWidget 管理，直接从 ShopConfig 中获取
        this.menu.getShopConfig().setMenuItems(this.shopMenuInventoryWidget.getMenuItems());
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
                this.shopLayoutWidget.render(guiGraphics, mouseX, mouseY, partialTicks);
                break;
            case MENU_INVENTORY:
                this.shopMenuInventoryWidget.render(guiGraphics, mouseX, mouseY, partialTicks);
                break;
            case SAVE:
                this.shopValidationWidget.render(guiGraphics, mouseX, mouseY, partialTicks);
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

        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.business_status"), this.leftPos + 10, this.topPos + 120, 0x404040, false);
        boolean isBusiness = config.isBusiness();
        Component statusComponent = isBusiness ? Component.translatable("gui.foodieshop.shop_config.business_status.open") : Component.translatable("gui.foodieshop.shop_config.business_status.closed");
        int statusColor = isBusiness ? 0x00FF00 : 0xFF0000; // Green for open, Red for closed
        guiGraphics.drawString(this.font, statusComponent, this.leftPos + 15, this.topPos + 130, statusColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.currentTab == Tabs.LAYOUT) {
            if (this.shopLayoutWidget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        } else if (this.currentTab == Tabs.MENU_INVENTORY) {
            if (this.shopMenuInventoryWidget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.currentTab == Tabs.LAYOUT) {
            if (this.shopLayoutWidget.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.currentTab == Tabs.LAYOUT) {
            if (this.shopLayoutWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.currentTab == Tabs.LAYOUT) {
            if (this.shopLayoutWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void updateStockContents(List<ItemStack> contents) {
        this.shopMenuInventoryWidget.updateStockContents(contents);
    }

    public void updateValidationResult(ValidateShopResultPacket.ValidationResultType resultType, List<Component> messages) {
        this.shopValidationWidget.updateValidationResult(resultType, messages);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    enum Tabs {
        GENERAL,
        LAYOUT,
        MENU_INVENTORY,
        SAVE
    }
}
