package com.example.exposuregrading;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ExposureGrading.MODID)
public class ExposureGrading {
    public static final String MODID = "exposuregrading";

    public ExposureGrading(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
    }
}
