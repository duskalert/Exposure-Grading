package com.example.exposure_grading.network;

import com.example.exposure_grading.ExposureGrading;
import com.example.exposure_grading.ModDataComponents;
import com.example.exposure_grading.block.ReviewTableBlockEntity;
import com.example.exposure_grading.data.PhotoRating;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SRatingPacket(PhotoRating rating, BlockPos pos, String exposureId, String ratingState) implements CustomPacketPayload {

    private static final ResourceLocation TYPE_RL = ResourceLocation.fromNamespaceAndPath(ExposureGrading.MODID, "c2s_rating");
    public static final CustomPacketPayload.Type<C2SRatingPacket> TYPE = new CustomPacketPayload.Type<>(TYPE_RL);

    @Override
    public Type<C2SRatingPacket> type() {
        return TYPE;
    }

    private static void applyRating(ItemStack stack, PhotoRating rating) {
        stack.set(ModDataComponents.PHOTO_RATING.get(), rating);
    }

    public static void handle(C2SRatingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                // Try block entity first (player still has GUI open)
                if (context.player().level().getBlockEntity(packet.pos) instanceof ReviewTableBlockEntity be) {
                    if (be.hasPhotograph()) {
                        applyRating(be.getPhotograph(), packet.rating);
                        be.setChanged();
                        ExposureGrading.LOGGER.info("Rating applied to BE at {}", packet.pos);
                        return;
                    }
                }
                // Fallback: scan player inventory for matching photograph
                if (!packet.exposureId.isEmpty()) {
                    for (ItemStack item : context.player().getInventory().items) {
                        if (!item.isEmpty() && item.get(ModDataComponents.PHOTO_RATING.get()) == null) {
                            // Found an unrated photograph in inventory; apply rating
                            applyRating(item, packet.rating);
                            ExposureGrading.LOGGER.info("Rating applied to inventory item");
                            return;
                        }
                    }
                }
                ExposureGrading.LOGGER.warn("No target found for rating (pos={}, exposureId={})", packet.pos, packet.exposureId);
            } catch (Exception e) {
                ExposureGrading.LOGGER.error("Failed to apply rating", e);
            }
        });
    }

    public static class Codec implements net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, C2SRatingPacket> {
        @Override
        public C2SRatingPacket decode(RegistryFriendlyByteBuf buf) {
            return new C2SRatingPacket(
                    PhotoRating.STREAM_CODEC.decode(buf),
                    buf.readBlockPos(),
                    buf.readUtf(),
                    buf.readUtf()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, C2SRatingPacket packet) {
            PhotoRating.STREAM_CODEC.encode(buf, packet.rating);
            buf.writeBlockPos(packet.pos);
            buf.writeUtf(packet.exposureId != null ? packet.exposureId : "");
            buf.writeUtf(packet.ratingState != null ? packet.ratingState : "");
        }
    }
}
