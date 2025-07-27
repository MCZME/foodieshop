package mczme.foodieshop.registry;

import java.util.function.Supplier;

import mczme.foodieshop.FoodiesShop;
import mczme.foodieshop.entity.FoodieEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, FoodiesShop.MODID);

    public static final Supplier<EntityType<FoodieEntity>> FOODIE =
            ENTITY_TYPES.register("foodie",
                    () -> EntityType.Builder.of(FoodieEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .build("foodie"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
