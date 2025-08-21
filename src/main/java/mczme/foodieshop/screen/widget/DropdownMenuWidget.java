package mczme.foodieshop.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DropdownMenuWidget extends AbstractWidget implements GuiEventListener, Renderable {
    private final Button mainButton;
    private final int width;
    private final List<LayoutElement> children = new ArrayList<>();
    private final List<DropdownMenuWidget> nestedMenus = new ArrayList<>();
    private boolean expanded = false;
    private Runnable expandCollapseListener;
    private LinearLayout optionsContainer;
    private GuiEventListener clickedOption;

    public DropdownMenuWidget(Component text, int width) {
        super(0, 0, width, 20, text); // 默认高度为20
        this.width = width;
        this.mainButton = Button.builder(text, btn -> this.toggleExpanded()).width(this.width).build();
        this.optionsContainer = LinearLayout.vertical().spacing(2);
    }

    public void setOnExpandCollapse(Runnable listener) {
        this.expandCollapseListener = listener;
    }

    /**
     * 添加一个嵌套的子菜单。
     */
    public DropdownMenuWidget addNestedMenu(Component text, int width) {
        DropdownMenuWidget nestedMenu = new DropdownMenuWidget(text, width);
        this.children.add(nestedMenu);
        this.nestedMenus.add(nestedMenu);
        return nestedMenu;
    }

    /**
     * 添加一个普通的选项按钮。
     */
    public void addOption(Component text, Button.OnPress onPress) {
        this.children.add(new PaddedButton(this.width, 20, text, onPress));
    }

    private void toggleExpanded() {
        this.expanded = !this.expanded;
        if (this.expandCollapseListener != null) {
            this.expandCollapseListener.run();
        }
    }

    /**
     * 递归地排列所有元素，并计算控件的总高度。
     * 这是确保嵌套菜单正确更新其内容的关键。
     */
    public void arrangeElements() {
        // 递归地为所有子菜单排列元素
        for (DropdownMenuWidget nestedMenu : this.nestedMenus) {
            nestedMenu.arrangeElements();
        }

        this.optionsContainer = LinearLayout.vertical().spacing(2);
        if (this.expanded) {
            for (LayoutElement child : this.children) {
                this.optionsContainer.addChild(child);
            }
        }
        this.optionsContainer.arrangeElements();

        // 根据主按钮和展开内容计算总高度
        this.height = this.mainButton.getHeight();
        if (this.expanded) {
            this.height += this.optionsContainer.getHeight() + 2; // 为内边距增加一些空间
        }
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.mainButton.setX(x);
        this.optionsContainer.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.mainButton.setY(y);
        this.optionsContainer.setY(y + this.mainButton.getHeight() + 2);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // renderWidget只渲染主按钮，展开的内容由renderOverlay处理。
        this.mainButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * 渲染浮层内容（即展开的菜单项）。
     * 这个方法由父容器（如ScrollWidget）在所有常规UI渲染完毕后调用。
     */
    public void renderOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.expanded) {
            this.optionsContainer.visitWidgets(widget -> {
                // 首先，渲染子控件本身（例如，按钮或嵌套下拉菜单的主按钮）。
                if (widget instanceof Renderable) {
                    ((Renderable) widget).render(guiGraphics, mouseX, mouseY, partialTick);
                }
                // 然后，如果子控件是一个嵌套的下拉菜单，则递归调用其renderOverlay方法。
                if (widget instanceof DropdownMenuWidget nestedMenu) {
                    nestedMenu.renderOverlay(guiGraphics, mouseX, mouseY, partialTick);
                }
            });
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.mainButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (this.expanded) {
            for (LayoutElement child : this.children) {
                if (child instanceof GuiEventListener listener && listener.mouseClicked(mouseX, mouseY, button)) {
                    this.clickedOption = listener;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.clickedOption != null) {
            boolean result = this.clickedOption.mouseReleased(mouseX, mouseY, button);
            this.clickedOption = null;
            return result;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        this.mainButton.updateNarration(pNarrationElementOutput);
    }

    @OnlyIn(Dist.CLIENT)
    static class Spacer extends AbstractWidget {
        public Spacer(int width, int height) {
            super(0, 0, width, height, Component.empty());
        }

        @Override
        public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {}

        @Override
        protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
    }

    @OnlyIn(Dist.CLIENT)
    static class PaddedButton extends AbstractWidget implements GuiEventListener, Renderable {
        private final Button button;

        public PaddedButton(int width, int height, Component text, Button.OnPress onPress) {
            super(0, 0, width, height, text);
            this.button = Button.builder(text, onPress).width(width - 10).build();
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            this.button.setX(x + 10);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            this.button.setY(y);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.button.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.button.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.button.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
            this.button.updateNarration(pNarrationElementOutput);
        }
    }
}
