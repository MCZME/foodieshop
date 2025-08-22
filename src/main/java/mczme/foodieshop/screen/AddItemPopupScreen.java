package mczme.foodieshop.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import mczme.foodieshop.api.trading.TradingManager; // 导入 TradingManager
import net.minecraft.world.item.ItemStack; // 导入 ItemStack
import net.minecraft.core.registries.Registries; // 导入 Registries
import net.minecraft.resources.ResourceLocation; // 导入 ResourceLocation
import net.minecraft.core.HolderLookup; // 导入 HolderLookup
import net.minecraft.resources.ResourceKey; // 导入 ResourceKey
import net.minecraft.world.item.Item; // 导入 Item

public class AddItemPopupScreen extends Screen {
    private final Screen parent;
    private String currentModId; // 新增成员变量

    private enum Mode { ITEM, MOD }
    private Mode currentMode = Mode.ITEM;

    private static final int POPUP_WIDTH = 250;
    private static final int POPUP_HEIGHT = 180;

    // 声明 EditBox 成员变量
    private EditBox itemNameBox;
    private EditBox itemValueBox;
    private EditBox modNameBox;

    public AddItemPopupScreen(Screen parent, String modId) { // 修改构造函数
        super(Component.translatable("foodieshop.config.trading_setting.popup.add_item_title"));
        this.parent = parent;
        this.currentModId = modId; // 初始化 currentModId
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets(); // 清除旧的部件，防止重建时重复添加

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
            itemNameBox = new EditBox(this.font, 0, 0, 100, 20, Component.translatable("foodieshop.config.trading_setting.popup.item_id"));
            if (currentModId != null) { // 如果是模组页面，自动填充模组ID
                itemNameBox.setValue(currentModId + ":");
            }
            itemValueBox = new EditBox(this.font, 0, 0, 60, 20, Component.translatable("foodieshop.config.trading_setting.popup.price"));
            GridLayout itemLayout = new GridLayout().columnSpacing(8);
            itemLayout.addChild(itemNameBox, 0, 0);
            itemLayout.addChild(itemValueBox, 0, 1);
            mainLayout.addChild(itemLayout, 1, 0);
        } else { // Mode.MOD
            modNameBox = new EditBox(this.font, 0, 0, 170, 20, Component.translatable("foodieshop.config.trading_setting.popup.mod_id"));
            mainLayout.addChild(modNameBox, 1, 0);
        }

        // --- 页脚按钮 ---
        Button saveButton = Button.builder(CommonComponents.GUI_DONE, button -> {
            if (this.minecraft != null && this.minecraft.level != null) {
                HolderLookup.Provider registries = this.minecraft.level.registryAccess();
                if (currentMode == Mode.ITEM) {
                    String itemId = itemNameBox.getValue();
                    String itemValueStr = itemValueBox.getValue();
                    try {
                        int value = Integer.parseInt(itemValueStr);
                        ResourceLocation itemRL = ResourceLocation.tryParse(itemId);
                        if (itemRL == null) {
                            System.err.println("Invalid item ID format: " + itemId);
                            return; // 或者显示错误消息给用户
                        }
                        // 将 ResourceLocation 转换为 ResourceKey<Item>
                        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, itemRL);
                        ItemStack itemStack = new ItemStack(registries.lookupOrThrow(Registries.ITEM).getOrThrow(itemKey));
                        
                        // 根据 currentModId 调用不同的 addSellableItem 方法
                        if (currentModId == null) { // 全局页面
                            TradingManager.addSellableItem(itemStack, value);
                        } else { // 模组设置页面
                            TradingManager.addSellableItem(currentModId, itemStack, value);
                        }
                    } catch (NumberFormatException e) {
                        // TODO: 显示错误消息给用户
                        System.err.println("Invalid number format for item value: " + itemValueStr);
                    } catch (Exception e) {
                        // TODO: 显示错误消息给用户
                        System.err.println("Error adding item: " + e.getMessage());
                    }
                } else { // Mode.MOD
                    String modId = modNameBox.getValue();
                    try {
                        TradingManager.addModFolder(modId);
                    } catch (Exception e) {
                        // TODO: 显示错误消息给用户
                        System.err.println("Error adding mod folder: " + e.getMessage());
                    }
                }
            }
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

        int top = (this.height - POPUP_HEIGHT) / 2;

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, top + 8, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.parent instanceof TradingSettingScreen) {
            ((TradingSettingScreen) this.parent).refreshScreen();
        }
        this.minecraft.setScreen(this.parent);
    }
}
