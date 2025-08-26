package mczme.foodieshop.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.locale.Language;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScrollableStringWidget extends AbstractWidget {
    private final Font font;
    private int color = 16777215; // 默认白色
    public ScrollableStringWidget(int x, int y, int width, int height, Component message, Font font) {
        super(x, y, width, height, message);
        this.font = font;
        this.active = false; // 默认不可交互
    }

    public ScrollableStringWidget setColor(int color) {
        this.color = color;
        return this;
    }

    public ScrollableStringWidget horizontalAlignment(float horizontalAlignment) {
        return this;
    }

    public ScrollableStringWidget alignLeft() {
        return this.horizontalAlignment(0.0F);
    }

    public ScrollableStringWidget alignCenter() {
        return this.horizontalAlignment(0.5F);
    }

    public ScrollableStringWidget alignRight() {
        return this.horizontalAlignment(1.0F);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Component component = this.getMessage();
        int i = this.getWidth();
        int j = this.font.width(component);
        int k = this.getX();
        int l = this.getY() + (this.getHeight() - 9) / 2;

        FormattedCharSequence formattedcharsequence = j > i ? this.clipText(component, i) : component.getVisualOrderText();
        guiGraphics.drawString(this.font, formattedcharsequence, k, l, this.color);
    }

    private FormattedCharSequence clipText(Component message, int width) {
        FormattedText formattedtext = this.font.substrByWidth(message, width - this.font.width(CommonComponents.ELLIPSIS));
        return Language.getInstance().getVisualOrder(FormattedText.composite(formattedtext, CommonComponents.ELLIPSIS));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // 实现旁白
    }
}
