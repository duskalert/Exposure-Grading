package com.example.exposure_grading.api;

import com.example.exposure_grading.ExposureGrading;
import com.example.exposure_grading.data.PhotoRating;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GlmApiClient {
    public static PhotoRateResult call(String apiUrl, String apiKey, String prompt, String base64Png) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("model", "glm-4v-flash");

            JsonArray messages = new JsonArray();
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");

            JsonArray content = new JsonArray();

            JsonObject textPart = new JsonObject();
            textPart.addProperty("type", "text");
            textPart.addProperty("text", prompt);
            content.add(textPart);

            JsonObject imgPart = new JsonObject();
            imgPart.addProperty("type", "image_url");
            JsonObject urlObj = new JsonObject();
            urlObj.addProperty("url", "data:image/png;base64," + base64Png);
            imgPart.add("image_url", urlObj);
            content.add(imgPart);

            userMsg.add("content", content);
            messages.add(userMsg);
            body.add("messages", messages);

            byte[] jsonBytes = body.toString().getBytes(StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBytes);
            }

            int code = conn.getResponseCode();
            String respBody;
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                respBody = scanner.useDelimiter("\\A").next();
            }

            if (code != 200) {
                return new PhotoRateResult(false, "HTTP " + code + ": " + respBody, null);
            }

            JsonObject json = JsonParser.parseString(respBody).getAsJsonObject();
            String text = json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            return new PhotoRateResult(true, text, parseRating(text));
        } catch (Exception e) {
            return new PhotoRateResult(false, e.getMessage(), null);
        }
    }

    private static PhotoRating parseRating(String text) {
        try {
            JsonObject json = JsonParser.parseString(text).getAsJsonObject();
            return new PhotoRating(
                    getFloat(json, "composition"),
                    getFloat(json, "lighting"),
                    getFloat(json, "creativity"),
                    getString(json, "comment")
            );
        } catch (Exception e) {
            return null;
        }
    }

    private static float getFloat(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsFloat() : 0;
    }

    private static String getString(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsString() : "";
    }

    public record PhotoRateResult(boolean success, String message, PhotoRating rating) {}
}
