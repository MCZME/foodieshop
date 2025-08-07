package mczme.foodieshop.client.screen;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.api.trading.TradingManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import net.neoforged.fml.loading.FMLPaths;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;

import java.io.File;

public class TradingSettingScreen extends OptionsSubScreen {
    private static final File CONFIG_DIR = new File(FMLPaths.CONFIGDIR.get().toFile(), FoodieShop.MODID + "/trading");

    public TradingSettingScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable("foodieshop.config.trading_setting.title"));
    }

    @Override
    protected void addOptions() {
        // 添加“重新加载交易配置”按钮
        this.list.addSmall(
                Button.builder(Component.translatable("foodieshop.config.reload_trading"), button -> {
                    TradingManager.reload();
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.displayClientMessage(Component.translatable("foodieshop.config.reloaded"), false);
                    }
                }).build(),
                Button.builder(Component.translatable("foodieshop.config.open_folder"), button -> {
                    Util.getPlatform().openFile(CONFIG_DIR);
                }).build()
        );
    }
}
