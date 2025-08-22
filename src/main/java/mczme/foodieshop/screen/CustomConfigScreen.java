package mczme.foodieshop.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

import java.util.Collection;
import java.util.Collections;

@OnlyIn(Dist.CLIENT)
public class CustomConfigScreen extends ConfigurationScreen.ConfigurationSectionScreen {

    private final ModConfig.Type configType;

    public CustomConfigScreen(Screen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
        super(parent, type, modConfig, title);
        this.configType = type;
    }

    @Override
    protected Collection<? extends Element> createSyntheticValues() {
        if (this.configType == ModConfig.Type.COMMON) {
            return Collections.singletonList(
                    new Element(
                            Component.translatable("foodieshop.config.trading_setting.title"),
                            Component.empty(),
                            Button.builder(
                                    Component.translatable("foodieshop.config.trading_setting.button"),
                                    button -> {
                                        if (this.minecraft != null) {
                                            if (this.minecraft.level != null) {
                                                this.minecraft.setScreen(new TradingSettingScreen(this));
                                            } else {
                                                this.minecraft.setScreen(new ConfirmScreen(
                                                        confirmed -> this.minecraft.setScreen(this),
                                                        Component.translatable("foodieshop.config.trading_setting.no_world.title"),
                                                        Component.translatable("foodieshop.config.trading_setting.no_world.message")
                                                ));
                                            }
                                        }
                                    }
                            ).build()
                    )
            );
        }
        return Collections.emptyList();
    }
}
