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
                        .map(msg -> (Component) Component.translatable(msg.type().getTranslationKey(), msg.args().toArray())) // 显式转换为 Component
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

    public enum MessageType {
        SHOP_VALIDATED_SUCCESS(0, "foodieshop.validation.success"),
        SHOP_VALIDATED_WARNING_NO_PATH(1, "foodieshop.validation.warning.no_path"),
        SHOP_VALIDATED_ERROR_NO_CASHIER(2, "foodieshop.validation.error.no_cashier"),
        SHOP_VALIDATED_ERROR_INVALID_PATH(3, "foodieshop.validation.error.invalid_path"),
        SHOP_VALIDATED_ERROR_NO_AREA(4, "foodieshop.validation.error.no_area"),
        SHOP_VALIDATED_ERROR_CASHIER_MISMATCH(5, "foodieshop.validation.error.cashier_mismatch"),
        SHOP_VALIDATED_ERROR_NO_TABLES(6, "foodieshop.validation.error.no_tables"),
        SHOP_VALIDATED_ERROR_INVALID_TABLE(7, "foodieshop.validation.error.invalid_table"),
        SHOP_VALIDATED_ERROR_NO_SEATS(8, "foodieshop.validation.error.no_seats"),
        SHOP_VALIDATED_ERROR_INVALID_SEAT(9, "foodieshop.validation.error.invalid_seat"),
        SHOP_VALIDATED_ERROR_SEAT_NOT_CONNECTED_TO_TABLE(10, "foodieshop.validation.error.seat_not_connected_to_table"),
        SHOP_VALIDATED_ERROR_NO_PATH(11, "foodieshop.validation.error.no_path"),        
        SHOP_VALIDATED_ERROR_NO_PATH_ENTRY(12, "foodieshop.validation.error.no_path_entry"),
        SHOP_VALIDATED_ERROR_NO_PATH_EXIT(13, "foodieshop.validation.error.no_path_exit"),
        SHOP_VALIDATED_ERROR_PATH_NOT_CONNECTED(14, "foodieshop.validation.error.path_not_connected"),
        SHOP_VALIDATED_ERROR_PATH_ENTRY_NOT_IN_NODES(15, "foodieshop.validation.error.path_entry_not_in_nodes"),
        SHOP_VALIDATED_ERROR_PATH_EXIT_NOT_IN_NODES(16, "foodieshop.validation.error.path_exit_not_in_nodes"),
        SHOP_VALIDATED_ERROR_SEAT_NOT_ADJACENT_TO_PATH(17, "foodieshop.validation.error.seat_not_adjacent_to_path"),
        SHOP_VALIDATED_ERROR_NO_MENU_ITEMS(18, "foodieshop.validation.error.no_menu_items"),
        SHOP_VALIDATED_ERROR_UNKNOWN(19, "foodieshop.validation.error.unknown");

        private final int id;
        private final String translationKey;

        MessageType(int id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public static final StreamCodec<RegistryFriendlyByteBuf, MessageType> STREAM_CODEC = StreamCodec.of(
                (buf, type) -> buf.writeVarInt(type.ordinal()),
                buf -> MessageType.values()[buf.readVarInt()]
        );
    }

    public record ValidationMessage(MessageType type, List<Component> args) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ValidationMessage> STREAM_CODEC = StreamCodec.composite(
                MessageType.STREAM_CODEC,
                ValidationMessage::type,
                ByteBufCodecs.collection(ArrayList::new, ComponentSerialization.STREAM_CODEC),
                ValidationMessage::args,
                ValidationMessage::new
        );
    }
}
