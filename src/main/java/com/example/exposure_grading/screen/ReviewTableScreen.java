package com.example.exposure_grading.screen;

import com.example.exposure_grading.ExposureGrading;
import com.example.exposure_grading.ModDataComponents;
import com.example.exposure_grading.api.GlmApiClient;
import com.example.exposure_grading.api.ImageUtil;
import com.example.exposure_grading.config.ModConfig;
import com.example.exposure_grading.menu.ReviewTableMenu;
import com.example.exposure_grading.network.C2SRatingPacket;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.concurrent.CompletableFuture;

public class ReviewTableScreen extends AbstractContainerScreen<ReviewTableMenu> {
    private static final ResourceLocation BG_DEFAULT = ResourceLocation.fromNamespaceAndPath(ExposureGrading.MODID, "textures/gui/review_table.png");
    private static final ResourceLocation BG_COZY = ResourceLocation.fromNamespaceAndPath(ExposureGrading.MODID, "textures/gui/review_table_cozy.png");

    private ResourceLocation bgTexture;
    private Component statusText = Component.empty();
    private boolean currentRating = false;

    public ReviewTableScreen(ReviewTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    private static boolean hasCozyPack() {
        try {
            for (var pack : Minecraft.getInstance().getResourcePackRepository().getSelectedPacks()) {
                if (pack.getId().toLowerCase().contains("cozy")) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    protected void init() {
        super.init();
        boolean cozy = hasCozyPack();
        boolean cozyTexExists = Minecraft.getInstance().getResourceManager().getResource(BG_COZY).isPresent();
        bgTexture = (cozy && cozyTexExists) ? BG_COZY : BG_DEFAULT;

        addRenderableWidget(Button.builder(Component.translatable("gui.exposure_grading.rate"), button -> onRate())
                .bounds(leftPos + 79, topPos + 34, 60, 20)
                .build());
    }

    private void onRate() {
        ItemStack stack = menu.getSlot(0).getItem();
        if (stack.isEmpty()) return;

        if (stack.has(ModDataComponents.PHOTO_RATING.get())) {
            statusText = Component.translatable("gui.exposure_grading.already_rated");
            return;
        }

        statusText = Component.translatable("gui.exposure_grading.rating");
        CompletableFuture.runAsync(this::doRating);
    }

    private void doRating() {
        try {
            ItemStack stack = menu.getSlot(0).getItem();
            if (stack.isEmpty()) return;

            Frame frame = stack.get(Exposure.DataComponents.PHOTOGRAPH_FRAME);
            if (frame == null) {
                Minecraft.getInstance().execute(() -> statusText = Component.translatable("gui.exposure_grading.error_no_frame"));
                return;
            }

            String exposureId = frame.identifier().getId().orElse(null);
            if (exposureId == null) {
                Minecraft.getInstance().execute(() -> statusText = Component.translatable("gui.exposure_grading.error_no_id"));
                return;
            }

            var request = ExposureClient.exposureStore().getOrRequest(exposureId);
            var opt = request.getData();
            if (opt.isEmpty()) {
                Minecraft.getInstance().execute(() -> statusText = Component.translatable("gui.exposure_grading.error_no_data"));
                return;
            }
            ExposureData exposureData = opt.get();

            String base64 = ImageUtil.exposureToBase64Png(exposureData);
            if (base64.isEmpty()) {
                Minecraft.getInstance().execute(() -> statusText = Component.translatable("gui.exposure_grading.error_image"));
                return;
            }

            String apiKey = ModConfig.CLIENT.apiKey().get();
            String apiUrl = ModConfig.CLIENT.apiUrl().get();
            if (apiKey.isEmpty()) {
                Minecraft.getInstance().execute(() -> statusText = Component.translatable("gui.exposure_grading.error_no_key"));
                return;
            }

            String prompt = ModConfig.CLIENT.prompt().get();
            var result = GlmApiClient.call(apiUrl, apiKey, prompt, base64);

            Minecraft.getInstance().execute(() -> {
                if (result.success() && result.rating() != null) {
                    statusText = Component.translatable("gui.exposure_grading.rated");
                    PacketDistributor.sendToServer(new C2SRatingPacket(0, result.rating()));
                    currentRating = true;
                } else {
                    statusText = Component.literal("§c" + result.message());
                }
            });
        } catch (Exception e) {
            Minecraft.getInstance().execute(() -> {
                statusText = Component.literal("§c" + e.getMessage());
            });
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(bgTexture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        ItemStack stack = menu.getSlot(0).getItem();
        if (stack.has(ModDataComponents.PHOTO_RATING.get())) {
            var r = stack.get(ModDataComponents.PHOTO_RATING.get());
            if (r != null) {
                guiGraphics.drawString(this.font, "构图: " + r.composition(), 80, 58, 0xFFFFFF);
                guiGraphics.drawString(this.font, "光影: " + r.lighting(), 80, 68, 0xFFFFFF);
                guiGraphics.drawString(this.font, "创意: " + r.creativity(), 80, 78, 0xFFFFFF);
            }
        }
        guiGraphics.drawString(this.font, statusText, 80, 100, 0xAAAAAA);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
