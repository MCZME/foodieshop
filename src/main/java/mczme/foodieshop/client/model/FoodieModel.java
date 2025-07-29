package mczme.foodieshop.client.model;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.entity.FoodieEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FoodieModel extends GeoModel<FoodieEntity> {

    @Override
    public ResourceLocation getModelResource(FoodieEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(FoodieShop.MODID, "geo/foodie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FoodieEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(FoodieShop.MODID, "textures/entity/foodie.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FoodieEntity animatable) {
        return null;
    }

}
