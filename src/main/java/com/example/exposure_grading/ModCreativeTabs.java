package com.example.exposure_grading;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExposureGrading.MODID);

    public static final Supplier<CreativeModeTab> EXPOSURE_GRADING_TAB = TABS.register("exposure_grading_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.exposure_grading"))
                    .icon(() -> ModItems.REVIEW_TABLE.toStack())
                    .displayItems((params, output) -> {
                        output.accept(ModItems.REVIEW_TABLE);
                    })
                    .build());
}
