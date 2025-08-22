package mczme.foodieshop.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AddItemPopupScreen extends Screen {
    private final Screen parent;

    private enum Mode { ITEM, MOD }
    private Mode currentMode = Mode.ITEM;

    private static final int POPUP_WIDTH = 250;
    private static final int POPUP_HEIGHT = 180;

    public AddItemPopupScreen(Screen parent) {
        super(Component.translatable("foodieshop.config.trading_setting.popup.add_item_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int left = (this.width - POPUP_WIDTH) / 2;
        int top = (this.height - POPUP_HEIGHT) / 2;

        // --- 主布局 ---
        GridLayout mainLayout = new GridLayout().rowSpacing(8);
        mainLayout.defaultCellSetting().alignHorizontallyCenter();

        // --- 模式切换按钮 ---
        Button itemModeButton = Button.builder(Component.translatable("foodieshop.config.trading_setting.popup.item_mode"), button -> {
            this.currentMode = Mode.ITEM;
            this.rebuildWidgets(); // 重建界面
        }).build();
        Button modModeButton = Button.builder(Component.translatable("foodieshop.config.trading_setting.popup.mod_mode"), button -> {
            this.currentMode = Mode.MOD;
            this.rebuildWidgets(); // 重建界面
        }).build();
        
        // 根据当前模式禁用对应的按钮
        if (currentMode == Mode.ITEM) {
            itemModeButton.active = false;
        } else {
            modModeButton.active = false;
        }

        GridLayout modeButtonsLayout = new GridLayout().columnSpacing(8);
        modeButtonsLayout.addChild(itemModeButton, 0, 0);
        modeButtonsLayout.addChild(modModeButton, 0, 1);
        mainLayout.addChild(modeButtonsLayout, 0, 0);

        // --- 动态设置区域 ---
        if (currentMode == Mode.ITEM) {
            EditBox itemNameBox = new EditBox(this.font, 0, 0, 100, 20, Component.translatable("foodieshop.config.trading_setting.popup.item_id"));
            EditBox itemValueBox = new EditBox(this.font, 0, 0, 60, 20, Component.translatable("foodieshop.config.trading_setting.popup.price"));
            GridLayout itemLayout = new GridLayout().columnSpacing(8);
            itemLayout.addChild(itemNameBox, 0, 0);
            itemLayout.addChild(itemValueBox, 0, 1);
            mainLayout.addChild(itemLayout, 1, 0);
        } else { // Mode.MOD
            EditBox modNameBox = new EditBox(this.font, 0, 0, 170, 20, Component.translatable("foodieshop.config.trading_setting.popup.mod_id"));
            mainLayout.addChild(modNameBox, 1, 0);
        }

        // --- 页脚按钮 ---
        Button saveButton = Button.builder(CommonComponents.GUI_DONE, button -> {
            // TODO: 实现保存逻辑
            this.onClose();
        }).build();
        Button cancelButton = Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build();

        GridLayout footerLayout = new GridLayout().columnSpacing(8);
        footerLayout.addChild(saveButton, 0, 0);
        footerLayout.addChild(cancelButton, 0, 1);
        mainLayout.addChild(footerLayout, 2, 0);

        mainLayout.arrangeElements();
        mainLayout.setX(left + (POPUP_WIDTH - mainLayout.getWidth()) / 2);
        mainLayout.setY(top + 20);
        mainLayout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.parent.render(pGuiGraphics, -1, -1, pPartialTick);
        pGuiGraphics.fill(0, 0, this.width, this.height, 0x80000000);

        int left = (this.width - POPUP_WIDTH) / 2;
        int top = (this.height - POPUP_HEIGHT) / 2;
        pGuiGraphics.fill(left, top, left + POPUP_WIDTH, top + POPUP_HEIGHT, 0xFFC0C0C0);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, top + 8, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
