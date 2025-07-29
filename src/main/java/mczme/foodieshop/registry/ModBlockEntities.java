package mczme.foodieshop.registry;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.block.blockentity.CashierDeskBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FoodieShop.MODID);

    public static final Supplier<BlockEntityType<CashierDeskBlockEntity>> CASHIER_DESK_BE =
            BLOCK_ENTITIES.register("cashier_desk", () ->
                    BlockEntityType.Builder.of(CashierDeskBlockEntity::new, ModBlocks.CASHIER_DESK_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
