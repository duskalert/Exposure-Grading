package com.example.exposuregrading;

import com.example.exposuregrading.screen.ReviewTableScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(ExposureGrading.MODID)
public class ExposureGrading {
    public static final String MODID = "exposuregrading";

    public ExposureGrading(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenuTypes.MENUS.register(modBus);

        if (FMLEnvironment.dist.isClient()) {
            modBus.addListener(RegisterMenuScreensEvent.class, ClientSetup::onRegisterMenuScreens);
        }
    }
}
