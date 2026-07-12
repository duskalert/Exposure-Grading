package com.example.exposure_grading.screen;

import com.example.exposure_grading.ExposureGrading;
import com.example.exposure_grading.ModDataComponents;
import com.example.exposure_grading.api.GlmApiClient;
import com.example.exposure_grading.api.ImageUtil;
import com.example.exposure_grading.block.ReviewTableBlockEntity;
import com.example.exposure_grading.config.ModConfig;
import com.example.exposure_grading.data.PhotoRating;
import com.example.exposure_grading.menu.ReviewTableMenu;
import com.example.exposure_grading.network.C2SRatingPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
    }

    private void onRate() {
        if (ratingInProgress) return;
        ItemStack stack = menu.getSlot(0).getItem();
        if (stack.isEmpty()) return;

        if (stack.has(ModDataComponents.PHOTO_RATING.get())) {
            statusText = Component.translatable("gui.exposure_grading.already_rated");
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

            String exposureId = frame.identifier().getId().orElse(null);
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

            String apiKey = ModConfig.CLIENT.apiKey().get();
            String apiUrl = ModConfig.CLIENT.apiUrl().get();
            if (apiKey.isEmpty()) {
                Minecraft.getInstance().execute(() -> {
                    ratingInProgress = false;
                    rateButton.active = true;
                    statusText = Component.translatable("gui.exposure_grading.error_no_key");
                    clearRatingState();
                });
                return;
            }

            String prompt = """
你是一位专业的摄影评委。请从以下四个维度评价这张照片，每项打分0-10分（精确到一位小数），并给出简短评语。

评分标准：
- 构图(composition)：主体突出、画面平衡、线条引导、黄金分割/三分法等构图手法的运用
- 影调(tone)：光影层次、明暗对比、色彩协调、氛围营造
- 创意(creativity)：拍摄角度、主题表达、独特视角、想象力
- 内容(content)：画面故事性、情感传达、主体吸引力、趣味性

请严格按照以下JSON格式返回，不要包含其他文字：
{"composition": 0.0, "tone": 0.0, "creativity": 0.0, "content": 0.0, "comment": "评语"}
""";
            var result = GlmApiClient.call(apiUrl, apiKey, prompt, base64);

            Minecraft.getInstance().execute(() -> {
                ratingInProgress = false;
                rateButton.active = true;
                if (result.success() && result.rating() != null) {
                    ItemStack slotStack = menu.getSlot(0).getItem();
                    if (!slotStack.isEmpty()) {
                        slotStack.set(ModDataComponents.PHOTO_RATING.get(), result.rating());
                        slotStack.set(ModDataComponents.RATING_STATE.get(), "rated");
                    }
                    PhotoRating finalRating = result.rating();
                    Minecraft mc = Minecraft.getInstance();
                    ExposureGrading.LOGGER.info("Rating success, writing to server...");
                    // Singleplayer: access server player's container menu directly from render thread
                    if (mc.getSingleplayerServer() != null) {
                        ExposureGrading.LOGGER.info("Singleplayer mode detected");
                        var serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                        if (serverPlayer != null) {
                            ExposureGrading.LOGGER.info("Found server player, containerMenu={}", serverPlayer.containerMenu);
                            if (serverPlayer.containerMenu instanceof ReviewTableMenu m) {
                                var ss = m.getSlot(0).getItem();
                                ExposureGrading.LOGGER.info("Server slot item: empty={}, hasRating={}", ss.isEmpty(), ss.has(ModDataComponents.PHOTO_RATING.get()));
                                if (!ss.isEmpty()) {
                                    ss.set(ModDataComponents.PHOTO_RATING.get(), finalRating);
                                    ExposureGrading.LOGGER.info("Rating written to server slot, now hasRating={}", ss.has(ModDataComponents.PHOTO_RATING.get()));
                                }
                            } else {
                                ExposureGrading.LOGGER.warn("Server player containerMenu is not ReviewTableMenu: {}", serverPlayer.containerMenu);
                            }
                        } else {
                            ExposureGrading.LOGGER.warn("Server player not found!");
                        }
                    }
                    // Dedicated server: C2S network packet as fallback
                    if (mc.getSingleplayerServer() == null) {
                        PacketDistributor.sendToServer(new C2SRatingPacket(finalRating, BlockPos.ZERO, "", ""));
                    }
                    statusText = Component.translatable("gui.exposure_grading.rated");
                } else {
                    ItemStack slotStack = menu.getSlot(0).getItem();
                    if (!slotStack.isEmpty()) {
                        slotStack.remove(ModDataComponents.RATING_STATE.get());
                    }
                    statusText = Component.literal("§c" + result.message());
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
