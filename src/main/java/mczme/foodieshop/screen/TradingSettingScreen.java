package mczme.foodieshop.screen;

import mczme.foodieshop.api.trading.TradingManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import mczme.foodieshop.screen.widget.DropdownMenuWidget;
import mczme.foodieshop.screen.widget.ScrollWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLPaths;

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
        LinearLayout settingsPanel = new LinearLayout(settingsPanelX, TOP_MARGIN + PADDING, LinearLayout.Orientation.VERTICAL).spacing(8);
        StringWidget settingsString = new StringWidget(Component.literal("Settings go here"), this.font);
        settingsPanel.addChild(settingsString);
        settingsString.setWidth(settingsPanelWidth);
        settingsPanel.arrangeElements();
        settingsPanel.visitWidgets(this::addRenderableWidget);

        // 页脚按钮
        final int buttonWidth = 98;
        LinearLayout footerButtons = new LinearLayout(0, 0, LinearLayout.Orientation.HORIZONTAL).spacing(8);
        footerButtons.addChild(Button.builder(Component.translatable("foodieshop.config.reload_trading"), button -> TradingManager.reload()).width(buttonWidth).build());
        footerButtons.addChild(Button.builder(Component.translatable("foodieshop.config.open_folder"), button -> Util.getPlatform().openFile(CONFIG_DIR)).width(buttonWidth).build());
        footerButtons.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(buttonWidth).build());
        footerButtons.arrangeElements();
        footerButtons.setX((this.width - footerButtons.getWidth()) / 2);
        footerButtons.setY(bottomLineY + PADDING);
        footerButtons.visitWidgets(this::addRenderableWidget);

        // 导航面板 (左侧)
        this.navScrollWidget = this.addRenderableWidget(new ScrollWidget(0, TOP_MARGIN, NAV_WIDTH, mainAreaHeight, Component.empty()));
        this.refreshNavLayout();
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
    }

    private void createDropdowns() {
        this.globalSettings = new DropdownMenuWidget(Component.translatable("foodieshop.config.trading_setting.button.global_settings"), 90);
        this.globalSettings.addOption(Component.translatable("foodieshop.config.trading_setting.button.tiers"), b -> {});
        this.globalSettings.addOption(Component.translatable("foodieshop.config.trading_setting.button.food_value_tiers"), b -> {});
        this.globalSettings.addOption(Component.translatable("foodieshop.config.trading_setting.button.reward_items"), b -> {});
        this.globalSettings.addOption(Component.translatable("foodieshop.config.trading_setting.button.fixed_trades"), b -> {});
        this.globalSettings.setOnExpandCollapse(this::refreshNavLayout);

        this.modSettings = new DropdownMenuWidget(Component.translatable("foodieshop.config.trading_setting.button.mod_settings"), 90);
        List<String> modFolders = TradingManager.getModFolders();
        for (String modId : modFolders) {
            DropdownMenuWidget modSpecificMenu = this.modSettings.addNestedMenu(Component.literal(modId), 80);
            modSpecificMenu.addOption(Component.translatable("foodieshop.config.trading_setting.button.food_value_tiers"), b -> {});
            modSpecificMenu.addOption(Component.translatable("foodieshop.config.trading_setting.button.reward_items"), b -> {});
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
}
