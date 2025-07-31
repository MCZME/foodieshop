package mczme.foodieshop.registry;

import mczme.foodieshop.FoodieShop;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static final TagKey<Item> EDIT_PEN = TagKey.create(
        Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(FoodieShop.MODID, "edit_pen")
    );
}
