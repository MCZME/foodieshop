package mczme.foodieshop.registry;

import java.util.function.Supplier;
import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.block.CashierDeskBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, FoodieShop.MODID);

    public static final Supplier<Block> CASHIER_DESK_BLOCK = BLOCKS.register("cashier_desk",
            () -> new CashierDeskBlock(BlockBehaviour.Properties.of()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
