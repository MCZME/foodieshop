package mczme.foodieshop.client.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

import java.util.Collection;
import java.util.Collections;

public class CustomConfigScreen extends ConfigurationScreen.ConfigurationSectionScreen {

    public CustomConfigScreen(Screen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
        super(parent, type, modConfig, title);
    }

    @Override
    protected Collection<? extends Element> createSyntheticValues() {
        return Collections.singletonList(
                new Element(
                        Component.translatable("foodieshop.config.trading_setting.title"),
                        Component.empty(), // 使用空的 Component 替代 null
                        Button.builder(
                                Component.translatable("foodieshop.config.trading_setting.button"),
                                button -> this.minecraft.setScreen(new TradingSettingScreen(this))
                        ).build()
                )
        );
    }
}
