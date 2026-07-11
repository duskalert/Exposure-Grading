package com.example.exposure_grading.network;

import com.example.exposure_grading.ExposureGrading;
import com.example.exposure_grading.ModDataComponents;
import com.example.exposure_grading.data.PhotoRating;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SRatingPacket(int slotIndex, PhotoRating rating) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SRatingPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(ExposureGrading.MODID, "c2s_rating"));

    @Override
    public Type<C2SRatingPacket> type() {
        return TYPE;
    }

    public static void handle(C2SRatingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.containerMenu.getSlot(packet.slotIndex).getItem();
            if (!stack.isEmpty()) {
                stack.set(ModDataComponents.PHOTO_RATING.get(), packet.rating);
            }
        });
    }

    public static class Codec implements net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, C2SRatingPacket> {
        @Override
        public C2SRatingPacket decode(RegistryFriendlyByteBuf buf) {
            return new C2SRatingPacket(buf.readVarInt(), PhotoRating.STREAM_CODEC.decode(buf));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, C2SRatingPacket packet) {
            buf.writeVarInt(packet.slotIndex);
            PhotoRating.STREAM_CODEC.encode(buf, packet.rating);
        }
    }
}
