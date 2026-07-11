package com.example.exposure_grading.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;

public record PhotoRating(
        float composition,
        float lighting,
        float creativity,
        String comment
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, PhotoRating> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, PhotoRating::composition,
            ByteBufCodecs.FLOAT, PhotoRating::lighting,
            ByteBufCodecs.FLOAT, PhotoRating::creativity,
            ByteBufCodecs.STRING_UTF8, PhotoRating::comment,
            PhotoRating::new
    );
}
