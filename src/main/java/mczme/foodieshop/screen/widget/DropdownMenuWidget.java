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
        Button button = Button.builder(text, onPress).width(this.width - 10).build();
        LinearLayout paddedButton = new LinearLayout(this.width, button.getHeight(), LinearLayout.Orientation.HORIZONTAL);
        paddedButton.addChild(new Spacer(10, 0));
        paddedButton.addChild(button);
        paddedButton.arrangeElements();
        this.children.add(paddedButton);
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
        // 优先级1：检查主按钮是否被点击。这会切换展开/折叠状态。
        if (this.mainButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // 优先级2：如果菜单已展开，则检查是否有任何子项被点击。
        if (this.expanded) {
            // 遍历容器中的子控件以检查点击事件。
            for (LayoutElement child : this.children) {
                if (child instanceof GuiEventListener && ((GuiEventListener) child).mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
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
}
