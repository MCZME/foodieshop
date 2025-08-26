package mczme.foodieshop.screen.widget;

import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import mczme.foodieshop.network.packet.s2c.ValidateShopResultPacket;
import mczme.foodieshop.screen.ShopConfigMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ShopValidationWidget {

    private final Font font;
    private final int screenLeftPos;
    private final int screenTopPos;
    private final ScrollWidget scrollWidget;

    private ValidateShopResultPacket.ValidationResultType lastValidationResultType = null;

    public ShopValidationWidget(ShopConfigMenu menu, CashierDeskBlockEntity blockEntity, Font font, int screenLeftPos, int screenTopPos, int imageWidth, int imageHeight, Consumer<Button> addRenderableWidget) {
        this.font = font;
        this.screenLeftPos = screenLeftPos;
        this.screenTopPos = screenTopPos;
        int scrollWidgetX = screenLeftPos + 10;
        int scrollWidgetY = screenTopPos + 50;
        int scrollWidgetWidth = imageWidth - 20 - ScrollWidget.SCROLL_BAR_WIDTH; // 减去左右边距和滚动条宽度
        int scrollWidgetHeight = imageHeight - 58 - 20; // 从 58 开始，底部留 20 像素

        this.scrollWidget = new ScrollWidget(scrollWidgetX, scrollWidgetY, scrollWidgetWidth, scrollWidgetHeight, Component.empty());
        initWidgets();
    }

    private void initWidgets() {
        // 验证按钮将由 ShopConfigScreen 管理，因为它是一个通用的底部按钮
        // 或者，如果验证按钮是此组件特有的，则可以在这里添加
        // 为了保持 ShopConfigScreen 的底部按钮逻辑一致性，我将让 ShopConfigScreen 继续管理验证按钮。
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.validation_results"), this.screenLeftPos + 10, this.screenTopPos + 40, 0x404040, false);

        this.scrollWidget.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public void updateValidationResult(ValidateShopResultPacket.ValidationResultType resultType, List<Component> messages) {
        this.lastValidationResultType = resultType;

        int width = this.scrollWidget.getWidth() - ScrollWidget.INNER_PADDING * 2;
        
        LinearLayout newContents = new LinearLayout(width, 0, LinearLayout.Orientation.VERTICAL);
        
        Component titleComponent;
        int titleColor;
        switch (this.lastValidationResultType) {
            case SUCCESS:
                titleComponent = Component.translatable("gui.foodieshop.shop_config.validation_success");
                titleColor = 0x00FF00; // 绿色
                break;
            case WARNING:
                titleComponent = Component.translatable("gui.foodieshop.shop_config.validation_warning");
                titleColor = 0xFFA500; // 橙色
                break;
            case ERROR:
                titleComponent = Component.translatable("gui.foodieshop.shop_config.validation_failure");
                titleColor = 0xFF0000; // 红色
                break;
            default:
                titleComponent = Component.translatable("gui.foodieshop.shop_config.validation_unknown");
                titleColor = 0x7F7F7F; // 灰色
                break;
        }
        newContents.addChild(new ScrollableStringWidget(0, 0, width, 9, titleComponent, this.font).setColor(titleColor));

        if (messages.isEmpty()) {
            newContents.addChild(new ScrollableStringWidget(0, 0, width, 9, Component.translatable("gui.foodieshop.shop_config.no_validation_run"), this.font).setColor(0x7F7F7F));
        } else {
            for (Component message : messages) {
                newContents.addChild(new ScrollableStringWidget(0, 0, width, 9, message, this.font).setColor(0xffffff));
            }
        }
        this.scrollWidget.setContents(newContents);
        this.scrollWidget.setScrollAmount(0); // 重置滚动位置
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.scrollWidget.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.scrollWidget.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return this.scrollWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.scrollWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
