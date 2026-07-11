package com.example.exposure_grading.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        var pair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = pair.getKey();
        CLIENT_SPEC = pair.getValue();
    }

    public record Client(
            ModConfigSpec.ConfigValue<String> apiKey,
            ModConfigSpec.ConfigValue<String> apiUrl,
            ModConfigSpec.ConfigValue<Double> weightComposition,
            ModConfigSpec.ConfigValue<Double> weightLighting,
            ModConfigSpec.ConfigValue<Double> weightCreativity
    ) {
        Client(ModConfigSpec.Builder builder) {
            this(
                    builder.comment("GLM-4V-Flash API Key from https://open.bigmodel.cn")
                            .define("apiKey", ""),
                    builder.comment("API endpoint URL")
                            .define("apiUrl", "https://open.bigmodel.cn/api/paas/v4/chat/completions"),
                    builder.comment("Weight for composition (0.0 ~ 1.0)")
                            .defineInRange("weightComposition", 1.0d, 0.0d, 1.0d),
                    builder.comment("Weight for lighting (0.0 ~ 1.0)")
                            .defineInRange("weightLighting", 1.0d, 0.0d, 1.0d),
                    builder.comment("Weight for creativity (0.0 ~ 1.0)")
                            .defineInRange("weightCreativity", 1.0d, 0.0d, 1.0d)
            );
        }
    }
}
