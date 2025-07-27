package mczme.foodieshop.event;

import mczme.foodieshop.FoodiesShop;
import mczme.foodieshop.entity.FoodieEntity;
import mczme.foodieshop.registry.ModEntityTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = FoodiesShop.MODID, bus = Bus.MOD)
public class ModEvent {
    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.FOODIE.get(), FoodieEntity.createAttributes().build());
    }

}
