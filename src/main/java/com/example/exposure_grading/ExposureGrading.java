package com.example.exposure_grading;

import com.example.exposure_grading.config.ModConfig;
import com.example.exposure_grading.network.ModNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static com.example.exposure_grading.config.ModConfig.CLIENT_SPEC;
import static com.example.exposure_grading.config.ModConfig.SERVER_SPEC;

@Mod(ExposureGrading.MODID)
public class ExposureGrading {
    public static final String MODID = "exposure_grading";
    public static final Logger LOGGER = LoggerFactory.getLogger(ExposureGrading.class);

    public ExposureGrading(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenuTypes.MENUS.register(modBus);
        ModDataComponents.COMPONENTS.register(modBus);

        ModContainer container = ModLoadingContext.get().getActiveContainer();
        container.registerConfig(Type.CLIENT, CLIENT_SPEC);
        container.registerConfig(Type.COMMON, SERVER_SPEC);
        modBus.addListener(ModNetworking::onRegisterPayload);

        if (FMLEnvironment.dist.isClient()) {
            modBus.addListener(RegisterMenuScreensEvent.class, ClientSetup::onRegisterMenuScreens);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ClientSetup::onItemTooltip);
        }
    }
}
