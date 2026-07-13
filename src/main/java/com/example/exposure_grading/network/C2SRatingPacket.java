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
                if (apiKey.isEmpty()) return;

                BlockPos pos = packet.pos.immutable();
                byte[] data = packet.pngData;

                // Set "rating" state immediately on server BE
                setRatingState(pos, "rating", context);

                CompletableFuture.runAsync(() -> {
                    try {
                        String base64 = java.util.Base64.getEncoder().encodeToString(data);
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
                    } else {
                        be.getPhotograph().remove(ModDataComponents.RATING_STATE.get());
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
                    be.setChanged();
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
