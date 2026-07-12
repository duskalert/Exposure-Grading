package com.example.exposure_grading.config;

import com.example.exposure_grading.ExposureGrading;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(ExposureGrading.MODID + "-client.json");

    public static final ClientConfig CLIENT = new ClientConfig();

    public static class ClientConfig {
        public String apiKey = "";
        public String apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
        public double weightComposition = 1.0;
        public double weightTone = 1.0;
        public double weightCreativity = 1.0;
        public double weightContent = 1.0;

        private ClientConfig() {
            load();
        }

        public void load() {
            try {
                if (Files.exists(CONFIG_PATH)) {
                    String json = Files.readString(CONFIG_PATH);
                    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                    if (obj.has("apiKey")) apiKey = obj.get("apiKey").getAsString();
                    if (obj.has("apiUrl")) apiUrl = obj.get("apiUrl").getAsString();
                    if (obj.has("weightComposition")) weightComposition = obj.get("weightComposition").getAsDouble();
                    if (obj.has("weightTone")) weightTone = obj.get("weightTone").getAsDouble();
                    if (obj.has("weightCreativity")) weightCreativity = obj.get("weightCreativity").getAsDouble();
                    if (obj.has("weightContent")) weightContent = obj.get("weightContent").getAsDouble();
                }
            } catch (Exception ignored) {
            }
        }

        public void save() {
            try {
                JsonObject obj = new JsonObject();
                obj.add("apiKey", new JsonPrimitive(apiKey));
                obj.add("apiUrl", new JsonPrimitive(apiUrl));
                obj.add("weightComposition", new JsonPrimitive(weightComposition));
                obj.add("weightTone", new JsonPrimitive(weightTone));
                obj.add("weightCreativity", new JsonPrimitive(weightCreativity));
                obj.add("weightContent", new JsonPrimitive(weightContent));
                Files.createDirectories(CONFIG_PATH.getParent());
                Files.writeString(CONFIG_PATH, obj.toString());
            } catch (IOException ignored) {
            }
        }
    }
}
