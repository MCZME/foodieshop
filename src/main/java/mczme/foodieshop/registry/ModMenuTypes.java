package mczme.foodieshop.registry;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.screen.ShopConfigMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, FoodieShop.MODID);

    public static final Supplier<MenuType<ShopConfigMenu>> SHOP_CONFIG_MENU = MENUS.register("shop_config_menu",
            () -> IMenuTypeExtension.create((windowId, playerInv, extraData) -> new ShopConfigMenu(windowId, playerInv, extraData)));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
