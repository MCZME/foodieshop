package mczme.foodieshop.client;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.screen.CustomConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

@Mod(value = FoodieShop.MODID, dist = Dist.CLIENT)
public class FoodieShopClient {
    public FoodieShopClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> new ConfigurationScreen(
                container,
                parent,
                CustomConfigScreen::new
        ));
    }
}
