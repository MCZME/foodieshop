package mczme.foodieshop.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.item.DinerBlueprintPenItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, FoodieShop.MODID);

    public static final List<Supplier<Item>> ITEMS_LIST = new ArrayList<>();

    public static final Supplier<Item> CASHIER_DESK_ITEM = registerItem("cashier_desk",
            () -> new BlockItem(ModBlocks.CASHIER_DESK_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Item> DINER_BLUEPRINT_PEN = registerItem("diner_blueprint_pen",
            () -> new DinerBlueprintPenItem(new Item.Properties().stacksTo(1)));

    public static Supplier<Item> registerItem(String name, Supplier<Item> itemSupplier) {
        Supplier<Item> item = ITEMS.register(name, itemSupplier);
        ITEMS_LIST.add(item);
        return item;
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
