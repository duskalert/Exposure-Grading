package com.example.exposure_grading;

import com.example.exposure_grading.config.ModConfig;
import com.example.exposure_grading.network.ModNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(ExposureGrading.MODID)
public class ExposureGrading {
    public static final String MODID = "exposure_grading";

    public ExposureGrading(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenuTypes.MENUS.register(modBus);
        ModDataComponents.COMPONENTS.register(modBus);

        ModConfig.CLIENT.save();
        modBus.addListener(ModNetworking::onRegisterPayload);

        if (FMLEnvironment.dist.isClient()) {
            modBus.addListener(RegisterMenuScreensEvent.class, ClientSetup::onRegisterMenuScreens);
        }
    }
}
