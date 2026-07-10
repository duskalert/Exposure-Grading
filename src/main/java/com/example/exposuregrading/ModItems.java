package com.example.exposuregrading;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExposureGrading.MODID);

    public static final DeferredItem<?> REVIEW_TABLE = ITEMS.registerSimpleBlockItem(ModBlocks.REVIEW_TABLE);
}
