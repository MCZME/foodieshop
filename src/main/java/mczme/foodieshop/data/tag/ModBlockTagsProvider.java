package mczme.foodieshop.data.tag;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import mczme.foodieshop.FoodieShop;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockTagsProvider extends BlockTagsProvider  {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, FoodieShop.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider) {
        
    }

}
