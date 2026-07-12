package com.example.exposure_grading.network;

import com.example.exposure_grading.ExposureGrading;
import com.example.exposure_grading.ModDataComponents;
import com.example.exposure_grading.api.GlmApiClient;
import com.example.exposure_grading.block.ReviewTableBlockEntity;
import com.example.exposure_grading.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
        context.enqueueWork(() -> {
            try {
                ExposureGrading.LOGGER.info("C2S handler executing");
                String apiKey = ModConfig.SERVER.apiKey().get();
                String apiUrl = ModConfig.SERVER.apiUrl().get();
                ExposureGrading.LOGGER.info("apiKey empty={}, apiUrl={}", apiKey.isEmpty(), apiUrl);
                if (apiKey.isEmpty()) return;

                BlockPos pos = packet.pos.immutable();
                byte[] data = packet.pngData;

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
                        ExposureGrading.LOGGER.info("API result: success={}", result.success());
                        if (!result.success() || result.rating() == null) return;

                        context.enqueueWork(() -> writeRating(pos, result.rating(), context));
                    } catch (Exception e) {
                        ExposureGrading.LOGGER.error("Rating API call failed", e);
                    }
                });
            } catch (Exception e) {
                ExposureGrading.LOGGER.error("Rating trigger failed", e);
            }
        });
    }

    private static void writeRating(BlockPos pos, com.example.exposure_grading.data.PhotoRating rating, IPayloadContext context) {
        try {
            if (context.player().level().getBlockEntity(pos) instanceof ReviewTableBlockEntity be) {
                if (be.hasPhotograph()) {
                    be.getPhotograph().set(ModDataComponents.PHOTO_RATING.get(), rating);
                    be.setChanged();
                    return;
                }
            }
            for (ItemStack item : context.player().getInventory().items) {
                if (!item.isEmpty() && item.get(ModDataComponents.RATING_STATE.get()) != null) {
                    item.set(ModDataComponents.PHOTO_RATING.get(), rating);
                    return;
                }
            }
        } catch (Exception e) {
            ExposureGrading.LOGGER.error("Write rating failed", e);
        }
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRatingPacket> CODEC = StreamCodec.of(
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
