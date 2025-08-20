package mczme.foodieshop.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class ItemUtils {

    /**
     * 从JSON对象创建ItemStack。
     *
     * @param json       代表物品的JSON对象。
     * @param registries 用于注册表查找的HolderLookup.Provider。
     * @return 创建的ItemStack。
     * @throws JsonSyntaxException 如果JSON格式错误或缺少必需字段。
     */
    @NotNull
    public static ItemStack fromJson(JsonObject json, HolderLookup.Provider registries) {
        // 1. 解析物品ID
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("id").getAsString()));
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        // 2. 解析数量
        int count = json.has("count") ? json.get("count").getAsInt() : 1;
        ItemStack itemStack = new ItemStack(item, count);

        // 3. 解析并应用组件
        if (json.has("components")) {
            JsonObject componentsJson = json.getAsJsonObject("components");
            for (Map.Entry<String, JsonElement> entry : componentsJson.entrySet()) {
                ResourceLocation componentId = ResourceLocation.parse(entry.getKey());
                Optional<DataComponentType<?>> componentTypeOpt = BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(componentId);

                if (componentTypeOpt.isPresent()) {
                    DataComponentType<?> componentType = componentTypeOpt.get();
                    JsonElement componentValueJson = entry.getValue();
                    applyComponent(itemStack, componentType, componentValueJson, registries);
                } else {
                    // 记录或处理未知的组件类型
                }
            }
        }

        return itemStack;
    }

    private static <T> void applyComponent(ItemStack stack, DataComponentType<T> type, JsonElement json, HolderLookup.Provider registries) {
        if (type.codec() == null) return;

        Optional<T> value = type.codec().parse(registries.createSerializationContext(JsonOps.INSTANCE), json)
                .resultOrPartial(error -> {
                    // 记录错误
                });

        value.ifPresent(v -> stack.set(type, v));
    }
}
