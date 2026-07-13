package com.example.exposure_grading.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;

public record PhotoRating(
        float composition,
        float tone,
        float creativity,
        float content,
        float totalScore,
        String comment
) {
    public static final StreamCodec<FriendlyByteBuf, PhotoRating> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, PhotoRating::composition,
            ByteBufCodecs.FLOAT, PhotoRating::tone,
            ByteBufCodecs.FLOAT, PhotoRating::creativity,
            ByteBufCodecs.FLOAT, PhotoRating::content,
            ByteBufCodecs.FLOAT, PhotoRating::totalScore,
            ByteBufCodecs.STRING_UTF8, PhotoRating::comment,
            PhotoRating::new
    );
}
