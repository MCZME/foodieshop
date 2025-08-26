package mczme.foodieshop.screen.widget;

import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import mczme.foodieshop.network.packet.c2s.ValidateShopPacket;
import mczme.foodieshop.network.packet.s2c.ValidateShopResultPacket;
import mczme.foodieshop.screen.ShopConfigMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ShopValidationWidget {

    private final ShopConfigMenu menu;
    private final CashierDeskBlockEntity blockEntity;
    private final Font font;
    private final int screenLeftPos;
    private final int screenTopPos;
    private final int imageWidth;
    private final int imageHeight;
    private final Consumer<Button> addRenderableWidget; // 用于添加验证按钮

    private ValidateShopResultPacket.ValidationResultType lastValidationResultType = null;
    private List<Component> validationMessages = new ArrayList<>();

    public ShopValidationWidget(ShopConfigMenu menu, CashierDeskBlockEntity blockEntity, Font font, int screenLeftPos, int screenTopPos, int imageWidth, int imageHeight, Consumer<Button> addRenderableWidget) {
        this.menu = menu;
        this.blockEntity = blockEntity;
        this.font = font;
        this.screenLeftPos = screenLeftPos;
        this.screenTopPos = screenTopPos;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.addRenderableWidget = addRenderableWidget;
        initWidgets();
    }

    private void initWidgets() {
        // 验证按钮将由 ShopConfigScreen 管理，因为它是一个通用的底部按钮
        // 或者，如果验证按钮是此组件特有的，则可以在这里添加
        // 为了保持 ShopConfigScreen 的底部按钮逻辑一致性，我将让 ShopConfigScreen 继续管理验证按钮。
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.validation_results"), this.screenLeftPos + 10, this.screenTopPos + 65, 0x404040, false);

        if (this.lastValidationResultType != null) {
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
            guiGraphics.drawString(this.font, titleComponent, this.screenLeftPos + 15, this.screenTopPos + 78, titleColor, false);

            int messageY = this.screenTopPos + 95;
            for (Component message : this.validationMessages) {
                guiGraphics.drawString(this.font, message, this.screenLeftPos + 15, messageY, 0x7F7F7F, false);
                messageY += 10; // 每条消息向下偏移
            }
        } else {
            guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.no_validation_run"), this.screenLeftPos + 15, this.screenTopPos + 78, 0x7F7F7F, false);
        }
    }

    public void updateValidationResult(ValidateShopResultPacket.ValidationResultType resultType, List<Component> messages) {
        this.lastValidationResultType = resultType;
        this.validationMessages = messages;
    }
}
