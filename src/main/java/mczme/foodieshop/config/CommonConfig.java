package mczme.foodieshop.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue CAN_MODIFY_TRADING_CONFIG;

    static {
        CAN_MODIFY_TRADING_CONFIG = BUILDER
                .comment("aaa")
                .define("canModifyTradingConfig", true); // 默认为 true
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
