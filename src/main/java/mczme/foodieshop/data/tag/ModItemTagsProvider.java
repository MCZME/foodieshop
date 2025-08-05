package mczme.foodieshop.data.tag;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.registry.ModItems;
import mczme.foodieshop.registry.ModTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTags,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, FoodieShop.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider) {
        tag(ModTags.EDIT_PEN)
            .add(ModItems.DINER_BLUEPRINT_PEN.get())
            .add(ModItems.SHOP_PATH_PEN.get())
            .add(ModItems.SHOP_INVENTORY_PEN.get());
    }

}
