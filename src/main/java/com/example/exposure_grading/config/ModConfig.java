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

    public record Client(ModConfigSpec.ConfigValue<String> apiKey, ModConfigSpec.ConfigValue<String> apiUrl) {
        Client(ModConfigSpec.Builder builder) {
            this(
                    builder.comment("GLM-4V-Flash API Key").define("apiKey", ""),
                    builder.comment("API endpoint").define("apiUrl", "https://open.bigmodel.cn/api/paas/v4/chat/completions")
            );
        }
    }
}
