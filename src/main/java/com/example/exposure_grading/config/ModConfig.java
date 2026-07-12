package com.example.exposure_grading.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;
    public static final ModConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        var clientPair = new ModConfigSpec.Builder().configure(builder -> {
            builder.push("client");
            var w1 = builder.comment("Weight for composition").defineInRange("weightComposition", 1.0d, 0.0d, 1.0d);
            var w2 = builder.comment("Weight for tone").defineInRange("weightTone", 1.0d, 0.0d, 1.0d);
            var w3 = builder.comment("Weight for creativity").defineInRange("weightCreativity", 1.0d, 0.0d, 1.0d);
            var w4 = builder.comment("Weight for content").defineInRange("weightContent", 1.0d, 0.0d, 1.0d);
            builder.pop();
            return new Client(w1, w2, w3, w4);
        });
        CLIENT = clientPair.getKey();
        CLIENT_SPEC = clientPair.getValue();

        var serverPair = new ModConfigSpec.Builder().configure(builder -> {
            builder.push("server");
            var k = builder.comment("API Key for GLM-4V-Flash").define("apiKey", "");
            var u = builder.comment("API endpoint URL").define("apiUrl", "https://open.bigmodel.cn/api/paas/v4/chat/completions");
            builder.pop();
            return new Server(k, u);
        });
        SERVER = serverPair.getKey();
        SERVER_SPEC = serverPair.getValue();
    }

    public record Client(
            ModConfigSpec.ConfigValue<Double> weightComposition,
            ModConfigSpec.ConfigValue<Double> weightTone,
            ModConfigSpec.ConfigValue<Double> weightCreativity,
            ModConfigSpec.ConfigValue<Double> weightContent
    ) {}

    public record Server(
            ModConfigSpec.ConfigValue<String> apiKey,
            ModConfigSpec.ConfigValue<String> apiUrl
    ) {}
}
