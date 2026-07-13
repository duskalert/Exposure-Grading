package com.example.exposure_grading;

import com.example.exposure_grading.data.PhotoRating;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(DATA_COMPONENT_TYPE, ExposureGrading.MODID);

    public static final Codec<PhotoRating> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("composition").forGetter(PhotoRating::composition),
            Codec.FLOAT.fieldOf("tone").forGetter(PhotoRating::tone),
            Codec.FLOAT.fieldOf("creativity").forGetter(PhotoRating::creativity),
            Codec.FLOAT.fieldOf("content").forGetter(PhotoRating::content),
            Codec.FLOAT.optionalFieldOf("totalScore", 0f).forGetter(PhotoRating::totalScore),
            Codec.STRING.fieldOf("comment").forGetter(PhotoRating::comment)
    ).apply(instance, PhotoRating::new));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PhotoRating>> PHOTO_RATING = COMPONENTS.register("photo_rating",
            () -> DataComponentType.<PhotoRating>builder()
                    .persistent(CODEC)
                    .networkSynchronized(PhotoRating.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> RATING_STATE = COMPONENTS.register("rating_state",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8)
                    .build());
}
