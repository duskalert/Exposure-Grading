package com.example.exposure_grading.screen;

import com.example.exposure_grading.ExposureGrading;
import com.example.exposure_grading.ModDataComponents;
import com.example.exposure_grading.api.ImageUtil;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.concurrent.CompletableFuture;

public class ReviewTableScreen extends AbstractContainerScreen<ReviewTableMenu> {
    private static final ResourceLocation BG_DEFAULT = ResourceLocation.fromNamespaceAndPath(ExposureGrading.MODID, "textures/gui/review_table.png");
    private static final ResourceLocation BG_COZY = ResourceLocation.fromNamespaceAndPath(ExposureGrading.MODID, "textures/gui/review_table_cozy.png");

    private ResourceLocation bgTexture;
    private Component statusText = Component.empty();
    private boolean ratingInProgress = false;
    private Button rateButton;

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

        rateButton = addRenderableWidget(Button.builder(Component.translatable("gui.exposure_grading.rate"), button -> onRate())
                .bounds(leftPos + 79, topPos + 34, 60, 20)
                .build());

        ItemStack stack = menu.getSlot(0).getItem();
        if (!stack.isEmpty()) {
            String state = stack.get(ModDataComponents.RATING_STATE.get());
            if ("rating".equals(state)) {
                ratingInProgress = true;
                rateButton.active = false;
                statusText = Component.translatable("gui.exposure_grading.rating");
            }
        }
    }

    private void onRate() {
        if (ratingInProgress) return;
        ItemStack stack = menu.getSlot(0).getItem();
        if (stack.isEmpty()) return;

        if (stack.has(ModDataComponents.PHOTO_RATING.get())) {
            statusText = Component.translatable("gui.exposure_grading.already_rated");
            return;
        }

        String state = stack.get(ModDataComponents.RATING_STATE.get());
        if ("rating".equals(state)) {
            statusText = Component.translatable("gui.exposure_grading.rating");
            return;
        }

        ratingInProgress = true;
        rateButton.active = false;
        stack.set(ModDataComponents.RATING_STATE.get(), "rating");
        statusText = Component.translatable("gui.exposure_grading.rating");
        CompletableFuture.runAsync(this::doRating);
    }

    private void doRating() {
        try {
            ItemStack stack = menu.getSlot(0).getItem();
            if (stack.isEmpty()) {
                Minecraft.getInstance().execute(() -> {
                    ratingInProgress = false;
                    rateButton.active = true;
                });
                return;
            }

            Frame frame = stack.get(Exposure.DataComponents.PHOTOGRAPH_FRAME);
            if (frame == null) {
                Minecraft.getInstance().execute(() -> {
                    ratingInProgress = false;
                    rateButton.active = true;
                    statusText = Component.translatable("gui.exposure_grading.error_no_frame");
                    clearRatingState();
                });
                return;
            }

            var ident = frame.identifier();
            String exposureId = ident.id();
            if (exposureId == null) {
                Minecraft.getInstance().execute(() -> {
                    ratingInProgress = false;
                    rateButton.active = true;
                    statusText = Component.translatable("gui.exposure_grading.error_no_id");
                    clearRatingState();
                });
                return;
            }

            var request = ExposureClient.exposureStore().getOrRequest(exposureId);
            var opt = request.getData();
            if (opt.isEmpty()) {
                Minecraft.getInstance().execute(() -> {
                    ratingInProgress = false;
                    rateButton.active = true;
                    statusText = Component.translatable("gui.exposure_grading.error_no_data");
                    clearRatingState();
                });
                return;
            }
            ExposureData exposureData = opt.get();

            String base64 = ImageUtil.exposureToBase64Png(exposureData);
            if (base64.isEmpty()) {
                Minecraft.getInstance().execute(() -> {
                    ratingInProgress = false;
                    rateButton.active = true;
                    statusText = Component.translatable("gui.exposure_grading.error_image");
                    clearRatingState();
                });
                return;
            }

            byte[] pngBytes = java.util.Base64.getDecoder().decode(base64);
            BlockPos bePos = menu.getBlockEntity() != null ? menu.getBlockEntity().getBlockPos() : null;

            Minecraft.getInstance().execute(() -> {
                if (bePos != null) {
                    PacketDistributor.sendToServer(new C2SRatingPacket(bePos, pngBytes));
                } else {
                    ratingInProgress = false;
                    rateButton.active = true;
                    clearRatingState();
                    statusText = Component.literal("§cBlock entity not found!");
                }
            });
        } catch (Exception e) {
            Minecraft.getInstance().execute(() -> {
                ratingInProgress = false;
                rateButton.active = true;
                statusText = Component.literal("§c" + e.getMessage());
                clearRatingState();
            });
        }
    }

    private void clearRatingState() {
        ItemStack s = menu.getSlot(0).getItem();
        if (!s.isEmpty()) {
            s.remove(ModDataComponents.RATING_STATE.get());
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(bgTexture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(this.font, statusText, 80, 68, 0xAAAAAA);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
