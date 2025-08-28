package mczme.foodieshop.network.packet.s2c;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.screen.ShopConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record ValidateShopResultPacket(ValidationResultType resultType, List<ValidationMessage> messages) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FoodieShop.MODID, "validate_shop_result");
    public static final Type<ValidateShopResultPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ValidateShopResultPacket> STREAM_CODEC = StreamCodec.composite(
            ValidationResultType.STREAM_CODEC,
            ValidateShopResultPacket::resultType,
            ByteBufCodecs.collection(ArrayList::new, ValidationMessage.STREAM_CODEC),
            ValidateShopResultPacket::messages,
            ValidateShopResultPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ValidateShopResultPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ShopConfigScreen shopConfigScreen) {
                List<Component> translatedMessages = packet.messages().stream()
                        .map(msg -> (Component) Component.translatable(msg.translationKey(), msg.args().toArray())) // 显式转换为 Component
                        .toList();
                shopConfigScreen.updateValidationResult(packet.resultType(), translatedMessages);
            }
        });
    }

    public enum ValidationResultType {
        SUCCESS,
        WARNING,
        ERROR;

        public static final StreamCodec<RegistryFriendlyByteBuf, ValidationResultType> STREAM_CODEC = StreamCodec.of(
                (buf, type) -> buf.writeVarInt(type.ordinal()),
                buf -> ValidationResultType.values()[buf.readVarInt()]
        );
    }

    public record ValidationMessage(String translationKey, List<Component> args) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ValidationMessage> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                ValidationMessage::translationKey,
                ByteBufCodecs.collection(ArrayList::new, ComponentSerialization.STREAM_CODEC),
                ValidationMessage::args,
                ValidationMessage::new
        );
    }
}
