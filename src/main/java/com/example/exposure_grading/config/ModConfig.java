package com.example.exposure_grading.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        var pair = new ModConfigSpec.Builder().configure(builder -> {
            builder.push("client");
            var k = builder.comment("GLM-4V-Flash API Key from https://open.bigmodel.cn").define("apiKey", "");
            var u = builder.comment("API endpoint URL").define("apiUrl", "https://open.bigmodel.cn/api/paas/v4/chat/completions");
            var w1 = builder.comment("Weight for composition (0.0 ~ 1.0)").defineInRange("weightComposition", 1.0d, 0.0d, 1.0d);
            var w2 = builder.comment("Weight for tone (0.0 ~ 1.0)").defineInRange("weightTone", 1.0d, 0.0d, 1.0d);
            var w3 = builder.comment("Weight for creativity (0.0 ~ 1.0)").defineInRange("weightCreativity", 1.0d, 0.0d, 1.0d);
            var w4 = builder.comment("Weight for content (0.0 ~ 1.0)").defineInRange("weightContent", 1.0d, 0.0d, 1.0d);
            builder.pop();
            return new Client(k, u, w1, w2, w3, w4);
        });
        CLIENT = pair.getKey();
        CLIENT_SPEC = pair.getValue();
    }

    public record Client(
            ModConfigSpec.ConfigValue<String> apiKey,
            ModConfigSpec.ConfigValue<String> apiUrl,
            ModConfigSpec.ConfigValue<Double> weightComposition,
            ModConfigSpec.ConfigValue<Double> weightTone,
            ModConfigSpec.ConfigValue<Double> weightCreativity,
            ModConfigSpec.ConfigValue<Double> weightContent
    ) {}
}
