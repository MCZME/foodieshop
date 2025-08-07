package mczme.foodieshop.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TradingConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 这里为未来的配置项留空
    // 例如:
    public static final ModConfigSpec.BooleanValue SOME_FUTURE_OPTION = BUILDER
            .comment("一个未来的配置选项")
            .define("someFutureOption", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
