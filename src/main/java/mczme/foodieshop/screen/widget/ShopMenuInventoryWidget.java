package mczme.foodieshop.screen.widget;

import mczme.foodieshop.screen.ShopConfigMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ShopMenuInventoryWidget {

    private final Font font;
    private final int screenLeftPos;
    private final int screenTopPos;
    private List<ItemStack> stockContents;
    private Set<Item> menuItems;

    public ShopMenuInventoryWidget(ShopConfigMenu menu, Font font, int screenLeftPos, int screenTopPos, int imageWidth, int imageHeight, List<ItemStack> stockContents, Set<Item> menuItems) {
        this.font = font;
        this.screenLeftPos = screenLeftPos;
        this.screenTopPos = screenTopPos;
        this.stockContents = stockContents;
        this.menuItems = menuItems;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 渲染 库存
        guiGraphics.drawString(this.font, Component.translatable("gui.foodieshop.shop_config.stock"), this.screenLeftPos + 10, this.screenTopPos + 35, 0x404040, false);
        int stockX = this.screenLeftPos + 10;
        int stockY = this.screenTopPos + 45;
        for (int i = 0; i < this.stockContents.size(); i++) {
            ItemStack stack = this.stockContents.get(i);
            int x = stockX + (i % 9) * 18;
            int y = stockY + (i / 9) * 18;
            guiGraphics.renderItem(stack, x, y);
            guiGraphics.renderItemDecorations(this.font, stack, x, y);

            if (this.menuItems.contains(stack.getItem())) {
                guiGraphics.fill(x, y, x + 16, y + 16, 0x8000FF00); //选择物品绿色覆盖
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int stockX = this.screenLeftPos + 10;
        int stockY = this.screenTopPos + 45;
        for (int i = 0; i < this.stockContents.size(); i++) {
            int x = stockX + (i % 9) * 18;
            int y = stockY + (i / 9) * 18;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                Item item = this.stockContents.get(i).getItem();
                if (this.menuItems.contains(item)) {
                    this.menuItems.remove(item);
                } else {
                    this.menuItems.add(item);
                }
                return true;
            }
        }
        return false;
    }

    public void updateStockContents(List<ItemStack> contents) {
        this.stockContents = contents;
    }

    public Set<Item> getMenuItems() {
        return this.menuItems;
    }
}
