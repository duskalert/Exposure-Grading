package com.example.exposure_grading;

import com.example.exposure_grading.block.ReviewTableBlockEntity;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExposureGrading.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReviewTableBlockEntity>> REVIEW_TABLE = BLOCK_ENTITIES.register("review_table",
            () -> BlockEntityType.Builder.of(ReviewTableBlockEntity::new, ModBlocks.REVIEW_TABLE.get()).build(null));
}
