package com.example.exposure_grading.api;

import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class ImageUtil {
    public static String exposureToBase64Png(ExposureData data) {
        int w = data.getWidth();
        int h = data.getHeight();
        byte[] pixels = data.getPixels();

        var registry = Minecraft.getInstance().level.registryAccess();
        var holder = ColorPalettes.get(registry, data.getPaletteId());
        ColorPalette palette = holder.value();

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = pixels[y * w + x] & 0xFF;
                img.setRGB(x, y, palette.byId(idx));
            }
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }
}
