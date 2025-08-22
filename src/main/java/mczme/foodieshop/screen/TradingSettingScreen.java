package mczme.foodieshop.screen;

import mczme.foodieshop.api.trading.TradingManager;
import mczme.foodieshop.network.packet.c2s.ReloadTradingDataPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import mczme.foodieshop.screen.widget.DropdownMenuWidget;
import mczme.foodieshop.screen.widget.ItemDisplayWidget;
import mczme.foodieshop.screen.widget.ScrollWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLPaths;
import mczme.foodieshop.config.ServerConfig;
import net.minecraft.client.gui.screens.ConfirmScreen;

import java.io.File;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TradingSettingScreen extends Screen {
    private static final int NAV_WIDTH = 100;
    private static final int TOP_MARGIN = 30;
    private static final int BOTTOM_MARGIN = 30;
    private static final int PADDING = 4;
    private static final int LINE_COLOR = 0xFF808080;
    private static final File CONFIG_DIR = new File(FMLPaths.CONFIGDIR.get().toFile(), "foodieshop/trading");
    private final Screen parent;

    private DropdownMenuWidget globalSettings;
    private DropdownMenuWidget modSettings;
    private ScrollWidget navScrollWidget;
    private ScrollWidget infoScrollWidget;
    private String currentConfigType = "general_sellable_items"; // 默认显示通用可出售物品
    public TradingSettingScreen(Screen parent) {
        super(Component.translatable("foodieshop.config.trading_setting.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        if (this.globalSettings == null) {
            this.createDropdowns();
        }

        int bottomLineY = this.height - BOTTOM_MARGIN - PADDING;
        int mainAreaHeight = bottomLineY - TOP_MARGIN;

        // 设置面板 (右侧)
        int settingsPanelX = NAV_WIDTH + PADDING;
        int settingsPanelWidth = this.width - settingsPanelX - PADDING;
        LinearLayout settingsPanel = new LinearLayout(settingsPanelX, TOP_MARGIN + PADDING, LinearLayout.Orientation.VERTICAL).spacing(4);

        // 工具区域
        LinearLayout toolArea = LinearLayout.horizontal().spacing(4);
        EditBox searchBox = new EditBox(this.font, 0, 0, settingsPanelWidth - 100, 20, Component.translatable("foodieshop.config.trading_setting.search_placeholder"));
        toolArea.addChild(searchBox);
        toolArea.addChild(Button.builder(Component.translatable("foodieshop.config.trading_setting.add_button"), button -> {
            String modId = null;
            if (this.currentConfigType.endsWith("_sellable_items") || this.currentConfigType.endsWith("_currency_items")) {
                if (!this.currentConfigType.startsWith("general_")) {
                    modId = this.currentConfigType.substring(0, this.currentConfigType.indexOf("_"));
                }
            }
            this.minecraft.setScreen(new AddItemPopupScreen(this, modId));
        }).width(40).build());
        toolArea.addChild(Button.builder(Component.translatable("foodieshop.config.trading_setting.settings_button"), button -> {}).width(40).build());
        toolArea.arrangeElements();
        settingsPanel.addChild(toolArea);

        // 信息区域
        this.infoScrollWidget = new ScrollWidget(0, 0, settingsPanelWidth, mainAreaHeight - toolArea.getHeight() - PADDING, Component.empty());
        settingsPanel.addChild(this.infoScrollWidget);

        settingsPanel.arrangeElements();
        settingsPanel.visitWidgets(this::addRenderableWidget);

        // 页脚按钮
        final int buttonWidth = 98;
        LinearLayout footerButtons = new LinearLayout(0, 0, LinearLayout.Orientation.HORIZONTAL).spacing(8);
        footerButtons.addChild(Button.builder(Component.translatable("foodieshop.config.open_folder"), button -> Util.getPlatform().openFile(CONFIG_DIR)).width(buttonWidth).build());
        footerButtons.addChild(Button.builder(Component.translatable("foodieshop.config.save"), button -> {
            if (!ServerConfig.CAN_MODIFY_TRADING_CONFIG.get()) {
                this.minecraft.setScreen(new ConfirmScreen(
                        (result) -> this.minecraft.setScreen(this),
                        Component.translatable("foodieshop.config.trading_setting.save_disabled.title"),
                        Component.translatable("foodieshop.config.trading_setting.save_disabled.message")
                ));
                return;
            }
            if (this.minecraft != null && this.minecraft.level != null) {
                TradingManager.save(this.minecraft.level.registryAccess());
            }
        }).width(buttonWidth).build());
        footerButtons.addChild(Button.builder(Component.translatable("foodieshop.config.reload"), button -> {
            if (this.minecraft != null && this.minecraft.level != null) {
                this.minecraft.getConnection().send(new ReloadTradingDataPacket());
                // TradingManager.reload(this.minecraft.level.registryAccess());
                this.createDropdowns(); // 重新创建下拉菜单以反映更改
                this.refreshNavLayout(); // 刷新导航布局
            }
        }).width(buttonWidth).build());
        footerButtons.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(buttonWidth).build());
        footerButtons.arrangeElements();
        footerButtons.setX((this.width - footerButtons.getWidth()) / 2);
        footerButtons.setY(bottomLineY + PADDING);
        footerButtons.visitWidgets(this::addRenderableWidget);

        // 导航面板 (左侧)
        this.navScrollWidget = this.addRenderableWidget(new ScrollWidget(0, TOP_MARGIN, NAV_WIDTH, mainAreaHeight, Component.empty()));
        this.refreshNavLayout();
        this.refreshInfoPanel(); // 首次加载屏幕时刷新信息面板
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, (TOP_MARGIN - this.font.lineHeight) / 2, 0xFFFFFF);

        int bottomLineY = this.height - BOTTOM_MARGIN - PADDING;
        pGuiGraphics.hLine(0, this.width, TOP_MARGIN, LINE_COLOR);
        pGuiGraphics.vLine(NAV_WIDTH, TOP_MARGIN, bottomLineY, LINE_COLOR);
        pGuiGraphics.hLine(0, this.width, bottomLineY, LINE_COLOR);

        // 在其他所有内容之上渲染下拉菜单的浮层
        if (this.navScrollWidget != null) {
            this.navScrollWidget.renderOverlay(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }
        if (this.infoScrollWidget != null) { // 渲染信息区域的浮层
            this.infoScrollWidget.renderOverlay(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }
    }

    private void createDropdowns() {
        this.globalSettings = new DropdownMenuWidget(Component.translatable("foodieshop.config.trading_setting.button.global_settings"), 90);
        this.globalSettings.addOption(Component.translatable("foodieshop.config.trading_setting.button.sellable_items"), b -> {
            this.currentConfigType = "general_sellable_items";
            this.refreshInfoPanel();
        });
        this.globalSettings.addOption(Component.translatable("foodieshop.config.trading_setting.button.currency_items"), b -> {
            this.currentConfigType = "general_currency_items";
            this.refreshInfoPanel();
        });
        this.globalSettings.addOption(Component.translatable("foodieshop.config.trading_setting.button.fixed_trades"), b -> {
            this.currentConfigType = "fixed_trades";
            this.refreshInfoPanel();
        });
        this.globalSettings.setOnExpandCollapse(this::refreshNavLayout);

        this.modSettings = new DropdownMenuWidget(Component.translatable("foodieshop.config.trading_setting.button.mod_settings"), 90);
        List<String> modFolders = TradingManager.getModFolders();
        for (String modId : modFolders) {
            DropdownMenuWidget modSpecificMenu = this.modSettings.addNestedMenu(Component.literal(modId), 80);
            modSpecificMenu.addOption(Component.translatable("foodieshop.config.trading_setting.button.sellable_items"), b -> {
                this.currentConfigType = modId + "_sellable_items";
                this.refreshInfoPanel();
            });
            modSpecificMenu.addOption(Component.translatable("foodieshop.config.trading_setting.button.currency_items"), b -> {
                this.currentConfigType = modId + "_currency_items";
                this.refreshInfoPanel();
            });
            modSpecificMenu.setOnExpandCollapse(this::refreshNavLayout);
        }
        this.modSettings.setOnExpandCollapse(this::refreshNavLayout);
    }

    private void refreshNavLayout() {
        // 排列下拉菜单中的元素以计算其大小
        this.globalSettings.arrangeElements();
        this.modSettings.arrangeElements();

        LinearLayout navPanel = LinearLayout.vertical().spacing(PADDING);
        navPanel.addChild(this.globalSettings);
        navPanel.addChild(this.modSettings);
        navPanel.arrangeElements();

        // 在将导航面板设置为内容之前，先为其应用内边距
        LinearLayout paddedNavPanel = new LinearLayout(0, 0, LinearLayout.Orientation.VERTICAL);
        paddedNavPanel.defaultCellSetting().padding(PADDING);
        paddedNavPanel.addChild(navPanel);
        paddedNavPanel.arrangeElements();

        this.navScrollWidget.setContents(paddedNavPanel);
        // 重建内容后，将滚动量限制在新的最大值内。
        this.navScrollWidget.setScrollAmount(this.navScrollWidget.scrollAmount());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void refreshInfoPanel() {
        if (this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        LinearLayout infoContent = LinearLayout.vertical().spacing(2);
        int settingsPanelWidth = this.width - NAV_WIDTH - PADDING * 2; // 重新计算宽度

        switch (this.currentConfigType) {
            case "general_sellable_items":
                TradingManager.getSellableItems("general").forEach((itemStack, value) -> {
                    infoContent.addChild(new ItemDisplayWidget(0, 0, settingsPanelWidth, 40, itemStack, value));
                });
                break;
            case "general_currency_items":
                TradingManager.getCurrencyItems("general").forEach((itemStack, value) -> {
                    infoContent.addChild(new ItemDisplayWidget(0, 0, settingsPanelWidth, 40, itemStack, value));
                });
                break;
            case "fixed_trades":
                TradingManager.getFixedTrades().forEach(fixedTrade -> {
                    infoContent.addChild(new StringWidget(Component.literal(fixedTrade.toString()), this.font)); // 暂时显示字符串
                });
                break;
            default:
                // 处理模组特定的可出售物品和货币物品
                if (this.currentConfigType.endsWith("_sellable_items")) {
                    String modId = this.currentConfigType.replace("_sellable_items", "");
                    TradingManager.getSellableItems(modId).forEach((itemStack, value) -> {
                        infoContent.addChild(new ItemDisplayWidget(0, 0, settingsPanelWidth, 40, itemStack, value));
                    });
                } else if (this.currentConfigType.endsWith("_currency_items")) {
                    String modId = this.currentConfigType.replace("_currency_items", "");
                    TradingManager.getCurrencyItems(modId).forEach((itemStack, value) -> {
                        infoContent.addChild(new ItemDisplayWidget(0, 0, settingsPanelWidth, 40, itemStack, value));
                    });
                }
                break;
        }

        infoContent.arrangeElements();
        this.infoScrollWidget.setContents(infoContent);
        this.infoScrollWidget.setScrollAmount(this.infoScrollWidget.scrollAmount()); // 重建内容后，将滚动量限制在新的最大值内。
    }
}
