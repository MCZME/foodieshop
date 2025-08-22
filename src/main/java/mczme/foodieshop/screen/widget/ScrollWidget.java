package mczme.foodieshop.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ScrollWidget extends AbstractWidget implements Renderable, GuiEventListener {

    private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("widget/text_field"), ResourceLocation.withDefaultNamespace("widget/text_field_highlighted")
    );
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
    private static final int INNER_PADDING = 4;
    private static final int SCROLL_BAR_WIDTH = 8;

    private LinearLayout contents;
    private GuiEventListener clickedWidget;

    private double scrollAmount;
    private boolean scrolling;

    public ScrollWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.contents = new LinearLayout(width, 0, LinearLayout.Orientation.VERTICAL);
    }

    public void setContents(LinearLayout contents) {
        this.contents = contents;
    }

    // --- 子组件管理 ---

    private List<GuiEventListener> children() {
        List<GuiEventListener> children = new ArrayList<>();
        this.contents.visitWidgets(widget -> {
            if (widget instanceof GuiEventListener) {
                children.add((GuiEventListener) widget);
            }
        });
        return children;
    }

    // --- 滚动逻辑 ---

    public void setScrollAmount(double scrollAmount) {
        this.scrollAmount = Mth.clamp(scrollAmount, 0.0, (double)this.getMaxScrollAmount());
    }

    public double scrollAmount() {
        return this.scrollAmount;
    }

    protected int getMaxScrollAmount() {
        return Math.max(0, this.getInnerHeight() - (this.height - 4));
    }

    private int getInnerHeight() {
        return this.contents.getHeight();
    }

    private boolean scrollbarVisible() {
        return this.getInnerHeight() > this.getHeight();
    }

    private double scrollRate() {
        return 9.0; // 一个合理的滚动速率
    }

    // --- 事件处理 ---

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible) return false;


        // 优先级2：处理滚动条点击。
        boolean isClickOnScrollBar = this.scrollbarVisible()
            && mouseX >= (double)(this.getX() + this.width)
            && mouseX <= (double)(this.getX() + this.width + SCROLL_BAR_WIDTH)
            && mouseY >= (double)this.getY()
            && mouseY < (double)(this.getY() + this.height);

        if (isClickOnScrollBar && button == 0) {
            this.scrolling = true;
            return true;
        }

        // 优先级3：处理内容区域内的点击。
        if (this.withinContentAreaPoint(mouseX, mouseY)) {
            double adjustedY = mouseY + this.scrollAmount();
            for (GuiEventListener child : this.children()) {
                if (child.mouseClicked(mouseX, adjustedY, button)) {
                    this.clickedWidget = child;
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.scrolling = false;
        }
        if (this.clickedWidget != null) {
            double adjustedY = mouseY + this.scrollAmount();
            boolean result = this.clickedWidget.mouseReleased(mouseX, adjustedY, button);
            this.clickedWidget = null;
            return result;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling) {
            int scrollBarHeight = Mth.clamp((int)((float)(this.height * this.height) / (float)this.getInnerHeight()), 32, this.height);
            double scrollRatio = (double)Math.max(1, this.getMaxScrollAmount() / (this.height - scrollBarHeight));
            this.setScrollAmount(this.scrollAmount + dragY * scrollRatio);
            return true;
        }
        if (this.clickedWidget != null) {
            double adjustedY = mouseY + this.scrollAmount();
            return this.clickedWidget.mouseDragged(mouseX, adjustedY, button, dragX, dragY);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.visible) return false;
        this.setScrollAmount(this.scrollAmount - scrollY * this.scrollRate());
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean up = keyCode == 265;
        boolean down = keyCode == 264;
        if (up || down) {
            this.setScrollAmount(this.scrollAmount + (double)(up ? -1 : 1) * this.scrollRate());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // --- 渲染 ---

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        guiGraphics.blitSprite(BACKGROUND_SPRITES.get(this.isActive(), this.isFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
        guiGraphics.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0, -this.scrollAmount, 0.0);
        this.renderContents(guiGraphics, mouseX, (int)(mouseY + this.scrollAmount), partialTick);
        guiGraphics.pose().popPose();

        guiGraphics.disableScissor();

        if (this.scrollbarVisible()) {
            this.renderScrollBar(guiGraphics);
        }
    }

    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.contents.setX(this.getX() + INNER_PADDING);
        this.contents.setY(this.getY() + INNER_PADDING);
        this.contents.visitWidgets(widget -> {
            if (widget instanceof Renderable) {
                ((Renderable)widget).render(guiGraphics, mouseX, mouseY, partialTick);
            }
        });
    }

    public void renderOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0, -this.scrollAmount, 0.0);
        this.contents.visitWidgets(widget -> {
            if (widget instanceof DropdownMenuWidget dropdown) {
                dropdown.renderOverlay(guiGraphics, mouseX, (int)(mouseY + this.scrollAmount), partialTick);
            }
        });
        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
    }

    private void renderScrollBar(GuiGraphics guiGraphics) {
        int scrollBarHeight = Mth.clamp((int)((float)(this.height * this.height) / (float)this.getInnerHeight()), 32, this.height);
        int scrollBarX = this.getX() + this.width;
        int scrollBarY = Math.max(this.getY(), (int)this.scrollAmount * (this.height - scrollBarHeight) / this.getMaxScrollAmount() + this.getY());
        
        RenderSystem.enableBlend();
        guiGraphics.blitSprite(SCROLLER_SPRITE, scrollBarX, scrollBarY, SCROLL_BAR_WIDTH, scrollBarHeight);
        RenderSystem.disableBlend();
    }

    private boolean withinContentAreaPoint(double x, double y) {
        return x >= (double)this.getX()
            && x < (double)(this.getX() + this.width)
            && y >= (double)this.getY()
            && y < (double)(this.getY() + this.height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        //  实现旁白
    }
}
