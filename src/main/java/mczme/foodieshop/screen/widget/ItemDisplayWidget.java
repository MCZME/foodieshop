package mczme.foodieshop.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemDisplayWidget extends AbstractWidget {
    private final ItemStack itemStack;
    private final int value;

    public ItemDisplayWidget(int x, int y, int width, int height, ItemStack itemStack, int value) {
        super(x, y, width, height, Component.empty());
        this.itemStack = itemStack;
        this.value = value;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        // 绘制物品图标
        guiGraphics.renderItem(this.itemStack, this.getX() + 2, this.getY() + 2);

        // 绘制物品名称
        Component itemName = this.itemStack.getHoverName();
        guiGraphics.drawString(font, itemName, this.getX() + 20, this.getY() + 6, 0xFFFFFF, false);

        // 绘制物品价值
        Component valueText = Component.literal("价值: " + this.value);
        guiGraphics.drawString(font, valueText, this.getX() + 20, this.getY() + 6 + font.lineHeight, 0xFFFFFF, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.itemStack.getHoverName());
        narrationElementOutput.add(NarratedElementType.HINT, Component.literal("价值: " + this.value));
    }
}
