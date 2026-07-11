package com.example.exposure_grading.network;

import com.example.exposure_grading.ExposureGrading;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CTriggerRatingPacket(String exposureId, int slotIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CTriggerRatingPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(ExposureGrading.MODID, "s2c_trigger_rating"));

    @Override
    public Type<S2CTriggerRatingPacket> type() {
        return TYPE;
    }

    public static void handle(S2CTriggerRatingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Client will handle the API call and send back C2SRatingPacket
        });
    }

    public static class Codec implements net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, S2CTriggerRatingPacket> {
        @Override
        public S2CTriggerRatingPacket decode(RegistryFriendlyByteBuf buf) {
            return new S2CTriggerRatingPacket(buf.readUtf(), buf.readVarInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CTriggerRatingPacket packet) {
            buf.writeUtf(packet.exposureId);
            buf.writeVarInt(packet.slotIndex);
        }
    }
}
