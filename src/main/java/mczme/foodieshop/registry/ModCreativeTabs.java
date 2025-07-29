package mczme.foodieshop.registry;

import java.util.function.Supplier;

import mczme.foodieshop.FoodieShop;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.Items;

import static mczme.foodieshop.registry.ModItems.ITEMS_LIST;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FoodieShop.MODID);

    public static final Supplier<CreativeModeTab> FOODIESSHOP_TAB = CREATIVE_MODE_TABS.register("foodieshop", () -> CreativeModeTab.builder()
    .title(Component.translatable("itemGroup." + FoodieShop.MODID + ".foodieshop"))
    .icon(() -> new ItemStack(Items.APPLE))
    .displayItems((params, output) -> {
        ITEMS_LIST.forEach(item -> output.accept(item.get()));
    }).build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
