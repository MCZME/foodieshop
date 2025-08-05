package mczme.foodieshop.data.modelprovider;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class FoodieShopModelProvider extends ItemModelProvider {

    public FoodieShopModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, FoodieShop.MODID, existingFileHelper);

    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.DINER_BLUEPRINT_PEN.get());
        basicItem(ModItems.SHOP_PATH_PEN.get());
        basicItem(ModItems.SHOP_INVENTORY_PEN.get());
    }

}
