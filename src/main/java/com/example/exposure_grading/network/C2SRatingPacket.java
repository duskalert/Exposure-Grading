package com.example.exposure_grading.network;

import com.example.exposure_grading.ExposureGrading;
import com.example.exposure_grading.ModDataComponents;
import com.example.exposure_grading.api.GlmApiClient;
import com.example.exposure_grading.block.ReviewTableBlockEntity;
import com.example.exposure_grading.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

public record C2SRatingPacket(BlockPos pos, byte[] pngData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<C2SRatingPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(ExposureGrading.MODID, "c2s_rating"));

    @Override
    public Type<C2SRatingPacket> type() {
        return TYPE;
    }

    public static void handle(C2SRatingPacket packet, IPayloadContext context) {
        System.out.println("C2S HANDLER START pngData.length=" + packet.pngData.length);
        context.enqueueWork(() -> {
            try {
                String apiKey = ModConfig.SERVER.apiKey().get();
                String apiUrl = ModConfig.SERVER.apiUrl().get();
                if (apiKey.isEmpty()) {
                    ExposureGrading.LOGGER.error("API key is empty, aborting rating");
                    return;
                }

                BlockPos pos = packet.pos.immutable();
                byte[] data = packet.pngData;

                // Set "rating" state immediately on server BE
                setRatingState(pos, "rating", context);

                CompletableFuture.runAsync(() -> {
                    try {
                        String base64 = java.util.Base64.getEncoder().encodeToString(data);
                        ExposureGrading.LOGGER.info("Calling API with {} bytes of PNG data", data.length);
                        String prompt = """
你是一位严格的 Minecraft 摄影评委。请从以下四个维度评价这张像素风格照片，每项打分0-10分（精确到一位小数，如 4.5、6.0），并给出简短评语。

评分标准（请严格控制分数，不要给虚高分数）：
1. 构图(composition)：主体是否突出、画面是否平衡对称、引导线与框架运用、视觉中心把控
2. 影调(tone)：光影层次是否丰富、色彩搭配是否协调、昼夜/天气氛围营造、整体色调感染力
3. 创意(creativity)：拍摄角度是否独特、场景设计是否有想象力、是否展现他人未见过的视角、构图巧思
4. 内容(content)：画面是否有故事感、能否引发情感共鸣、主体是否有吸引力、场景互动趣味性

分数参照（请严格遵守）：
- 1-4：有明显缺陷或欠缺
- 5：及格，中规中矩
- 6：还行，有小亮点
- 7：良好，有值得肯定的地方
- 8：优秀，令人印象深刻
- 9：非常出色
- 10：完美无缺

请务必根据照片实际水平客观打分，不要因为同情而给高分。大多数普通照片应该在 4-6 分之间。

注意：这是 Minecraft 像素风格摄影作品，请用相应的审美标准评判，不要与真实摄影混为一谈。

请只输出以下 JSON，不要包含任何其他文字、注解、代码块标记：
{"composition": 0.0, "tone": 0.0, "creativity": 0.0, "content": 0.0, "comment": "一句简评"}
""";
                        var result = GlmApiClient.call(apiUrl, apiKey, ModConfig.SERVER.modelName().get(), prompt, base64);
                        ExposureGrading.LOGGER.info("API call completed, success={}, message={}", result.success(), result.message());
                        if (!result.success() || result.rating() == null) {
                            setRatingState(pos, null, context);
                            return;
                        }

                        // Compute total with server weights
                        com.example.exposure_grading.data.PhotoRating raw = result.rating();
                        double w1 = ModConfig.SERVER.weightComposition().get();
                        double w2 = ModConfig.SERVER.weightTone().get();
                        double w3 = ModConfig.SERVER.weightCreativity().get();
                        double w4 = ModConfig.SERVER.weightContent().get();
                        double sum = w1 + w2 + w3 + w4;
                        float total = sum == 0 ? 0 : (float)((raw.composition() * w1 + raw.tone() * w2 + raw.creativity() * w3 + raw.content() * w4) / sum);
                        var rated = new com.example.exposure_grading.data.PhotoRating(raw.composition(), raw.tone(), raw.creativity(), raw.content(), total, raw.comment());

                        context.enqueueWork(() -> writeRating(pos, rated, context));
                    } catch (Exception e) {
                        setRatingState(pos, null, context);
                        ExposureGrading.LOGGER.error("Rating API call failed", e);
                    }
                });
            } catch (Exception e) {
                ExposureGrading.LOGGER.error("Rating trigger failed", e);
            }
        });
    }

    private static void setRatingState(BlockPos pos, String state, IPayloadContext context) {
        try {
            if (context.player().level().getBlockEntity(pos) instanceof ReviewTableBlockEntity be) {
                if (be.hasPhotograph()) {
                    if (state != null) {
                        be.getPhotograph().set(ModDataComponents.RATING_STATE.get(), state);
                        be.getPhotograph().set(ModDataComponents.RATING_START_TIME.get(), System.currentTimeMillis());
                    } else {
                        be.getPhotograph().remove(ModDataComponents.RATING_STATE.get());
                        be.getPhotograph().remove(ModDataComponents.RATING_START_TIME.get());
                    }
                    be.setChanged();
                }
            }
        } catch (Exception e) {
            ExposureGrading.LOGGER.error("Set rating state failed", e);
        }
    }

    private static void writeRating(BlockPos pos, com.example.exposure_grading.data.PhotoRating rating, IPayloadContext context) {
        try {
            if (context.player().level().getBlockEntity(pos) instanceof ReviewTableBlockEntity be) {
                if (be.hasPhotograph()) {
                    be.getPhotograph().set(ModDataComponents.PHOTO_RATING.get(), rating);
                    be.getPhotograph().set(ModDataComponents.RATING_STATE.get(), "rated");
                    be.getPhotograph().remove(ModDataComponents.RATING_START_TIME.get());
                    be.setChanged();
                    ExposureGrading.LOGGER.info("Rating written to BE at {}", pos);
                    return;
                }
            }
        } catch (Exception e) {
            ExposureGrading.LOGGER.error("Write rating failed", e);
        }
    }

    public static final StreamCodec<FriendlyByteBuf, C2SRatingPacket> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos);
                buf.writeVarInt(p.pngData.length);
                buf.writeBytes(p.pngData);
            },
            buf -> {
                BlockPos pos = buf.readBlockPos();
                int len = buf.readVarInt();
                byte[] data = new byte[len];
                buf.readBytes(data);
                return new C2SRatingPacket(pos, data);
            }
    );
}
