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
            ModConfigSpec.ConfigValue<String> prompt
    ) {
        Client(ModConfigSpec.Builder builder) {
            this(
                    builder.comment("GLM-4V-Flash API Key from https://open.bigmodel.cn")
                            .define("apiKey", ""),
                    builder.comment("API endpoint URL")
                            .define("apiUrl", "https://open.bigmodel.cn/api/paas/v4/chat/completions"),
                    builder.comment("Rating prompt sent to the AI. Use {json_format} as placeholder.")
                            .define("prompt",
                                    "请以JSON格式评价这张照片，包含字段：composition(float 构图)、lighting(float 光影)、creativity(float 创意)、comment(string 评语)。只返回JSON，不要其他文字。")
            );
        }
    }
}
