package mczme.foodieshop.client.entityrenderer;

import mczme.foodieshop.client.model.FoodieModel;
import mczme.foodieshop.entity.FoodieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FoodieEntityRenderer extends GeoEntityRenderer<FoodieEntity> {
    public FoodieEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FoodieModel());
    }
    
}
